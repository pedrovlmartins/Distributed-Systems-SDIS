package data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import channels.McChannel;
import exceptions.SizeException;
import messages.Header;
import messages.Message;
import peers.Peer;
import subprotocols.Restore;
import utilities.Constants;


public class Data implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 389160863840308321L;

	HashMap<ChunkInfo, ArrayList<Header>> receivedStoreMessages;

	HashMap<String, ChunksList> chunksBackedUp; //FileId as key, Array of ChuksList as value
	HashMap<String, ChunksList> chunksSaved; //FileId as key, Array of ChunkNo as value

	BackedUpFiles backedUpFiles; //HashMap containing which files are backed up, fileName as Keys
	int usedSpace;
	File chunks;
	
	public Data() {
		receivedStoreMessages = new HashMap<ChunkInfo, ArrayList<Header>>();
		chunksBackedUp = new HashMap<String, ChunksList>();
		chunksSaved = new HashMap<String, ChunksList>();
		backedUpFiles = new BackedUpFiles();
		chunks = new File(Constants.FILES_ROOT + Constants.CHUNKS_ROOT);
		usedSpace = 0;
		createFolders();
	}

	private void createFolders() {
		if (!chunks.exists())
			chunks.mkdirs();
	}


	public void saveChunk(Header header, byte[] data) throws IOException {
		File chunkFolder = new File(chunks.getPath() + "/" + header.getFileId() + "/");
		if (!chunkFolder.exists())
			chunkFolder.mkdirs();
		FileOutputStream stream = new FileOutputStream(chunkFolder.getPath() + "/" + header.getChunkNo() + ".data");
	
		try {
			if (data != null)
				stream.write(data);
		} finally {
			stream.close();
			ChunksList chunks = chunksSaved.get(header.getFileId()) != null ? chunksSaved.get(header.getFileId()) : new ChunksList();
			
			ChunkInfo chunk = new ChunkInfo(header);
			if (data != null) {
				chunk.setChunkSize((int)data.length);
				usedSpace += data.length;
			}
			chunks.addChunk(chunk);
			chunksSaved.put(header.getFileId(), chunks);
		}

	};
	public BackedUpFiles getBackedUpFiles() {
		return backedUpFiles;
	}
	public HashMap<String, ChunksList> getChunksBackedUp() {
		return chunksBackedUp;
	}

	public byte[] getChunkBody(String fileId, String chunkNo) throws IOException {
		Path restorableChunk = Paths.get(chunks.getPath() + "/" + fileId + "/" + chunkNo + ".data");
		return Files.readAllBytes(restorableChunk);
	}

	public boolean chunkIsStored(String fileId, int chunkNo) {
		ChunksList chunksList = chunksSaved.get(fileId) != null ? chunksSaved.get(fileId) : null;
		if (chunksList == null) {
			System.out.println("chunksList not found");
			return false;
		}
		for (int i = 0; i < chunksList.size(); i++)  {
			if (chunksList.get(i).getChunkNo() == chunkNo)
				return true;
		}
		System.out.println("ChunkNo not found");
		return false;
	}

	public void saveRestoredChunk(String fileName, byte[] body) throws IOException {
		FileInfo fileInfo = backedUpFiles.get(fileName);
		Restore.getOut().write(body);
		if (body.length < Constants.CHUNK_SIZE) {
			Peer.getInstance().getMdrChannel().setWaitingChunks(false);
			Restore.getOut().close();
			System.out.println("File was restored!");
			if (Restore.getNumOfChunks() != fileInfo.getNumberOfChunks())
				try {
					throw new SizeException("The restored file does not have the right number of chunks: " 
							+ Restore.getNumOfChunks() + "/" + fileInfo.getNumberOfChunks());
				} catch (SizeException e) {
					System.out.println("The number of received chunks doesn't match the number of chunks in this file");
				}
			Restore.loadDefaults();
		} else {
			Restore.incNumOfChunks();
			Restore.sendNextChunk();
		}
	}
	public void clearStoredChunks(Header header) {
		if (chunksSaved.get(header.getFileId()) != null) {
			if (header.getVersion().equals(Constants.ENHANCED_DELETE_VERSION))
				sendDeleteConfirms(header.getFileId());
			chunksSaved.remove(header.getFileId());
		}
	}
	private void sendDeleteConfirms(String fileId) {
		for (ChunkInfo chunkInfo : chunksSaved.get(fileId)) {
			System.out.println("Sending delete confirm for chunkNo " + chunkInfo.getChunkNo());
			Peer.getInstance();
			Header header = new Header(Message.CHUNK_DELETED, Constants.ENHANCED_DELETE_VERSION, Peer.getServerId(), chunkInfo.getFileId(), "" + chunkInfo.getChunkNo(), "" + chunkInfo.getReplicationDeg());
			Message message = new Message(Peer.getInstance().getMcChannel().getSocket(), Peer.getInstance().getMcChannel().getAddress(), header, null);
			int timeout = ThreadLocalRandom.current().nextInt(0, 400);
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				System.out.println("Could not wait after send a CHUNKDELETED message");
			}
			new Thread(message).start();
		}
	}

	public int getUsedSpace() {
		return usedSpace;
	}
	public HashMap<String, ChunksList> getChunksSaved() {
		return chunksSaved;
	}
	public HashMap<ChunkInfo, ArrayList<Header>> getReceivedStoreMessages() {
		return receivedStoreMessages;
	}

	public int deleteChunk(ChunkInfo chunkInfo) {
		File chunk = new File(chunks.getPath() + "/" + chunkInfo.getFileId() + "/" + chunkInfo.getChunkNo() + ".data");
		int size = (int) chunk.length();
		if(!chunk.delete()){
			System.out.println("Could not delete chunk.");
			return 0;
		}
		deleteFromChunksSaved(chunkInfo);
		McChannel.sendRemoved(chunkInfo);
		usedSpace -= size;
		return size;
	}

	private void deleteFromChunksSaved(ChunkInfo chunkInfo) {
		ChunksList chunks = chunksSaved.get(chunkInfo.getFileId());
		chunks.remove(chunkInfo);
	}

	public void addToReceivedStoreMessages(Header header) {
		ChunkInfo chunkInfo = new ChunkInfo(header);
		ArrayList<Header> headers = receivedStoreMessages.get(chunkInfo) != null ? receivedStoreMessages.get(chunkInfo) : new ArrayList<Header>();
		if(!headers.contains(header)) {
			headers.add(header);
			receivedStoreMessages.put(chunkInfo, headers);
		}
	}

	public ChunkInfo removeFromReceivedStoreMessages(Header header) {
		header.setMsgType(Message.STORED);
		ChunkInfo chunkInfo = new ChunkInfo(header);
		ArrayList<Header> headers = receivedStoreMessages.get(chunkInfo) != null ? receivedStoreMessages.get(chunkInfo) : new ArrayList<Header>();
		if(headers.contains(header)) {
			headers.remove(header);
		} 
		boolean iHaveIt = chunksSaved.get(header.getFileId()) != null  && chunksSaved.get(header.getFileId()).contains(chunkInfo) ? true : false;
		if (!iHaveIt) {
			System.out.println("I dont have this chunk");
			return null;
		}
		int replication = headers.size() + 1;
		int replicationDeg = -1;
		for (ChunkInfo info : chunksSaved.get(header.getFileId())) {
			if (info.equals(chunkInfo)) {
				replicationDeg = info.getReplicationDeg();
				if (replication < replicationDeg)
					return info;
				else
					break;
			}
		}
		return null;
	}

	public static boolean repDegAchieved(Header header) {
		ChunkInfo chunkInfo = new ChunkInfo(header);
		HashMap<ChunkInfo, ArrayList<Header>> stores = Peer.getInstance().getStorage().getReceivedStoreMessages();
		int repDeg = Integer.parseInt(header.getReplicationDeg());
		if (stores.get(chunkInfo) != null)
			if (stores.get(chunkInfo).size() >= repDeg)
				return true;
		return false;
	}

}

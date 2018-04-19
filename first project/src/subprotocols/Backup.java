package subprotocols;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import data.ChunkInfo;
import data.ChunksList;
import data.FileInfo;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;
import utilities.Utilities;

public class Backup extends Thread {
	private File file;
	private int replicationDeg;
	private boolean enhanced;
	
	public Backup(String fileName, String replicationDeg, boolean enhanced) {
		this.file = new File(Constants.FILES_ROOT + fileName);
		this.replicationDeg = Integer.parseInt(replicationDeg);
		this.enhanced = enhanced;
		if (this.replicationDeg > 9 && this.replicationDeg < 1) {
			System.out.println("ReplicationDeg must be a number between 1 and 9...");
			return;
		}
	}

	public void run() {
		try {
			byte[] data = Files.readAllBytes(file.toPath());
			sendChunks(data);
		} catch (IOException e) {
			System.out.println("The file '" + file.getName() + "' does not exist.");
		} catch (InterruptedException e) {
			System.out.println("Could not sleep while sending chunks, aborting...");
		}
	}

	private void sendChunks(byte[] data) throws InterruptedException, IOException {
		
		FileInputStream fileInputStream = new FileInputStream(Constants.FILES_ROOT + file.getName());
		byte[] buffer = new byte[Constants.CHUNK_SIZE]; // pick some buffer size
		String fileId = Utilities.getFileId(file);
		Peer.getInstance();
		String version = (String) (!enhanced ? Constants.PROTOCOL_VERSION : Constants.ENHANCED_BACKUP_VERSION);
		Header header = new Header(Message.PUTCHUNK, version, Peer.getServerId(), fileId, "0", replicationDeg + "");
		
		int bytesRead = 0;
		int numberOfChunks = 0;
		
		while ((bytesRead = fileInputStream.read(buffer)) != -1) {
			byte[] chunk = Arrays.copyOfRange(buffer, 0, bytesRead);
			header.setChunkNo(numberOfChunks + "");
			sendChunk(header, chunk);
			numberOfChunks++;
		}
		
		if (Peer.getInstance().getStorage().getBackedUpFiles().get(file.getName()) == null) {
			Peer.getInstance().getStorage();
			Peer.getInstance().getStorage().getBackedUpFiles().markAsBackedUp(file.getName(), new FileInfo(file.getName(), fileId, numberOfChunks, file.length()));
		}
		fileInputStream.close();
		System.out.println("File was backed up succesfully!");
	}


	public static void sendChunk(Header header, byte[] chunk) {
		int waitingTime = Constants.DEFAULT_WAITING_TIME;
		int chunksSent = 0;
		while (chunksSent < Constants.MAX_CHUNK_RETRY) {
			ChunkBackup backupChunk = new ChunkBackup(header, chunk);
			backupChunk.sendChunk();
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			backupChunk.checkReplies();
			ChunksList chunksList = Peer.getInstance().getStorage().getChunksBackedUp().get(header.getFileId()) != null ? Peer.getInstance().getStorage().getChunksBackedUp().get(header.getFileId()) : null;
			int confirmedBackUps = 0;
			ChunkInfo thisChunkInfo = new ChunkInfo(header, chunk.length);
			//Getting confirmedBackUps
			if (chunksList != null)
				for (ChunkInfo chunkInfo : chunksList)
					if (chunkInfo.equals(thisChunkInfo))
						confirmedBackUps = chunkInfo.getStoredHeaders().size();
			//Checking if this Peer has the chunk stored
			ChunkInfo chunkInfo = new ChunkInfo(header, chunk.length);
			if (Peer.getInstance().getStorage().getChunksSaved().get(header.getFileId()) != null) 
				if (Peer.getInstance().getStorage().getChunksSaved().get(header.getFileId()).contains(chunkInfo))
					confirmedBackUps++;
			
			int repDeg = Integer.parseInt(header.getReplicationDeg());
			if (confirmedBackUps < repDeg) {
				chunksSent++;
				waitingTime *= 2;
				System.out.println("ReplicationDeg was not achieved (" + confirmedBackUps + ") ... Waiting more " + waitingTime + "ms.");
			} else {
				break;
			}
		}
		waitingTime = Constants.DEFAULT_WAITING_TIME;
	}
}

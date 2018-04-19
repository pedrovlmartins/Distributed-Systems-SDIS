package subprotocols;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import data.FileInfo;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class Restore extends Thread {
	private static String fileName;
	private static byte[] file;
	private static int numOfChunks = 0;
	private static Header header;
	private static FileOutputStream out;
	
	public Restore(String fileName) {
		Restore.fileName = fileName;
		try {
			out = new FileOutputStream(Constants.FILES_ROOT + Constants.RESTORED + fileName);
		} catch (FileNotFoundException e) {
			System.out.println("Could not create an OutputStream");
		}
		file = new byte[0];
	}


	public void restore() {
		if (!Peer.getInstance().getStorage().getBackedUpFiles().containsKey(fileName)) {
			System.out.println("This file '" + fileName + "' was not backed up yet");
			return;
		}
		
		Peer.getInstance().getStorage();
		FileInfo fileInfo = Peer.getInstance().getStorage().getBackedUpFiles().get(fileName);
		Peer.getInstance();
		header = new Header(Message.GETCHUNK, Constants.PROTOCOL_VERSION, Peer.getServerId(), fileInfo.getFileId(), "0", null);
		
		Peer.getInstance().getMdrChannel().setWaitingChunks(true);
		sendNextChunk();
	}
	
	public void run() {
		restore();
	}

	public static byte[] getFileBytes() {
		return file;
	}
	
	public static void sendNextChunk() {
		header.setChunkNo("" + numOfChunks);
		ChunkRestore chunkRestore = new ChunkRestore(header);
		chunkRestore.sendMessage();
	}

	public static int getNumOfChunks() {
		return numOfChunks;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void loadDefaults() {
		file = new byte[0];
		numOfChunks = 0;
	}

	public static FileOutputStream getOut() {
		return out;
	}

	public static void incNumOfChunks() {
		numOfChunks++;
	}
}

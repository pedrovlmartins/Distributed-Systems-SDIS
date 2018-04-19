package subprotocols;

import java.util.ArrayList;

import data.ChunkInfo;
import data.ChunksList;
import messages.Header;
import messages.Message;
import peers.Peer;

public class ChunkBackup {
	private Message message;
	private ArrayList<Header> validReplies;
	
	public ChunkBackup(Header header, byte[] body) {
		this.message = new Message(Peer.getInstance().getMdbChannel().getSocket(), Peer.getInstance().getMdbChannel().getAddress(), header, body);
		this.validReplies = new ArrayList<>();
	}

	public void sendChunk() {
		new Thread(this.message).start();
	}
	

	private boolean validReply(Header replyHeader) {
		if (!replyHeader.getMsgType().equals(Message.STORED))
			return false;
		if (replyHeader.getSenderId().equals(Peer.getServerId()))
			return false;
		if (!replyHeader.getFileId().equals(message.getHeader().getFileId()))
			return false;
		if (!replyHeader.getChunkNo().equals(message.getHeader().getChunkNo()))
			return false;
		validReplies.add(replyHeader);
		return true;
	}

	public void checkReplies() {
		int replicationDeg = Integer.parseInt(message.getHeader().getReplicationDeg());
		Message reply;
		ArrayList<Message> storedReplies = Peer.getInstance().getMcChannel().getStoredReplies();
		int counter = 0;
		for (int i = 0; i < storedReplies.size(); i++) {
			reply = storedReplies.get(i);
			if (validReply(reply.getHeader())) {
				counter++;
			} 
			Peer.getInstance().getMcChannel().getStoredReplies().remove(storedReplies.get(i));
		}
		//Checking if this peer has the chunk saved
		ChunkInfo chunkInfo = new ChunkInfo(message.getHeader(), message.getBody().length);
		if (Peer.getInstance().getStorage().getChunksSaved().get(message.getHeader().getFileId()) != null) 
			if (Peer.getInstance().getStorage().getChunksSaved().get(message.getHeader().getFileId()).contains(chunkInfo))
				counter++;
		
		if (counter >= replicationDeg) {
			System.out.println("RepDeg achieved! Telling storage");
			tellStorage();
		}
		
	}
	
	private void tellStorage() {
		Peer.getInstance().getStorage();
		Peer.getInstance().getStorage();
		ChunksList chunksList = Peer.getInstance().getStorage().getChunksBackedUp().get(message.getHeader().getFileId()) != null ? Peer.getInstance().getStorage().getChunksBackedUp().get(message.getHeader().getFileId()) :
			new ChunksList();
		ChunkInfo chunkInfo = new ChunkInfo(message.getHeader(), message.getBody().length);
		for (ChunkInfo savedChunkInfo : chunksList) {
			if (chunkInfo.equals(savedChunkInfo)) {
				chunkInfo = savedChunkInfo;
				break;
			}
		}
		chunkInfo.addToStoredHeaders(validReplies);
		if (!chunksList.contains(chunkInfo))
			chunksList.add(chunkInfo);
		
		Peer.getInstance().getStorage().getChunksBackedUp().put(message.getHeader().getFileId(), chunksList);
	}
}

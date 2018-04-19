package subprotocols;

import java.util.Deque;
import java.util.HashMap;

import data.ChunkInfo;
import data.ChunksList;
import peers.Peer;
import utilities.Knapsack;

public class SpaceReclaim extends Thread {
	int space; //space in bytes
	
	public SpaceReclaim (int space) {
		this.space = space;
	}
	
	public void run() {
		if (Peer.getInstance().getStorage().getUsedSpace() == 0) {
			System.out.println("This peer has not any saved chunk.");
		} else if (space > Peer.getInstance().getStorage().getUsedSpace()) {
			System.out.println("Space to recover is bigger than the space used by this peer, will delete all chunks.");
			space = Peer.getInstance().getStorage().getUsedSpace();
		}
		
		ChunksList allChunks = new ChunksList();
		HashMap<String, ChunksList> chunksSaved = Peer.getInstance().getStorage().getChunksSaved();
		for (ChunksList chunks : chunksSaved.values()) {
		    allChunks.addAll(chunks);
		}
		Knapsack knapsack = new Knapsack(space, allChunks);
		Deque<ChunkInfo> chunksToDelete = knapsack.solve();
		while (!chunksToDelete.isEmpty()) {
			Peer.getInstance().getStorage().deleteChunk(chunksToDelete.pop());
		}
	}
	
}

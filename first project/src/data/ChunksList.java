package data;

import java.io.Serializable;
import java.util.ArrayList;

public class ChunksList extends ArrayList<ChunkInfo> implements Serializable {

	private static final long serialVersionUID = 1L;

	public void addChunk(ChunkInfo chunk) {
		for (int i = 0; i < this.size(); i++) {
			if (chunk.getChunkNo() == this.get(i).getChunkNo())
				return;
		}
		this.add(chunk);
	}
}

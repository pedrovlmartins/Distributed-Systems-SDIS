package subprotocols;

import messages.Header;
import messages.Message;
import peers.Peer;

public class ChunkRestore {
	Message message;
	public ChunkRestore(Header header) {
		this.message = new Message(Peer.getInstance().getMcChannel().getSocket(), Peer.getInstance().getMcChannel().getAddress(), header, null);
	}
	public void sendMessage() {
		new Thread(this.message).start();
	}

}

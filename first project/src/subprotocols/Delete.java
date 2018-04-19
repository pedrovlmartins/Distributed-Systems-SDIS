package subprotocols;

import java.io.File;

import enhancements.EnhancedDelete;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;
import utilities.Utilities;

public class Delete extends Thread {
	String fileName;
	boolean enhanced;
	public Delete(String fileName, boolean enhanced) {
		this.fileName = fileName;
		this.enhanced = enhanced;
	}

	public void run() {
		File file = new File(Constants.FILES_ROOT + fileName);
		String fileId = Utilities.getFileId(file);
		if (!enhanced) {
			Peer.getInstance();
			Header header = new Header(Message.DELETE, Constants.PROTOCOL_VERSION, Peer.getServerId(), fileId, null, null);
			Message deleteMsg = new Message(Peer.getInstance().getMcChannel().getSocket(), Peer.getInstance().getMcChannel().getAddress(), header, null);
			new Thread(deleteMsg).start();
		} else {
			new EnhancedDelete(fileId).start();
		}
		if(file.delete()){
			System.out.println(file.getName() + " is deleted!");
		}else{
			System.out.println("Delete operation failed.");
		}
		Peer.getInstance().getStorage().getBackedUpFiles().remove(file.getName());
		if (!enhanced)
			Peer.getInstance().getStorage().getChunksBackedUp().remove(fileId);
	}
}

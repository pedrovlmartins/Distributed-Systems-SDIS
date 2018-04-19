package messages;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import utilities.Constants;
import utilities.Utilities;

public class Message implements Runnable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TEST = "TEST";
	public static final String PUTCHUNK = "PUTCHUNK";
	public static final String STORED = "STORED";
	public static final String GETCHUNK = "GETCHUNK";
	public static final String CHUNK = "CHUNK";
	public static final String DELETE = "DELETE";
	public static final String REMOVED = "REMOVED";
	public static final String CHUNK_DELETED = "CHUNKDELETED";
	
	
	private MulticastSocket socket;
	private InetAddress address;
	private Header header;
	private byte[] body;

	
	public Message(MulticastSocket socket, InetAddress address, Header header, byte[] body) {
		this.socket = socket;
		this.address = address;
		this.header = header;
		this.body = body;
	}
	

	
	public Message(Header header, byte[] body) {
		this.header = header;
		this.body = body;
		this.socket = null;
		this.address = null;
	}



	public static String[] splitArgs(String message) {
		return message.split(" ");
	}

	@Override
	public void run() {
		System.out.println("Sending " + header.getMsgType());
		byte[] headerBytes = header.toString().getBytes();
		byte[] message = {};
		
		if (body != null) {
			message = Utilities.concatenateBytes(headerBytes, body);
		} else {
			message = headerBytes;
		}
		
		DatagramPacket packet = new DatagramPacket(message,
				message.length, address,
				socket.getLocalPort());
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send chunk");
		}
	}


	public static Message getMessageFromData(byte[] data) {
		String emptyLine = Constants.CRLF + Constants.CRLF;
		int index = Utilities.findString(data, emptyLine.getBytes());
		String headerStr = new String(data, 0, index);
		byte[] body = index + 4 < data.length ? Arrays.copyOfRange(data, index + 4, data.length) : null;
		String[] splittedHeader = Message.splitArgs(headerStr);
		String chunkNo = splittedHeader.length > Constants.CHUNK_NO ? splittedHeader[Constants.CHUNK_NO] : null;
		String replicationDeg = splittedHeader.length > Constants.REPLICATION_DEG ? splittedHeader[Constants.REPLICATION_DEG] : null;
		Header header = new Header(splittedHeader[Constants.MESSAGE_TYPE], splittedHeader[Constants.VERSION], splittedHeader[Constants.SENDER_ID],
				splittedHeader[Constants.FILE_ID], chunkNo, replicationDeg);
		Message msg = new Message(header, body);
		return msg;
	}


	public Header getHeader() {
		return header;
	}
	public byte[] getBody() {
		return body;
	}

}

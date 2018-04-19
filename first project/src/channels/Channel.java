package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;



public class Channel {
	Thread thread;
	MulticastSocket socket;
    InetAddress address;
	private int port;
	
	Channel(String address, String port) throws IOException {
		this.address = InetAddress.getByName(address);
		this.port = Integer.parseInt(port);
		this.socket = new MulticastSocket(this.port);
		this.socket.setTimeToLive(1);
	}
	
	public void listen() {
		this.thread.start();
	}
	
	public byte[] rcvMultiCastData() throws IOException {
        byte[] rbuf = new byte[utilities.Constants.CHUNK_SIZE + 1000];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        return data;
	}

	
	public MulticastSocket getSocket() {
		return socket;
	}

	public InetAddress getAddress() {
		return address;
	}
	
	
}

package launcher;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TestApp {
	static DatagramSocket socket;
	private static InetAddress address;
	private static int portNumber;
	
	public static void main(String[] args) throws SocketException {
		if (args.length < 2) {
			System.out.println("Wrong usage! Usage: java launcher.TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> ");
			System.out.println("Exiting...");
			return;
		}
		socket = new DatagramSocket();
		getSocketInfo(args[0]);
		String[] commands = Arrays.copyOfRange(args, 1, args.length);
		StringBuilder builder = new StringBuilder();
		for(String s : commands) {
		    builder.append(s + " ");
		}
		byte[] buf = builder.toString().getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, portNumber);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send the packet");
		}
	}
	private static void getSocketInfo(String string) {
		if (string.contains(":")) {
			String[] info = string.split(":");
			if (info.length == 1) {
				getLocalHost(Integer.parseInt(info[1]));
			} else if (info.length == 2) {
				try {
					address = InetAddress.getByName(info[1]);
				} catch (UnknownHostException e) {
					System.out.println("Could not get host: " + info[1]);
				}
				portNumber = Integer.parseInt(info[2]);
			}
		} else {
			getLocalHost(Integer.parseInt(string));
		}
	}
	private static void getLocalHost(int port) {
		portNumber = port;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Could not get localhost");
		}
	}
}

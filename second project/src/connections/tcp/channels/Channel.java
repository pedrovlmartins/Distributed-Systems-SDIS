package connections.tcp.channels;

import utilities.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

/**
 * Created by Pedro Fraga on 22-May-16.
 */
public class Channel {

    private Thread thread;
    private MulticastSocket socket;
    private InetAddress address;
    private int port;

    Channel(String address, int port) throws IOException {
        this.address = InetAddress.getByName(address);
        this.port = port;
        this.socket = new MulticastSocket(this.port);
        this.socket.setTimeToLive(1);
    }

    public byte[] rcvMultiCastMsg() {
        byte[] rbuf = new byte[Constants.MSG_SIZE];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.err.println(getChannelTag() + "Could not receive packet");
            return null;
        }
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        return data;
    }

    void joinGroup() {
        try {
            socket.joinGroup(address);
        } catch (IOException e) {
            System.err.println(getChannelTag() + "Could not join group");
            return;
        }
    }

    void leaveGroup() {
        try {
            socket.leaveGroup(address);
        } catch (IOException e) {
            System.err.println(getChannelTag() + "Could not leave group");
            return;
        }
    }

    public void listen() {
        thread.start();
    }

    void setThread(Thread thread) {
        this.thread = thread;
    }

    public String getChannelTag() {
        return "[" + address.getHostAddress() + ":" + socket.getLocalPort() + "] ";
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public InetAddress getAddress() {
        return address;
    }

}

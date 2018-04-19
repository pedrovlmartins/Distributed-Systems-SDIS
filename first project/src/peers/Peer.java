package peers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import channels.McChannel;
import channels.MdbChannel;
import channels.MdrChannel;
import data.Data;
import exceptions.ArgsException;
import messages.Message;
import subprotocols.Backup;
import subprotocols.Delete;
import subprotocols.Restore;
import subprotocols.SpaceReclaim;
import utilities.Constants;

public class Peer {
	private static String serverId;
	
	private McChannel mcChannel;
	private MdbChannel mdbChannel;
	private MdrChannel mdrChannel;
	
	private Data storage;
	private DatagramSocket socket;
	private static Peer peer;
	
	public static Peer getInstance() {
		return peer;
	}
	
	public Peer(String serverId, String mcAddress, String mcPort,
			String mdbAddress, String mdbPort, String mdrAddress,
			String mdrPort) throws IOException {
		Peer.serverId = serverId;
		
		mcChannel = new McChannel(mcAddress, mcPort);
		mdbChannel = new MdbChannel(mdbAddress, mdbPort);
		mdrChannel = new MdrChannel(mdrAddress, mdrPort);
		socket = new DatagramSocket(Integer.parseInt(serverId));
		
		loadData();
	
		peer = this;
	}

	private void listenChannels() {
		mcChannel.listen();
		mdbChannel.listen();
		mdrChannel.listen();
	}
	
	private void listenActions() throws ArgsException {
		System.out.println("Listening TestApp...");
		String received = "";
		while(received != "quit") {
			byte[] rbuf = new byte[utilities.Constants.MAX_MSG_SIZE];
			DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
			try {
				this.socket.receive(packet);
			} catch (IOException e) {
				System.out.println("This peer could not receive a packet from TestApp...");
			}
			received = new String(packet.getData(), 0, packet.getLength());
			String[] command = Message.splitArgs(received);
			switch (command[0]) {
			case "BACKUP":
				Backup backup = new Backup(command[1], command[2], false);
				backup.start();
				break;
			case "ENHANCEBACKUP":
				Backup enhancedBackup = new Backup(command[1], command[2], true);
				enhancedBackup.start();
				break;
			case "RESTORE":
				Restore restore = new Restore(command[1]);
				restore.start();
				break;
			case "DELETE":
				Delete delete = new Delete(command[1], false);
				delete.start();
				break;
			case "ENHANCEDDELETE":
				Delete enhancedDelete = new Delete(command[1], true);
				enhancedDelete.start();
				break;
			case "RECLAIM":
				SpaceReclaim spaceReclaim = new SpaceReclaim(Integer.parseInt(command[1]));
				spaceReclaim.start();
				break;
			case "QUIT":
				saveData();
			default:
				System.out.println("Unknown command: " + received);
				break;
			}
		}
	}

	
	public static void main(String[] args) throws ArgsException, IOException {
		if (args.length != 7)
			throw new ArgsException("java peers.Peer <Server ID> <MC> <MC port> <MDB> <MDB port> <MDR> <MDR port>");
		Peer peer = new Peer(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
		peer.listenChannels();
		peer.listenActions();
	}
	
	public void saveData() {
		try {
			FileOutputStream fileOut =
					new FileOutputStream(Constants.DATABASE_PATH);
			ObjectOutputStream output = new ObjectOutputStream(fileOut);
			output.writeObject(this.storage);
			output.close();
			fileOut.close();
		}  
		catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public void loadData() {
		try
		{
			File dataBase = new File(Constants.DATABASE_PATH);
			if (!dataBase.exists()) {
				System.out.println("Creating new file.");
				File dir = new File(Constants.DATA_PATH);
				dir.mkdirs();
				dataBase.createNewFile();
				storage =  new Data();
				saveData();
				return;
			}
			FileInputStream fileIn = new FileInputStream(Constants.DATABASE_PATH);
			ObjectInputStream input = new ObjectInputStream(fileIn);

			this.storage = (Data)input.readObject();
			input.close();
			fileIn.close();
			return;
		}
		catch(ClassNotFoundException ex){
			System.out.println("Cannot perform input. Class not found.");
		}
		catch(IOException ex){
			System.out.println("Could not load data, maybe the file does not exist.");
		}
		this.storage = null;
	}
	
	/* Getters */
	public McChannel getMcChannel() {
		return mcChannel;
	}


	public MdbChannel getMdbChannel() {
		return mdbChannel;
	}


	public MdrChannel getMdrChannel() {
		return mdrChannel;
	}

	
	public static String getServerId() {
		return serverId;
	}

	public Data getStorage() {
		return storage;
	}
	
}

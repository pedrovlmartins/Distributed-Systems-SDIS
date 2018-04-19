package utilities;

import peers.Peer;

public class Constants {
	public static final String IP = "225.0.0.0";
	public static final int MC_PORT = 4444;
	public static final int MDB_PORT = 4445;
	public static final int MDR_PORT = 4446;

	public static final String CRLF = "\r\n";

	public static final String FILES_ROOT = "../files/";
	public static final String CHUNKS_ROOT = "chunks_" + Peer.getServerId();
	public static final String RESTORED = "restored_";
	public static final String DATA_PATH = "../data_" + Peer.getServerId();
	public static final String DATABASE_PATH = DATA_PATH + "/data.ser";
	
	public static final int CHUNK_SIZE = 64 * 1000;
	public static final int MAX_CHUNK_RETRY = 5;
	public static final int DEFAULT_WAITING_TIME = 500;
	public static final int MAX_WAITING_TIME = 16000;
	
	public static final int MESSAGE_TYPE = 0;
	public static final int VERSION = 1;
	public static final int SENDER_ID = 2;
	public static final int FILE_ID = 3;
	public static final int CHUNK_NO = 4;
	public static final int REPLICATION_DEG = 5;
	public static final int DATA = 6;

	public static final String PROTOCOL_VERSION = "1.0";
	public static final String ENHANCED_DELETE_VERSION = "1.1";
	public static final Object ENHANCED_BACKUP_VERSION = "1.2";
	public static final String ENHANCED_RESTORE_VERSION = "1.3";
	
	public static final int DEFAULT_PORT = 1025;
	public static final int MAX_MSG_SIZE = 256;
}

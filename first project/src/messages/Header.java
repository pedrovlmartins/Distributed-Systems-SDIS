package messages;

import java.io.Serializable;

import utilities.Constants;

public class Header implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String stringType;
	private String version;
	private String senderId;
	private String fileId;
	private String chunkNo;
	private String replicationDeg;
	
	public Header(String stringType, String version, String senderId,
			String fileId, String chunkNo, String replicationDeg) {
		this.stringType = stringType;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
	}
	
	public String getMsgType() {
		return stringType;
	}

	public String getVersion() {
		return version;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getFileId() {
		return fileId;
	}

	public String getChunkNo() {
		return chunkNo;
	}

	public String getReplicationDeg() {
		return replicationDeg;
	}
	
	public void setMsgType(String type) {
		this.stringType = type;
	}
	
	public String toString() {
		String string = "";
		string += this.getMsgType() != null ? this.getMsgType() + " " : "";
		string += this.getVersion() != null ? this.getVersion() + " " : "";
		string += this.getSenderId() != null ? this.getSenderId() + " " : "";
		string += this.getFileId() != null ? this.getFileId() + " " : "";
		string += this.getChunkNo() != null ? this.getChunkNo() + " " : "";
		string += this.getReplicationDeg() != null ? this.getReplicationDeg() + " " : "";
		string += Constants.CRLF + Constants.CRLF;
		return string;
	}
	
	public void setChunkNo(String chunkNo) {
		this.chunkNo = chunkNo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chunkNo == null) ? 0 : chunkNo.hashCode());
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		result = prime * result + ((senderId == null) ? 0 : senderId.hashCode());
		result = prime * result + ((stringType == null) ? 0 : stringType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Header other = (Header) obj;
		if (chunkNo == null) {
			if (other.chunkNo != null)
				return false;
		} else if (!chunkNo.equals(other.chunkNo))
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		if (senderId == null) {
			if (other.senderId != null)
				return false;
		} else if (!senderId.equals(other.senderId))
			return false;
		if (stringType == null) {
			if (other.stringType != null)
				return false;
		} else if (!stringType.equals(other.stringType))
			return false;
		return true;
	}	
}

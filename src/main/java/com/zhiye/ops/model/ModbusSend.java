package com.zhiye.ops.model;

/**
 * @author zl
 */

public class ModbusSend {
	private String readOrWrit; /// ����д����

	private int slaveId; // ��վ��ַ

	private int start; // ���ݿ�ʼ��ַ

	private int len; // ���ݶ�

	private short[] shrtArray; // д��ֵ�ĳ���


	private boolean[] boolArray;
	

	public String getReadOrWrit() {
		return readOrWrit;
	}

	public void setReadOrWrit(String readOrWrit) {
		this.readOrWrit = readOrWrit;
	}

	public int getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(int slaveId) {
		this.slaveId = slaveId;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}


	public boolean[] getBoolArray() {
		return boolArray;
	}

	public void setBoolArray(boolean[] boolArray) {
		this.boolArray = boolArray;
	}
	
	public short[] getShrtArray() {
		return shrtArray;
	}

	public void setShrtArray(short[] shrtArray) {
		this.shrtArray = shrtArray;
	}


}

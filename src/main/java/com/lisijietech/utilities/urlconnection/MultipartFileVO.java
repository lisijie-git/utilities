package com.lisijietech.utilities.urlconnection;

/**
 * http请求的nultipart/form-data内容类型的文件数据对象
 * @author lisijie
 *
 */
public class MultipartFileVO {
	private String name;
	private String fileName;
	private byte[] data;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
}

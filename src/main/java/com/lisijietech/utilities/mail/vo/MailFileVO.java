package com.lisijietech.utilities.mail.vo;

/**
 * 邮件的文件数据对象。
 * 文件对象都差不多，以后设计一个全局通用文件对象。
 * @author lisijie
 * @date 2020-8-31
 */
public class MailFileVO {
	//name最好是个唯一值，在内嵌图片时需要cid，使用了name存储，所以要防止冲突。
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

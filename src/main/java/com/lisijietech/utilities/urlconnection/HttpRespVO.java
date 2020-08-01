package com.lisijietech.utilities.urlconnection;

import java.util.Map;

/**
 * Http响应数据对象。
 * 如果是获取字符串数据，直接用字符流解析消息体数据为字符串类型，并用content接收。
 * 如果是下载文件，文件名通过对应消息头解析后用fileName成员属性接收，文件数据用字节数组datas接收。
 * @author lisijie
 *
 */
public class HttpRespVO {
	//状态码
	private Integer code;
	//消息头
	private Map<String,String> headers;
	//文件名
	private String fileName;
	//消息体内容，字节数组格式
	private byte[] datas;
	//消息体内容，字符串格式
	private String content;
	
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte[] getDatas() {
		return datas;
	}
	public void setDatas(byte[] datas) {
		this.datas = datas;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}

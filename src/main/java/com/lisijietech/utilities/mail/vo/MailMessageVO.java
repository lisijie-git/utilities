package com.lisijietech.utilities.mail.vo;

import java.util.List;
import java.util.Map;

/**
 * 邮件消息数据对象
 * @author lisijie
 * @date 2020-8-31
 */
public class MailMessageVO {
	//发件人地址
	private String fromAddr;
	//发件人昵称。
	private String fromPersonal;
	//邮件发送类型。默认0为发送，1为回复。回复就是发送，只是报文可能有回复人消息头，看网络参考一般是拼接消息内容，和title前加回复。
	//https://blog.csdn.net/gh670011677/article/details/77712189?locationNum=10&fps=1
	//此属性暂时没用，以后再扩展完善。
	private String sendType;
	//邮件回执（收件人阅读邮件时提示发件人，标明邮件已收到，已阅读）。同样是有消息头，但实现个人认为还是发邮件，要自己实现。
	//暂时没用。
	private String notification;
	//收件人地址，地址-昵称键值对。
	private Map<String,String> to;
	//抄送人地址。地址-昵称键值对。Carbon Copy缩写
	private Map<String,String> cc;
	//密送人地址。地址-昵称键值对。Blind Carbon Copy缩写
	private Map<String,String> bcc;
	//邮件标题
	private String title;
	//邮件内容。可以是html，也可以是纯文本
	private String content;
	//邮件纯文本内容。基本不使用，为了兼容，因为有些邮件系统不支持html文本，可通过html相关操作组件从content的html文本获得纯文本。
	private String text;
	//邮件内嵌文件。一般是内嵌图片。
	private List<MailFileVO> innerFiles;
	//邮件附件
	private List<MailFileVO> attachments;
	//字符集
	private String charset;
	
	public String getFromAddr() {
		return fromAddr;
	}
	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}
	public String getFromPersonal() {
		return fromPersonal;
	}
	public void setFromPersonal(String fromPersonal) {
		this.fromPersonal = fromPersonal;
	}
	public String getSendType() {
		return sendType;
	}
	public void setSendType(String sendType) {
		this.sendType = sendType;
	}
	public String getNotification() {
		return notification;
	}
	public void setNotification(String notification) {
		this.notification = notification;
	}
	public Map<String, String> getTo() {
		return to;
	}
	public void setTo(Map<String, String> to) {
		this.to = to;
	}
	public Map<String, String> getCc() {
		return cc;
	}
	public void setCc(Map<String, String> cc) {
		this.cc = cc;
	}
	public Map<String, String> getBcc() {
		return bcc;
	}
	public void setBcc(Map<String, String> bcc) {
		this.bcc = bcc;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<MailFileVO> getInnerFiles() {
		return innerFiles;
	}
	public void setInnerFiles(List<MailFileVO> innerFiles) {
		this.innerFiles = innerFiles;
	}
	public List<MailFileVO> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<MailFileVO> attachments) {
		this.attachments = attachments;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
}

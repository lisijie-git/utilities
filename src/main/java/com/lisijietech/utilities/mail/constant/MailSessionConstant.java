package com.lisijietech.utilities.mail.constant;

/**
 * 邮件Session设置Properties属性的key
 * @author lisijie
 * @date 2020-9-4
 */
public interface MailSessionConstant {
	//传输协议
	public static final String MAIL_PROTOCOL = "mail.transport.protocol";
	//传输主机地址,没指定协议
	public static final String MAIL_HOST = "mail.host";
	//SMTP传输主机地址，不用指定MAIL_PROTOCOL协议。
	public static final String SMTP_HOST = "mail.smtp.host";
	//SMTP传输端口，如果不是默认25，或者SSL加密的465，需要指定。
	public static final String SMTP_PORT = "mail.smtp.port";
	//SMTP是否认证，一般客户端代理需要认证，服务器间传输不需要，服务器间认证是校验ip的DNS的MX方式
	public static final String SMTP_AUTH = "mail.smtp.auth";
	//SMTP是否使用SSL安全连接，一般都使用，QQ就使用安全连接
	public static final String SMTP_SSL = "mail.smtp.ssl.enable";
	//SMTP是否使用EHLO命令交互。默认true，设置为false将用HELO命令
	public static final String SMTP_EHLO = "mail.smtp.ehlo";
	//是否开启debug模式，开启会打印网络交互过程。可通过session的setDebug方法设置。
	public static final String MAIL_DEBUG = "mail.debug";
}

package com.lisijietech.utilities.mail.constant;

/**
 * 邮件服务器地址。
 * 这个类不必要。
 * 可以通过DNS服务网络请求得到MX信息获取地址，以后完善扩展DNS网络请求功能。
 * @author lisijie
 * @date 2020-9-5
 */
public interface MailAddressConstant {
	//qq
	public static final String MAIL_QQ = "qq.com";
	//第三方授权
	public static final String MAIL_QQ_SMTP = "smtp.qq.com";
	//通过DNS访问qq.com获得的邮件交换服务器地址。
	public static final String MAIL_QQ_MX1 = "mx1.qq.com";
	public static final String MAIL_QQ_MX2 = "mx2.qq.com";
	public static final String MAIL_QQ_MX3 = "mx3.qq.com";
}

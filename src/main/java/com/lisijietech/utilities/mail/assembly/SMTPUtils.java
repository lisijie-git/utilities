package com.lisijietech.utilities.mail.assembly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import com.lisijietech.utilities.mail.constant.MailSessionConstant;
import com.lisijietech.utilities.mail.vo.MailFileVO;
import com.lisijietech.utilities.mail.vo.MailMessageVO;

/**
 * 邮件SMTP协议工具类。
 * 邮件系统基本概念：
 * MUA（Message User Agent 消息用户代理），客户端，通过SMTP协议发送邮件到服务器。
 * MTA（Message Transfer Agent消息传送代理），服务器端，通过SMTP协议发送或转发邮件到其他服务器。
 * MDA（Message Deliver Agent消息投递代理），将邮件保存到指定地方，如磁盘，数据库等。
 * MRA（Message Receive Agent消息接受代理），用户收取邮件模块，通过POP3,IMAP等协议，个人认为可用HTML简单实现。MUA与MRA交互来收邮件。
 * SMTP（Simple Mail Transfer Protocol），传输发送邮件的标准协议。
 * IMAP（Internet Mail Access Protocol），收邮件的协议之一，qq邮件有用此协议。
 * POP3（Post Office Protocol 3），收邮件的协议之一。
 * 
 * MUA和MTA原理一样，电子邮件客户端不能直接传输邮件到目标服务器，因为要反垃圾邮件和病毒攻击，小部分利益关系。
 * MUA发邮件需要先发送到自己所属的邮件服务器，通过SMTP协议，服务器会校验认证，从MUA传输的SMTP协议中的认证数据。
 * 源服务器通过MTA发送到目标服务器，通过SMTP协议，目标服务器也会校验认证，但是是通过获取源ip从DNS获取的MX等信息。
 * 所以开发邮件系统需要有域名和服务器，主要是反垃圾和病毒攻击，小部分利益原因。
 * 当然网络上大部分都是通过第三方邮件代理的教程，用qq或者其他邮件服务代理，省去了域名和服务器的成本，但很容易被卡脖子。
 * 
 * SMTP协议很重要，开放、约定、统一的协议，能够使不同公司邮件系统能互相发邮件。
 * <br>
 * 实现参考：<br>
 * https://blog.csdn.net/qq_36474549/article/details/83342615<br>
 * https://blog.csdn.net/xyang81/article/details/7675152<br>
 * https://www.cnblogs.com/xmqa/p/8458300.html<br>
 * https://blog.csdn.net/baolingye/article/details/96598222<br>
 * https://blog.csdn.net/chenssy/article/details/8277515<br>
 * https://blog.csdn.net/zqz_zqz/article/details/80250654<br>
 * https://blog.csdn.net/suhuaiqiang_janlay/article/details/78766261<br>
 * @author lisijie
 * @date 2020-8-31
 */
public class SMTPUtils {
	/**
	 * 发送邮件。
	 * @param session
	 * @param message
	 * @param user 注意如果是中文要编码，转换成base64等非中文字符
	 * @param password 注意如果是中文要编码，转换成base64等非中文字符
	 * @return 返回值为null代表成功。这里的异常应该向上抛，让上层业务决定怎么处理。在此层当做业务处理不太合适，只是学习案列使用。
	 * @throws MessagingException 
	 */
	public static String send(Session session,MimeMessage message,String user,String password) 
			throws MessagingException {
		Transport ts = null;
		try {
			//通过session得到传输对象。
			ts = session.getTransport();
			//Transport对象调用connect方法连接目标服务器。connect有对个重载方法，可以设置host，port，user，password，
			//和session的设置重叠，如果session中都设置了，这里可不设置，直接使用connect()无参方法。
			//session中设置user和password时，是通过在获取session时，传入Authenticator接口实现的类。
			//默认session的auth为"false"时，将判断user和password是否为null，来决定是否认证。都为null，则不认证。
			//如果无需认证，这里都传入null就行。
			ts.connect(user, password);
			//发送邮件
			ts.sendMessage(message, message.getAllRecipients());
		} finally {
			if(ts != null) {
				try {
					//关闭邮件连接
					ts.close();
				} catch (MessagingException e) {
					return "关闭失败";
				}
			}
		}
		return null;
	}
	
	/**
	 * 得到邮件session
	 * @param prop
	 */
	public static Session getSession(Properties props) {
		return Session.getInstance(props);
	}
	
	/**
	 * 得到邮件session。
	 * @param host
	 * @param authFlag 是否要认证
	 * @param debugFlag 是否debug输出交互过程。
	 * @return
	 */
	public static Session getSession(String host,String authFlag,String debugFlag) {
		Properties props = new Properties();
		props.setProperty(MailSessionConstant.MAIL_PROTOCOL, "smtp");
		props.setProperty(MailSessionConstant.MAIL_HOST, host);
		props.setProperty(MailSessionConstant.SMTP_SSL, "true");
		
		//一般组件在得到Session时，通过是否传入Authenticator接口实现的类，来判断设置是否要认证。
		//或者Session得到的Transport，调用connect方法时，传入用户和密码来判断是否要认证。
		//不建议设置此属性。
		//mail.smtp.auth默认为"false"。
		//https://blog.csdn.net/zqz_zqz/article/details/80250654
		if(authFlag != null) {
			props.setProperty(MailSessionConstant.SMTP_AUTH, authFlag);
			//下面一行不必要。默认为true使用EHLO命令(HELO扩展)，false使用HELO命令
			props.setProperty(MailSessionConstant.SMTP_EHLO, authFlag);
		}
		
		props.setProperty(MailSessionConstant.MAIL_DEBUG, debugFlag);
		return getSession(props);
	}
	
	/**
	 * 纯文本邮件。
	 * @param session 邮件session
	 * @param from 邮件发送来源
	 * @param formPersonal 发件人昵称
	 * @param to 邮件发送目标
	 * @param toPersonal 目标人昵称
	 * @param title 邮件标题
	 * @param text 邮件内容
	 * @param charset 字符集
	 * @return
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException 
	 */
	public static MimeMessage createTextMail(Session session,String from,String formPersonal,String to,String toPersonal
			,String title,String text,String charset) 
					throws AddressException, MessagingException, UnsupportedEncodingException {
		//创建邮件对象
		MimeMessage message = new MimeMessage(session);
		//设置邮件发件人
		message.setFrom(new InternetAddress(from,formPersonal,charset));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(to,toPersonal,charset));
		//邮件日期
		message.setSentDate(new Date());
		//邮件标题
		message.setSubject(title,charset);
		//邮件内容
		message.setText(text,charset);
		//保存邮件
		message.saveChanges();
		return message;
	}
	
	/**
	 * 简单html邮件
	 * @param session
	 * @param from
	 * @param fromPersonal 发件人昵称
	 * @param to
	 * @param toPersonal 目标人昵称
	 * @param title
	 * @param content 内容可以是html文本
	 * @param charset 字符集
	 * @return
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException 
	 */
	public static MimeMessage createSimpleHtmlMail(Session session,String from,String fromPersonal,String to
			,String toPersonal,String title,String content,String charset) 
					throws AddressException, MessagingException, UnsupportedEncodingException {
		//创建邮件对象
		MimeMessage message = new MimeMessage(session);
		//设置邮件发件人
		message.setFrom(new InternetAddress(from,fromPersonal,charset));
		//设置邮件收件人
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(to,toPersonal,charset));
		//邮件日期
		message.setSentDate(new Date());
		//邮件标题
		message.setSubject(title,charset);
		//邮件内容
		message.setContent(content, "text/html;charset=" + charset);
		//保存邮件
		message.saveChanges();
		return message;
	}
	
	/**
	 * 生成内嵌图片的html邮件。
	 * <br>
	 * 参考：
	 * https://blog.csdn.net/xyang81/article/details/7675152<br>
	 * https://blog.csdn.net/qq_36474549/article/details/83342615<br>
	 * https://blog.csdn.net/suhuaiqiang_janlay/article/details/78766261，报文原理<br>
	 * @param session
	 * @param mesVO
	 * @return
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static MimeMessage createInnerImageHtmlMail(Session session,MailMessageVO mesVO) 
			throws AddressException, MessagingException, UnsupportedEncodingException {
		
		String charset = mesVO.getCharset();
		
		//创建邮件对象
		MimeMessage message = new MimeMessage(session);
		//设置邮件发件人
		message.setFrom(new InternetAddress(mesVO.getFromAddr(),mesVO.getFromPersonal(),mesVO.getCharset()));
		//设置邮件收件人数组
		message.setRecipients(Message.RecipientType.TO, createInternetAddressArr(mesVO.getTo(),charset));
		//设置邮件抄送人数组
		Map<String,String> cc = mesVO.getCc();
		if(cc != null && cc.size() > 0) {
			message.setRecipients(Message.RecipientType.CC, createInternetAddressArr(cc,charset));
		}
		//设置邮件抄送人数组
		Map<String,String> bcc = mesVO.getBcc();
		if(bcc != null && bcc.size() > 0) {
			message.setRecipients(Message.RecipientType.BCC, createInternetAddressArr(bcc,charset));
		}
		//设置发送时间
		message.setSentDate(new Date());
		//设置标题。注意字符集
		message.setSubject(mesVO.getTitle(),charset);
		
		//设置内容MIME类型
		//创建一个MIME类型为related（父类型为Multipart，子类型为related）的MimeMultipart对象
		MimeMultipart mp = new MimeMultipart("related");
		
		//设置文本
		//创建一个表示正文的MimeBodyPart对象，并加入到MimeMultipart类型的mp对象中。
		//可能会有html超文本和纯文本多段分段。多段没意义。但有些邮箱不支持html，所以最多一段超文本一段纯文本，系统自己选择，兼容考虑。
		MimeBodyPart htmlPart = new MimeBodyPart();
		
		//加入到MimeMultipart类型的mp对象中。
		mp.addBodyPart(htmlPart);
		
		//文本内容body再分段，html文本，纯文本。此处没有纯文本，只是举例，需要纯文本则在htmlMultPart加入纯文本body。
		//创建一个MIME类型为alternative（父类型为Multipart，子类型为alternative）的MimeMultipart对象
		MimeMultipart htmlMultPart = new MimeMultipart("alternative");
		
		//html文本设置。
		MimeBodyPart htmlbodyPart = new MimeBodyPart();
		//html内嵌img，需要在html文本中这样写法，<img src="cid:sss">,sss代表cid,在内嵌图片中有设置
		htmlbodyPart.setContent(mesVO.getContent(), "text/html;charset=" + charset);
		
		//body添加进part中
		htmlMultPart.addBodyPart(htmlbodyPart);
		
		//将MimeMultipart类型的htmlMultPart对象设置为htmlPart的内容。
		htmlPart.setContent(htmlMultPart);
		
		
		//设置内嵌图片
		//如果有内嵌图片。
		List<MailFileVO> innerImg = mesVO.getInnerFiles();
		if(innerImg != null && innerImg.size() > 0) {
			//批量设置内嵌图片邮件体
			for(MailFileVO file :innerImg) {
				//创建一个表示图片的MimeBodyPart对象，并加入到MimeMultipart类型的mp对象中。
				MimeBodyPart imgPart = new MimeBodyPart();
				mp.addBodyPart(imgPart);
				
				//设置内嵌图片邮件体
				String fileName = file.getFileName();
				String imgType = fileName.substring(fileName.lastIndexOf(".") + 1);
				//这里的content-type = image/imgType可能会有问题，简单实现，需要对图片类型表去设置。
				DataSource ds = new ByteArrayDataSource(file.getData(), "image/" + imgType);
				DataHandler dh = new DataHandler(ds);
				imgPart.setDataHandler(dh);
				//cid使用name代替，name需要是唯一值
				imgPart.setContentID(file.getName());
			}
		}
		
		//将MimeMultipart类型的mp对象设置为整个邮件的内容
		message.setContent(mp);
		//保存并生成最终邮件内容。
		message.saveChanges();
		
		return message;
	}
	
	/**
	 * 带内嵌图片、附件、各种类型多收件人（发送、抄送、密送、回复，显示邮箱姓名）、邮件优先级、阅读回执的完整的HTML邮件。
	 * 
	 * 邮件优先级。不了解，应该是邮件服务器接收邮件模块需要实现的功能，发送只是加入报文消息头信息，以后再了解。
	 * 回复邮件。邮件服务器发送模块，需要拼接title，回复正文和回复历史正文，报文消息头可设置可不设置。
	 * 阅读回执。需要邮件系统有回执模块，以后再了解实现。过程猜想大概如下：
	 * 邮件服务器发送邮件，设置报文消息头表示需要回执。
	 * 收邮件服务器，收到需要回执的邮件，邮件服务器根据邮件接收状态，和用户阅读状态，多次发送对应状态回执邮件。
	 * 源发邮件服务器，判断是回执邮件，在邮件系统中，回执模块，更新邮件发送状态。
	 * @param session
	 * @param mesVO
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public static MimeMessage createMultipleMail(Session session,MailMessageVO mesVO) 
			throws UnsupportedEncodingException, MessagingException {
		
		String charset = mesVO.getCharset();
		
		//创建邮件对象
		MimeMessage message = new MimeMessage(session);
		//设置邮件发件人
		message.setFrom(new InternetAddress(mesVO.getFromAddr(),mesVO.getFromPersonal(),mesVO.getCharset()));
		//设置邮件收件人数组
		message.setRecipients(Message.RecipientType.TO, createInternetAddressArr(mesVO.getTo(),charset));
		//设置邮件抄送人数组
		Map<String,String> cc = mesVO.getCc();
		if(cc != null && cc.size() > 0) {
			message.setRecipients(Message.RecipientType.CC, createInternetAddressArr(cc,charset));
		}
		//设置邮件抄送人数组
		Map<String,String> bcc = mesVO.getBcc();
		if(bcc != null && bcc.size() > 0) {
			message.setRecipients(Message.RecipientType.BCC, createInternetAddressArr(bcc,charset));
		}
		//设置发送时间
		message.setSentDate(new Date());
		//设置标题。注意字符集
		message.setSubject(mesVO.getTitle(),charset);
		
		//学习了解用，以后再完善。
		//设置回复人。通过邮件发送类型判断是否要设置回复人，其实回复人就是收件人，只是可能需要消息头标明，拼接邮件title和内容。
		//message.setReplyTo(addresses);
		//设置优先级（1紧急，3普通，5低）
		//message.setHeader("X-Priority","1");
		//要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
		//message.setHeader("Disposition-Notification-To",mesVO.getFromAddr());
		
		//创建一个MIME类型为mixed（父类型为Multipart，子类型为mixed）的MimeMultipart对象，表示这是一封混合组合类型的邮件
		MimeMultipart mailMixed = new MimeMultipart("mixed");
		
		//设置附件。如果有附件。
		List<MailFileVO> attachs = mesVO.getAttachments();
		if(attachs != null && attachs.size() > 0) {
			for(MailFileVO file : attachs) {
				//将附件bodyPart加入到mixed子类型的multipart对象中
				MimeBodyPart attachBody = new MimeBodyPart();
				mailMixed.addBodyPart(attachBody);
				
				//设置附件邮件体
				String fileName = file.getFileName();
				DataSource ds = new ByteArrayDataSource(file.getData(), "application/octet-stream");
				DataHandler dh = new DataHandler(ds);
				//设置附件名，注意中文乱码
				//这种方式对方服务器有可能不知道是解码成UTF-8还是GBK等。可能乱码
				//attachPart.setFileName(new String(fileName.getBytes("UTF-8"),"ISO-8859-1"));
				//用sun java mail的组件类。
				attachBody.setFileName(MimeUtility.encodeText(fileName, charset, null));
				attachBody.setDataHandler(dh);
			}
		}
		
		//设置邮件正文
		MimeBodyPart contentBody = new MimeBodyPart();
		//将正文bodyPart加入到mixed子类型的multipart对象中
		mailMixed.addBodyPart(contentBody);
		
		//邮件正文是个组合体。
		//创建一个MIME类型为related（父类型为Multipart，子类型为related）的MimeMultipart对象，表示这是一封关联类型的邮件
		MimeMultipart contentRelated = new MimeMultipart("related");
		//邮件正文组合体设置为正文内容
		contentBody.setContent(contentRelated);
		
		//设置文本
		//创建一个表示正文的MimeBodyPart对象，并加入到MimeMultipart类型的contentRelated对象中。
		//可能会有html超文本和纯文本多段分段。多段没意义。但有些邮箱不支持html，所以最多一段超文本一段纯文本，系统自己选择，兼容考虑。
		MimeBodyPart charactersBody = new MimeBodyPart();
		
		//加入到MimeMultipart类型的contentRelated对象中。
		contentRelated.addBodyPart(charactersBody);
		
		//文本内容body再分段，html文本，纯文本。此处没有纯文本，只是举例，需要纯文本则在charactersAlternative加入纯文本body。
		//创建一个MIME为alternative（父类型Multipart，子类型alternative）的MimeMultipart对象，表示这是一封兼容选择类型的邮件。
		MimeMultipart charactersAlternative = new MimeMultipart("alternative");
		//将MimeMultipart类型的charactersAlternative对象设置为charactersBody的内容。
		charactersBody.setContent(charactersAlternative);
		
		//html文本设置。
		MimeBodyPart htmlbody = new MimeBodyPart();
		//html内嵌img，需要在html文本中这样写法，<img src="cid:sss">,sss代表cid,在内嵌图片中有设置
		htmlbody.setContent(mesVO.getContent(), "text/html;charset=" + charset);
		
		//body添加进part中
		charactersAlternative.addBodyPart(htmlbody);
		
		//设置内嵌图片
		//如果有内嵌图片。
		List<MailFileVO> innerImgs = mesVO.getInnerFiles();
		if(innerImgs != null && innerImgs.size() > 0) {
			//批量设置内嵌图片邮件体
			for(MailFileVO file :innerImgs) {
				//创建一个表示图片的MimeBodyPart对象，并加入到MimeMultipart类型的contentRelated对象中。
				MimeBodyPart InnerImgBody = new MimeBodyPart();
				contentRelated.addBodyPart(InnerImgBody);
				
				//设置内嵌图片邮件体
				String fileName = file.getFileName();
				String imgType = fileName.substring(fileName.lastIndexOf(".") + 1);
				//这里的content-type = image/imgType可能会有问题，简单实现，需要对图片类型表去设置。
				DataSource ds = new ByteArrayDataSource(file.getData(), "image/" + imgType);
				DataHandler dh = new DataHandler(ds);
				InnerImgBody.setDataHandler(dh);
				//cid使用name代替，name需要是唯一值
				InnerImgBody.setContentID(file.getName());
			}
		}
		
		//将MimeMultipart类型的mailMixed对象设置为整个邮件的内容
		message.setContent(mailMixed);
		//保存并生成最终邮件内容。
		message.saveChanges();
		
		return message;
	}
	
	public static MimeMessage createMail(Session session,MailMessageVO mesVO) 
			throws UnsupportedEncodingException, MessagingException {

		String charset = mesVO.getCharset();
		
		//创建邮件对象
		MimeMessage message = new MimeMessage(session);
		//设置邮件发件人
		message.setFrom(new InternetAddress(mesVO.getFromAddr(),mesVO.getFromPersonal(),mesVO.getCharset()));
		//设置邮件收件人数组
		message.setRecipients(Message.RecipientType.TO, createInternetAddressArr(mesVO.getTo(),charset));
		//设置邮件抄送人数组
		Map<String,String> cc = mesVO.getCc();
		if(cc != null && cc.size() > 0) {
			message.setRecipients(Message.RecipientType.CC, createInternetAddressArr(cc,charset));
		}
		//设置邮件抄送人数组
		Map<String,String> bcc = mesVO.getBcc();
		if(bcc != null && bcc.size() > 0) {
			message.setRecipients(Message.RecipientType.BCC, createInternetAddressArr(bcc,charset));
		}
		//设置发送时间
		message.setSentDate(new Date());
		//设置标题。注意字符集
		message.setSubject(mesVO.getTitle(),charset);
		
		//学习了解用，以后再完善。
		//设置回复人。通过邮件发送类型判断是否要设置回复人，其实回复人就是收件人，只是可能需要消息头标明，拼接邮件title和内容。
		//message.setReplyTo(addresses);
		//设置优先级（1紧急，3普通，5低）
		//message.setHeader("X-Priority","1");
		//要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
		//message.setHeader("Disposition-Notification-To",mesVO.getFromAddr());
		
		//生成附件。
		MimeMultipart mixed = createMultipartSetAttach(mesVO.getAttachments(),charset);
		//生成内嵌图片。
		MimeMultipart related = createMultipartSetInner(mesVO.getInnerFiles(),charset);
		//生成兼容文本。
		MimeMultipart alternative = createAlternativeMultipart(mesVO.getContent(),mesVO.getText(),charset);
		//如果兼容文本MimeMultipart对象是null，判断是否是单种文本。
		//单种文本信息可以不生成兼容MimeMultipart对象。
		//单独处理单种文本设置，主要是为了传输报文少点嵌套报文数据信息，但是代码处理就多了一些。
		boolean textOneKind = false;
		String content = mesVO.getContent();
		String text = mesVO.getText();
		if((content == null || text == null) && (content != null || text != null)) {
			textOneKind = true;
		}
		
		//判断最外层MimeMultipart对象，用于Message设置邮件复杂内容。
		//如果只有html或者text文本，就用不到，直接设置message内容。
		//mixed类型包含related和alternative，related包含alternative。
		MimeMultipart mp = null;
		
		if(mixed != null) {
			mp = mixed;
		}else if(related != null){
			mp = related;
		}else {
			mp = alternative;
		}
		//如果message没有MimeMultipart对象，不是多部件邮件。
		//则判断是否是单种文本类型邮件，并直接在message对象中做相应设置。返回message。
		if(mp == null) {
			//是单种文本邮件。
			if(textOneKind) {
				if(content != null) {
					message.setContent(content, "text/html;charset=" + charset);
				}else {
					message.setText(text,charset);
				}
				message.saveChanges();
				return message;
			}else {
				//不是单种文本邮件，也不是多部件邮件，代表邮件没有内容，sun java mail设置空null会报错。返回null。
				return null;
			}
		}
		
		//邮件是多部件内容的邮件
		
		//因为包含关系，如果报文体有子报文体需要添加子报文体。设置它们之间的包含关系。
		MimeMultipart parentCache = mixed;
		if(related != null) {
			if(parentCache != null) {
				//设置子报文体
				MimeBodyPart body = new MimeBodyPart();
				body.setContent(related);
				parentCache.addBodyPart(body);
			}
			//父报文对象后移。
			parentCache = related;
		}
		if(alternative != null) {
			if(parentCache != null) {
				//设置子报文体
				MimeBodyPart body = new MimeBodyPart();
				body.setContent(alternative);
				parentCache.addBodyPart(body);
			}
			//父报文对象后移。包含关系设置结束，不用后移了
			//parentCache = alternative;
		}
		
		//判断是否是单种文本邮件。
		//如果是，由于是多部件邮件，生成MimeBodyPart对象，设置到最近的不为null的父层MimeMultipart对象中
		if(textOneKind) {
			MimeBodyPart textOneKindBodyPart = new MimeBodyPart();
			parentCache.addBodyPart(textOneKindBodyPart );
			if(content != null) {
				textOneKindBodyPart.setContent(content, "text/html;charset=" + charset);
			}else {
				textOneKindBodyPart.setText(text,charset);
			}
		}
		
		//message设置内容。最外层MimeMultipart对象
		message.setContent(mp);
		//保存邮件message修改
		message.saveChanges();
		
		return message;
		
	}
	
	/**
	 * 生成消息地址数组。
	 * 收件人、抄送人、密送人的地址数组。
	 * @param addrMap 地址-昵称键值对集合
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static InternetAddress[] createInternetAddressArr(Map<String,String> addrMap,String charset) 
			throws UnsupportedEncodingException {
		InternetAddress[] addrArr = new InternetAddress[addrMap.size()];
		int i = 0;
		for(Entry<String,String> entry : addrMap.entrySet()) {
			addrArr[i] = new InternetAddress(entry.getKey(),entry.getValue(),charset);
			++i;
		}
		return addrArr;
	}
	
	/**
	 * 创建混合类型的MimeMultipart对象，并且设置附件body。
	 * MIME类型为mixed（父类型为Multipart，子类型为mixed）的MimeMultipart对象，表示这是一封混合组合类型的邮件。
	 * @param attachs
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public static MimeMultipart createMultipartSetAttach(List<MailFileVO> attachs,String charset) 
			throws UnsupportedEncodingException, MessagingException {
		if(attachs == null || attachs.size() == 0) {
			return null;
		}
		//创建一个MIME类型为mixed（父类型为Multipart，子类型为mixed）的MimeMultipart对象，表示这是一封混合组合类型的邮件
		MimeMultipart mixed = new MimeMultipart("mixed");
		for(MailFileVO file : attachs) {
			//创建附件body
			MimeBodyPart body = createAttachmentBodyPart(file.getData(),file.getFileName(),charset);
			//附件body添加到mixed类型的multipart中
			mixed.addBodyPart(body);
		}
		return mixed;
	}
	
	/**
	 * 创建关联类型的MimeMultipart对象，并且设置内嵌文件body。
	 * MIME类型为related（父类型为Multipart，子类型为related）的MimeMultipart对象，表示这是一封内嵌关联类型的邮件。
	 * @param innerFiles
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public static MimeMultipart createMultipartSetInner(List<MailFileVO> innerFiles,String charset) 
			throws UnsupportedEncodingException, MessagingException {
		if(innerFiles == null || innerFiles.size() == 0) {
			return null;
		}
		//创建一个MIME类型为related（父类型为Multipart，子类型为related）的MimeMultipart对象
		MimeMultipart related = new MimeMultipart("related");
		for(MailFileVO file : innerFiles) {
			//创建内嵌body
			MimeBodyPart body = createInnerFileBodyPart(file.getData(),file.getFileName(),file.getName(),charset);
			//内嵌body添加到related类型的multipart中
			related.addBodyPart(body);
		}
		return related;
	}
	
	/**
	 * 生成可替换类型的Multipart对象。
	 * 必须要有html文本和纯文本都存在才生成。
	 * html文本和纯文本每种类型一段，为了兼容有些邮件系统不支持html文本。
	 * 每种类型分多段没有意义。
	 * @param content
	 * @param text
	 * @param charset
	 * @return
	 * @throws MessagingException
	 */
	public static MimeMultipart createAlternativeMultipart(String content,String text,String charset) 
			throws MessagingException {
		//如果没有文本，或者只有一种文本，返回null。
		//没必要生成alternative类型的MimeMultipart对象。
		//要么生成MimeBodyPart添加进父multipart中，要么直接设置就在message中设置content或text发送简单邮件。
		if(content == null || text == null) {
			return null;
		}
		MimeMultipart multipart = new MimeMultipart("alternative");
		if(content != null) {
			MimeBodyPart contentbody = createHtmlBodyPart(content,charset);
			multipart.addBodyPart(contentbody);
		}
		if(text != null) {
			MimeBodyPart textbody = createTextBodyPart(text,charset);
			multipart.addBodyPart(textbody);
		}
		return multipart;
	}
	
	/**
	 * 生成纯文本BodyPart对象
	 * @param text 纯文本
	 * @param charset
	 * @return
	 * @throws MessagingException
	 */
	public static MimeBodyPart createTextBodyPart(String text,String charset) throws MessagingException {
		//创建MimeBodyPart对象。
		MimeBodyPart body = new MimeBodyPart();
		//设置内容。
		body.setText(text, charset);
		return body;
	}
	
	/**
	 * 生成html文本（html文本也能是不包含标签的纯文本，不是标准html）BodyPart对象。
	 * @param html html文本
	 * @param charset
	 * @return
	 * @throws MessagingException
	 */
	public static MimeBodyPart createHtmlBodyPart(String content,String charset) throws MessagingException {
		//创建MimeBodyPart对象。
		MimeBodyPart body = new MimeBodyPart();
		//设置内容。
		body.setContent(content, "text/html;charset=" + charset);
		return body;
	}
	
	/**
	 * 生成内嵌文件BodyPart对象。
	 * 一般是图片。
	 * @param data 文件字节数据
	 * @param filename 文件名，带后缀名的文件名，用来判断内容类型
	 * @param cid 内容id，唯一标识，自定义使用实现cid（我暂时用文件业务name属性代替）。
	 * @param charset
	 * @return
	 * @throws MessagingException
	 */
	public static MimeBodyPart createInnerFileBodyPart(byte[] data,String filename,String cid,String charset) 
			throws MessagingException {
		//创建MimeBodyPart对象。
		MimeBodyPart body = new MimeBodyPart();
		//设置内容。
		//内嵌文件内容类型。
		String contentType = filename.substring(filename.lastIndexOf(".") + 1);
		//这里的content-type = image/contentType可能会有问题，简单实现，需要对图片类型表去设置。
		DataSource ds = new ByteArrayDataSource(data, "image/" + contentType);
		DataHandler dh = new DataHandler(ds);
		body.setDataHandler(dh);
		//cid是唯一的，内容body可以通过cid内嵌关联文件，一般是图片。
		body.setContentID(cid);
		return body;
	}
	
	/**
	 * 生成附件BodyPart对象。
	 * @param data 文件字节数据
	 * @param filename 文件名，带后缀名的文件名。内容类型，统一用二进制数据流。
	 * 判断附件是什么类型文件，可能是通过内容类型判断文件，也可能只是通过文件后缀名判断。取决于邮件系统具体业务实现。
	 * @param charset
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static MimeBodyPart createAttachmentBodyPart(byte[] data,String filename,String charset) 
			throws MessagingException, UnsupportedEncodingException {
		//创建MimeBodyPart对象。
		MimeBodyPart body = new MimeBodyPart();
		
		//设置内容。
		
		//由于是预先准备好了bytes数据，在发送邮件前内存存储的附件太大的话，会内存空间不足。
		//如果是在传输报文时，分段再读取文件传输文件，对内存空间大小要求就不高，但是速度相对慢些。
		//不知道底层FileDataSource是怎么实现的，是传输时才读取文件进行分段缓冲读取与输出，还是也是预先全部读好全部输出
		DataSource ds = new ByteArrayDataSource(data, "application/octet-stream");
		DataHandler dh = new DataHandler(ds);
		body.setDataHandler(dh);
		//设置附件名，注意中文乱码
		//这种方式对方服务器有可能不知道是解码成UTF-8还是GBK等。可能乱码
		//attachPart.setFileName(new String(fileName.getBytes("UTF-8"),"ISO-8859-1"));
		//用sun java mail的组件类编码中文。
		body.setFileName(MimeUtility.encodeText(filename, charset, null));
		return body;
	}
	
	/**
	 * 将邮件内容生成文件。
	 * <br>
	 * 参考：
	 * https://blog.csdn.net/xyang81/article/details/7675152
	 * @param path 文件路径
	 * @param message 邮件消息对象
	 * @throws IOException
	 * @throws MessagingException
	 */
	public static void writeTo(String path,MimeMessage message) throws IOException, MessagingException {
		OutputStream out = new FileOutputStream(new File(path));
		message.writeTo(out);
	}
	
}

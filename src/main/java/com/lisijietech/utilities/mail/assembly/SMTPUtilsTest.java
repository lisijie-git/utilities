package com.lisijietech.utilities.mail.assembly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import com.lisijietech.utilities.mail.constant.MailAddressConstant;
import com.lisijietech.utilities.mail.vo.MailFileVO;
import com.lisijietech.utilities.mail.vo.MailMessageVO;

/**
 * SMTPUtils测试
 * @author lisijie
 * @date 2020-9-5
 */
public class SMTPUtilsTest {
//	腾讯会校验发件人地址，与SMTP认证用户不一样就终止邮件交互了。
//	从阿里云服务器，并且域名配置了mx、txt没用，发送到qq.com、mx1(mx2和mx3).qq.com无响应，发送到smtp.qq.com需要认证。
//	邮件服务器间发送邮件可能有什么利益关系需要认证。或者邮件服务器间的发送地址和姿势不对。反垃圾和病毒绝对不是主要原因。
//	public static String FROM = "message@lisijietech.com";
	public static String FROM = "fff@qq.com";
	public static String FROM_PERSONAL = "铁头娃";
	//收件人不存在，qq邮箱会返回错误码550和信息。
	public static String TO = "ttt@qq.com";
	public static String TO_PERSONAL = "愣头青";
	public static String CC = "ccc@qq.com";
	public static String CC_PERSONAL = null;
	public static String TITLE = "邮件测试";
	public static String CONTENT = "<div>一段简单html文本片段</div><a href=\"http://www.lisijietech.com\" />李斯杰个人网站</a>";
	public static String INNER_CONTENT ="<div>内嵌图片html<img src=\"cid:img1.jpg\" /><img src=\"cid:img2.jpg\" /></div>";
	public static String TEXT = "这是一段纯文本。";
	public static String CHARSET = "UTF-8";
	
	
	public static void main(String[] args) {
		if(args == null || args.length == 0) {
			System.out.println("没有输入测试类型");
			return;
		}
		//测试方法选择
		String choose = args[0];
		//用户
		String user = args[1];
		//认证码
		String authCode = args[2];
		
		//生成邮件session。需要打印debug信息。
		Session session  = SMTPUtils.getSession(MailAddressConstant.MAIL_QQ_SMTP, null, "true");
		
		//生成邮件message
		
		//获取文件数据，生成邮件文件对象。
		File f1 = new File("C:/Users/ASUS/Desktop/javac/class/resource/img1.jpg");
		File f2 = new File("C:/Users/ASUS/Desktop/javac/class/resource/img2.jpg");
//		File f1 = new File("resource/img1.jpg");
//		File f2 = new File("resource/img2.jpg");
		long f1Len = f1.length();
		long f2Len = f2.length();
		
		if(f1Len > Integer.MAX_VALUE || f2Len > Integer.MAX_VALUE) {
			System.out.println("文件太大，超过1TB大小");
			return;
		}
		byte[] b1 = new byte[(int) f1Len];
		byte[] b2 = new byte[(int) f2Len];
		FileInputStream input1 = null;
		FileInputStream input2 = null;
		try {
			input1 = new FileInputStream(f1);
			//参考https://www.cnblogs.com/firstdream/p/7809404.html，分段读到同一个字节数组中。
			//我知道文件大小，直接一次全部读完。
			input1.read(b1);
			input2 = new FileInputStream(f2);
			input2.read(b2);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}finally {
			try {
				if(input1 != null) {
					input1.close();
				}
				if(input2 != null) {
					input2.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		MailFileVO file1 = new MailFileVO();
		file1.setName("img1.jpg");
		file1.setFileName(f1.getName());
		file1.setData(b1);
		
		MailFileVO file2 = new MailFileVO();
		file2.setName("img2.jpg");
		file2.setFileName(f2.getName());
		file2.setData(b2);
		
		//附件和内联图片共用这个list。
		List<MailFileVO> list = new ArrayList<>();
		list.add(file1);
		list.add(file2);
		
		//设置message数据对象
		MailMessageVO mesVO = new MailMessageVO();
		mesVO.setFromAddr(FROM);
		mesVO.setFromPersonal(FROM_PERSONAL);
		Map<String,String> to = new LinkedHashMap<>();
		to.put(TO, TO_PERSONAL);
		mesVO.setTo(to);
		Map<String,String> cc = new LinkedHashMap<>();
		cc.put(CC, CC_PERSONAL);
		mesVO.setCc(cc);
		mesVO.setTitle(TITLE);
		mesVO.setContent(CONTENT);
		mesVO.setText(TEXT);
		mesVO.setInnerFiles(list);
		mesVO.setAttachments(list);
		mesVO.setCharset(CHARSET);
		try {
			//纯文本邮件
			if(choose.equals("text")) {
				MimeMessage text = SMTPUtils.createTextMail(
						session,FROM,FROM_PERSONAL,TO,TO_PERSONAL,TITLE,TEXT,CHARSET);
//				try {
//					SMTPUtils.writeTo("./resource/email.eml", text);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				SMTPUtils.send(session, text, user, authCode);
				return;
			}
			//简单html文本邮件。不设置昵称
			if(choose.equals("html")) {
				MimeMessage html = SMTPUtils.createSimpleHtmlMail(session,FROM,null,TO,null,TITLE,CONTENT,CHARSET);
				try {
					SMTPUtils.writeTo("./resource/email.eml", html);
				} catch (IOException e) {
					e.printStackTrace();
				}
				SMTPUtils.send(session, html, user, authCode);
				return;
			}
			
			//内联图片可替换文本但只有html文本的邮件
			if(choose.equals("inner")) {
				mesVO.setContent(INNER_CONTENT);
				MimeMessage inner = SMTPUtils.createInnerImageHtmlMail(session, mesVO);
				try {
					SMTPUtils.writeTo("./resource/email.eml", inner);
				} catch (IOException e) {
					e.printStackTrace();
				}
				SMTPUtils.send(session, inner, user, authCode);
				return;
			}
			
			//混合邮件，包含附件、内联文件、可替换文本（同时存在html和纯文本，邮件系统二选一兼容）。
			if(choose.equals("mixed")) {
				mesVO.setContent(INNER_CONTENT);
				MimeMessage mixed = SMTPUtils.createMultipleMail(session, mesVO);
				try {
					SMTPUtils.writeTo("./resource/email.eml", mixed);
				} catch (IOException e) {
					e.printStackTrace();
				}
				SMTPUtils.send(session, mixed, user, authCode);
				return;
			}
			
			
			//邮件。自动判断邮件类型。
			//只有纯文本
			if(choose.equals("mail-text")) {
				mesVO.setContent(null);
				mesVO.setInnerFiles(null);
				mesVO.setAttachments(null);
			}
			//只有html
			if(choose.equals("mail-html")) {
				mesVO.setText(null);
				mesVO.setInnerFiles(null);
				mesVO.setAttachments(null);
				
			}
			//只有可替换类型alternative
			if(choose.equals("mail-alternative")) {
				mesVO.setInnerFiles(null);
				mesVO.setAttachments(null);
			}
			//只有内联图片
			if(choose.equals("mail-inner")) {
				mesVO.setText(null);
				mesVO.setContent(null);
				mesVO.setAttachments(null);
			}
			//只有内联图片和text
			if(choose.equals("mail-inner-text")) {
				mesVO.setContent(null);
				mesVO.setAttachments(null);
			}
			//只有内联图片和html。
			if(choose.equals("mail-inner-html")) {
				mesVO.setContent(INNER_CONTENT);
				mesVO.setText(null);
				mesVO.setAttachments(null);
			}
			//只有内联和可替换
			if(choose.equals("mail-inner-alternative")) {
				mesVO.setContent(INNER_CONTENT);
				mesVO.setAttachments(null);
			}
			//只有附件
			if(choose.equals("mail-attachment")) {
				mesVO.setContent(null);
				mesVO.setText(null);
				mesVO.setInnerFiles(null);
			}
			//只有附件和text
			if(choose.equals("mail-attachment-text")) {
				mesVO.setContent(null);
				mesVO.setInnerFiles(null);
			}
			//只有附件和html
			if(choose.equals("mail-attachment-html")) {
				mesVO.setText(null);
				mesVO.setInnerFiles(null);
			}
			//只有附件和可替换
			if(choose.equals("mail-attachment-alternative")) {
				mesVO.setInnerFiles(null);
			}
			//只有附件和内联图片
			if(choose.equals("mail-attachment-inner")) {
				mesVO.setContent(null);
				mesVO.setText(null);
			}
			//完全包括的完整邮件
			if(choose.equals("mail-all")) {
				mesVO.setContent(INNER_CONTENT);
			}
			
			MimeMessage mail = SMTPUtils.createMail(session, mesVO);
//			try {
//				SMTPUtils.writeTo("./resource/email.eml", mail);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			SMTPUtils.send(session, mail, user, authCode);
			
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}

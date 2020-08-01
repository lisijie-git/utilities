package com.lisijietech.utilities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * MD5摘要算法
 * @author lisijie
 *
 */
public class MD5Utils {
	//字符编码
	private static final String CHARSET = "UTF-8";
	
	/**
	 * MD5加密
	 * @param str
	 * @return 返回byte数组
	 */
	public static byte[] encrypt2Bytes(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
//			md5.reset();
//			md5.update(str.getBytes());
			return md5.digest(str.getBytes(CHARSET));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * MD5加密转换为32位16进制字母大写字符串。
	 * 至于byte & 0xff的位操作原因，
	 * java系统检测到byte类型数据可能会转化为int，或者byte与int运算，会把byte高位补位24位，转型成int，正数补0，负数补1，
	 * 0xff是int类型的字面量，byte & 0xFF 运算其实质是保持二进制数据的一致性，高位补0低位都不变，虽然输出的10进制数字表示改变了
	 * https://www.cnblogs.com/originate918/p/6378005.html
	 * https://www.jianshu.com/p/524dfe838cf1
	 * @param str
	 * @return
	 */
	public static String encrypt2HexStr(String str) {
		byte[] b = encrypt2Bytes(str);
		StringBuffer sb = new StringBuffer();
		String hexStr = null;
		for(int i = 0;i < b.length;i++) {
			//此处b[i] & 0xff是32bit正整数，低8位保持数据，高位全是0补位，保证二进制数据不变（一致），只是高位用0补位。
			int data = b[i] & 0xff;
			//由于data是由8bit长度的b[i]补位转换过来的，所以整数值data < 256 ，
			//所以十六进制hexStr字符串最大为FF,不可能出现FFF等两个以上长度的字符串，即hexStr.length()一定<2
			hexStr = Integer.toHexString(data);
			//hexStr只有一个字符用0字符补齐
			if(hexStr.length() == 1) {
				sb.append('0');
			}
			sb.append(hexStr);
//			for循环里的上面这几句代码效果等同于下面这一句， | 0x100的意思是让b[i]不会小于0x100，再截取字符串不要高位的1，达到补0效果
//			sb.append(Integer.toHexString((b[i] & 0xFF) | 0x100).substring(1,3));
		}
		return sb.toString().toUpperCase();
	}
	
	/**
	 * MD5加盐加密。
	 * 链接里的加密实现，随机生成16位盐，原密码+盐字符串拼接，然后MD5加密得到md5str，再把盐按自定义算法插到md5str中得到saltmd5。
	 * 验证。按照自定义算法，先得到saltmd5的盐值，然进行 客户端传回密码+盐 字符串拼接，MD5加密，与剔除盐值的saltmd5(md5str)对比。
	 * 个人理解：把盐值与原密码拼接，增强密码，然后MD5加密，再把盐值按照一定方法插到了MD5str中，好处是不用管理salt，可以从saltmd5中取得。
	 * 实现 https://blog.csdn.net/qq_39135287/article/details/82012441
	 * 加盐加密的意义 https://www.cnblogs.com/birdsmaller/p/5377104.html
	 * @param str
	 * @return
	 */
	public static String getSaltMD5(String str) {
		//随机生成16位字符salt
		Random rand = new Random();
		StringBuilder sBuilder = new StringBuilder(16);
		//这里应该是运行效率问题还是什么，不然直接for循环添加出16位随机数字符，也不用补全0
		sBuilder.append(rand.nextInt(99999999)).append(rand.nextInt(99999999));
		int len = sBuilder.length();
		if(len < 16) {
			for(int i = 0;i < 16 - len;i++) {
				sBuilder.append("0");
			}
		}
		String salt = sBuilder.toString();
		
		//加入盐。方式是password + salt 字符串，再MD5加密成md5str，然后再在md5str插入salt，规则是:md5str+盐+md5str
		String md5Str = encrypt2HexStr(str + salt);
		//在md5str中再次加入salt
		char[] cs = new char[48];
		for(int i = 0;i < 48;i += 3) {
			cs[i] = md5Str.charAt(i / 3 * 2);
			cs[i + 1] = salt.charAt(i / 3);
			cs[i + 2] = md5Str.charAt(i / 3 * 2 + 1);
		}
		//最终加盐加密结果
		return String.valueOf(cs);
	}
	
	/**
	 * 验证。按照自定义算法，先得到saltmd5的盐值，然进行 客户端传回密码+盐 字符串拼接，MD5加密，与剔除盐值的saltmd5(md5str)对比。
	 * @param password
	 * @param saltMD5
	 * @return
	 */
	public static boolean verifySaltMD5(String password,String saltMD5) {
		char[] cMD5 = new char[32];
		char[] cSalt = new char[16];
		for(int i = 0;i < 48;i += 3) {
			cMD5[i / 3 * 2] = saltMD5.charAt(i);
			cMD5[i / 3 * 2 + 1] = saltMD5.charAt(i+2);
			cSalt[i / 3] = saltMD5.charAt(i+1);
		}
		String md5Str = String.valueOf(cMD5);
		String salt = String.valueOf(cSalt);
		return encrypt2HexStr(password+salt).equals(md5Str);
	}
	
//	public static void main(String[] args){
		//https://www.cnblogs.com/originate918/p/6378005.html
		//https://www.jianshu.com/p/524dfe838cf1
		//java系统检测到byte类型数据可能会转化为int，或者byte与int运算，会把byte高位补位24位，转型成int，正数补0，负数补1，
		//0xff是int类型的字面量，byte & 0xFF 运算其实质是保持二进制数据的一致性，高位补0低位都不变，虽然输出的10进制数字表示改变了
//		byte b = -2;
//		System.out.println((b & 0xFF));
//		System.out.println((b & 0xFF) | 0x100);
//		System.out.println(0x100);
		
//		测试base64编码解码
//		String s = "acb";
//		String enStr = base64Encode(s.getBytes());
//		System.out.println(enStr);
//		System.out.println(new String(base64Decode(enStr)));
		
//		测试MD5加密
//		String s = "abcda啊啊啊是是是";
//		byte[] b = encrypt2Bytes(s);
//		for(int i = 0;i<b.length;i++) {
//			System.out.print(b[i]+" ");
//			if(i == b.length - 1) {System.out.println();}
//		}
//		System.out.println(Base64Util.base64Encode(s.getBytes()));
//		System.out.println(encrypt2HexStr(s));
//		System.out.println(encrypt2HexStr(s).length());
		
//		测试saltMD5加密与验证
//		String s="abcd1234加盐加密";
//		String saltMD5 = getSaltMD5(s);
//		System.out.println("saltMD5："+saltMD5 +"  长度："+saltMD5.length());
//		boolean result = verifySaltMD5(s,saltMD5);
//		System.out.println("验证结果："+result);
//		
//	}
	
}

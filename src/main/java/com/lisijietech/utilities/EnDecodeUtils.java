package com.lisijietech.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * 各种基础的编码解码工具类
 * @author lisijie
 *
 */
public class EnDecodeUtils {
	/**
	 * url字符串编码
	 * @param s 字符串
	 * @param charset 字符集
	 * @return
	 */
	public static String urlEncode(String s,String charset) {
		String result = "";
		try {
			result = URLEncoder.encode(s,charset);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
		return result;
	}
	
	/**
	 * url字符串解码
	 * @param s 字符串
	 * @param charset 字符集
	 * @return
	 */
	public static String urlDecode(String s,String charset) {
		String result = "";
		try {
			result = URLDecoder.decode(s,charset);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
		return result;
	}
	
	/**
	 * base64编码
	 * @param b 字节数组最好是UTF-8编码的字节 b = str.getBytes("UTF-8")
	 * @return
	 */
	public static String base64Encode(byte[] b) {
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(b);
	}
	
	/**
	 * base64解码
	 * @param str
	 * @return 返回字节数据转字符串，根据编码方式转。如new String(b,"UTF-8")
	 */
	public static byte[] base64Decode(String str) {
		Decoder dncoder = Base64.getDecoder();
		return dncoder.decode(str);
	}
	
}

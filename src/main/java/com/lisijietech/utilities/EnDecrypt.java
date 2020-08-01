package com.lisijietech.utilities;

/**
 * 加密解密算法
 * @author lisijie
 *
 */
public class EnDecrypt {
	/**
	 * 加密解密算法，执行一次，把字符串加密，把加密字符串在执行一次，还原为原来字符串
	 * https://blog.csdn.net/zhanglu1236789/article/details/80852766
	 * https://blog.csdn.net/qq_34869143/article/details/74010554
	 * 搜MD5加密解密搜出来的，还误以为MD5被破解了。感觉只能后台传输，加密会有乱码。
	 * @param str
	 * @return
	 */
	public static String encryptDecrypt(String str) {
		char[] a = str.toCharArray();
		for(int i = 0;i < a.length;i++) {
			a[i] = (char) (a[i] ^ 't');
		}
		return new String(a);
	}
	
//	public static void main(String[] args){
//		String s = "abcda啊啊啊是是是";
//		String str = null;
//		System.out.println(str = encryptDecrypt(s));
//		System.out.println(str = encryptDecrypt(str));
//	}
	
}

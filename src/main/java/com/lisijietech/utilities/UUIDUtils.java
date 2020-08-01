package com.lisijietech.utilities;

import java.util.Random;
import java.util.UUID;

public class UUIDUtils {
	
	/**
	 * java原生的UUID<br>
	 * 参考：<br>
	 * https://www.cnblogs.com/java-class/p/4727698.html
	 * @return
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
	}
	
	/**
	 * 得到定长随机数字符串，specify指定，规定
	 * @param length
	 * @return
	 */
	public static String getSpecLenNum(Integer length) {
//		double i = Math.pow(10, length);
		if(length == null || length.equals(0)) {
			return "";
		}
		int bound = 1;//边界
		int carryBit = 10;//进位为10
		for(int i = 0;i < length;i++) {
			bound = bound * carryBit;
		}
		String numberStr = String.valueOf(new Random().nextInt(bound) + bound).substring(1);
		return numberStr;
	}
	
//	public static void main(String[] args) {
//		Integer i = 500;
//		Integer i2 = 501;
//		System.out.println(i == i2);//这是个坑
//		String s = null;
//		System.out.println(s.equals(null));
//	}
	
}

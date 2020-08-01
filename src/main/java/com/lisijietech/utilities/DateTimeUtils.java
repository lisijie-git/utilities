package com.lisijietech.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期时间工具类
 * 参考：<br>
 * https://www.cnblogs.com/jinzhiming/p/6256552.html<br>
 * @author lisijie
 *
 */
public class DateTimeUtils {
	
	/**
	 * 获取日期格式对象
	 * @param format
	 * @return
	 */
	public static SimpleDateFormat getDateFormat(String format) {
		return new SimpleDateFormat(format);
	}
	
	/**
	 * 格式化日期为字符串
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date,String format) {
		SimpleDateFormat sdf = getDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * 日期字符串解析为日期对象
	 * @param dateStr
	 * @param format
	 * @return
	 */
	public static Date formatParse(String dateStr,String format) {
		SimpleDateFormat sdf = getDateFormat(format);
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
//	public static void main(String[] args) {
//		System.out.println(formatDate(new Date(),"yyyy-MM-dd HH:mm:ss.SSS"));
//		System.out.println(formatParse("20160909090909009","yyyyMMddHHmm"));
//	}
	
}

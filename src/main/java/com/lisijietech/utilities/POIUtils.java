package com.lisijietech.utilities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Microsoft office格式文档读写工具类
 * @author lisijie
 * @date 2020年5月5日
 */
public class POIUtils {
	
	public static final String XLS = "xls";
	public static final String XLSX = "xlsx";
	
	/**
	 * 生成excel工作簿。
	 * 生成不需要判断生成哪中类型excel，统一用当前最新新版本类型xlsx。
	 * @return
	 */
	public static XSSFWorkbook genericWorkbook() {
		return new XSSFWorkbook();
	}
	
	/**
	 * 读取文档生成excel工作簿。
	 * 读取解析需要判断使用哪种构造方法去兼容xls,xlsx。
	 * @param is
	 * @param filename
	 * @return
	 */
	public static Workbook readToWorkbook(InputStream is,String filename) {
		Workbook wb = null;
		try {
			if(filename.endsWith(XLS)) {
				wb = new HSSFWorkbook(is);
			}else if(filename.endsWith(XLSX)){
				wb = new XSSFWorkbook(is);
			}else {
				//这里文件名不是excel类型，可以记录日志，可以抛出异常，暂时做个多余的null赋值。
				wb = null;
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return wb;
	}

}

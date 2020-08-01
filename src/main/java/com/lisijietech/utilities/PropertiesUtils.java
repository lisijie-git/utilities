package com.lisijietech.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties文件工具类
 * @author lisijie
 * @date 2020年6月4日
 */
public class PropertiesUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);
	
	private static final String CHARSET_UTF8 = "UTF-8";
//	//相对路径目录名字
//	private static final String RELATIVE_DIRECTORY = "config";
	
	/**
	 * 系统文件加载properties文件
	 * @param path 系统文件路径
	 * @param charset 字符集
	 * @return
	 */
	public static Properties fileLoad(String path,String charset) {
		if(charset == null || charset.trim().length() < 1) {
			charset = CHARSET_UTF8;
		}
		Properties properties = null;
		//先从系统文件中获取
		File file = new File(path);
		FileInputStream fis = null;
		InputStreamReader isr = null;
		if(file.exists()) {
			try {
//				FileReader fr = new FileReader(file);
//				InputStreamReader isr = new InputStreamReader(new FileInputStream(path),charset);
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis,charset);
				properties = new Properties();
				properties.load(isr);
			} catch (FileNotFoundException e) {
				logger.debug("读取系统文件{}不存在", path);
			} catch (UnsupportedEncodingException e) {
				logger.debug("读取系统文件{}字符集{}异常", path, charset);
			} catch (IOException e) {
				logger.debug("加载系统文件{}到Properties中IO异常", path);
			} finally {
				if(fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						logger.debug("fis的IO关闭异常");
					}
				}
				if(isr != null) {
					try {
						isr.close();
					} catch (IOException e) {
						logger.debug("isr的IO关闭异常");
					}
				}
			}
		}
		return properties;
	}
	
	/**
	 * 类路径加载propertis文件
	 * @param path 类路径。是从类根路径开始，无论有没有/开头。
	 * @return
	 */
	public static Properties classLoad(String path,String charset) {
		if(charset == null || charset.trim().length() < 1) {
			charset = CHARSET_UTF8;
		}
		Properties properties = null;
		//class.getResourceAsStream的方法是相对于此类的同级目录，要从项目根目录需要在路径开头加/斜杠
		//InputStream in = PropertiesUtils.class.getResourceAsStream("/a");
		//class.getClassLoader().getResourceAsStream方法是从项目类路径根目录开始，不能以/斜杠开头会报空指针异常
		if(path.startsWith("/")) {
			path = path.replaceAll("^/+", "");
			System.out.println(path + "=========================");
		}
		InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream(path);
		InputStreamReader isr = null;
		try {
			//创建字符输入流，设置字符集
			isr = new InputStreamReader(in,charset);
			properties = new Properties();
			properties.load(isr);
		} catch (UnsupportedEncodingException e1) {
			logger.debug("读取系统文件{}字符集{}异常", path, charset);
		} catch (IOException e) {
			logger.debug("properties的load方法IO操作失败");
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.debug("in的IO关闭异常");
				}
			}
			if(isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					logger.debug("isr的IO关闭异常");
				}
			}
		}
		return properties;
	}
	
	/**
	 * 路径加载propertis文件
	 * @param path 文件路径。通过file:和classpath:前缀判断文件加载方式。没有前缀默认为类路径加载方式。
	 * 如果是系统文件方式，绝对路径(file:/,file:///)，相对路径(file:a/b,这种也是相对路径写法file://a/b)，基础目录是用户工作目录。
	 * 如果是类路径方式，无论是绝对(/a/b)还是相对路径(a/b)，都是从类路径根目录加载。
	 * @param charset 字符集
	 * @return
	 */
	public static Properties load(String path,String charset) {
		if(charset == null || charset.trim().length() < 1) {
			charset = CHARSET_UTF8;
		}
		//统一文件路径分割符。windows系统的文件路径会是C:\a\b\c这样，也能是C:/a/b/c
		path = path.replace("\\", "/");
		//判断路径是系统文件路径，还是类路径。
		if(path.startsWith("file:")) {
			//去除file:字符串
			path = path.replaceAll("^file:", "");
			
			//绝对路径模板。file:协议绝对路径是file:///a/b或者file:/a格式。
			String pattern = "^/\\w\\S*|^///\\w\\S*";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(path);
			//判断是绝对路径还是相对路径
			if(m.matches()) {
				//判断是什么系统的绝对路径。
				//file:协议的绝对路径是一个或多个/斜杠开头，获取windows的绝对路径是盘符开头如C:，不需要/斜杠
				if(path.contains(":")) {
					path = path.replaceAll("^/+", "");
				}else {
					path = path.replaceAll("^/+", "/");
				}
			}else {
				//文件是相对路径。
				//相对路径有可能写为file:a/b，file://a/b
				path = path.replaceAll("^//", "");
				//约定系统文件下的配置文件基础目录是当前用户工作目录
				String projectPath = System.getProperty("user.dir");
				path = projectPath + File.separator + path;
			}
			
			return fileLoad(path,charset);
		}else {
			//类路径
			//如果有，去除类路径协议前缀
			if(path.startsWith("classpath:")) {
				path = path.replace("classpath:", "");
			}
			return classLoad(path,charset);
		}
	}
	
	public static void main(String[] args) {
		Properties p = load("code-config.properties",null);
		System.out.println(p.get("blank") ==  null);
		System.out.println(p.get("blank").getClass());
		System.out.println(p.get("blank").equals(""));
		System.out.println(p.get("blank").toString());
		System.out.println(p.get("nonono") == null);
		System.out.println(p.get("is-null") == null);
		System.out.println(p.get("is-null"));
//		
//		Properties p = classLoad("/application.properties",null);
//		System.out.println(PropertiesUtils.class.getClassLoader().getResource("application.properties"));
//		System.out.println(p.toString());
//		System.out.println(p.getProperty("spring.datasource.username"));
//		
//		String projectPath = System.getProperty("user.dir");
//		System.out.println(projectPath);
//		Properties p = fileLoad(projectPath + "\\src\\main\\resources\\application.properties",null);
////		Properties p = fileLoad("D:/ProgramData/sts-eclipse-workspace/generator-service/target/classes/application.properties");
//		System.out.println(p.getProperty("spring.datasource.username"));
		
//		String projectPath = System.getProperty("user.dir");
//		System.out.println(projectPath);
//		String filePath1 = "file:///"+ projectPath +"/src/main/resources/application.properties";
//		String filePath2 = "file://application.properties";
//		Properties p1 = load(filePath1,null);
//		Properties p2 = load(filePath2,null);
//		System.out.println(p1.getProperty("spring.datasource.username"));
//		System.out.println(p2.getProperty("spring.datasource.username"));
	}
	
}

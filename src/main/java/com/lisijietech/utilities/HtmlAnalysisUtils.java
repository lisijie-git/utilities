package com.lisijietech.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * html文本解析工具
 * @author lisijie
 *
 */
public class HtmlAnalysisUtils {
	
	/**
	 * 匹配元素列表，并获取他们的src值
	 * 参考:
	 * https://www.cnblogs.com/ly-520/p/10203307.html
	 * @param htmlStr
	 * @param name
	 * @return
	 */
	public static List<String> getElementSrc(String htmlStr,String elementName){
		List<String> list = new ArrayList<>();
		String elementStr = "";
		Pattern pElement;
		Matcher mElement;
		String regElement = "<"+elementName+".*src\\s*=\\s*(.*?)[^>]*?>";
		String regSrc =  "src\\s*=\\s*[\\\"\\\'](.*?)[\\\"\\\']";
		pElement = Pattern.compile(regElement, Pattern.CASE_INSENSITIVE);
		mElement = pElement.matcher(htmlStr);
		while(mElement.find()) {
			//得到整个正则表达式匹配到的字符串，即<img />,或者<video />
			elementStr = mElement.group();
			//匹配<img />或者<video />中的src数据
			//这里的正则表达式做了单双引号的修改
//			Matcher mSrc = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(elementStr);
			Matcher mSrc = Pattern.compile(regSrc).matcher(elementStr);
			while(mSrc.find()) {
				//获得第一个分组匹配的字符，即src的值
				list.add(mSrc.group(1));
			}
		}
		return list;
	}
	
//	public static void main(String[] args) {
//		String elementStr = "<img src = 'a.jpg' id='id' />  <img src = 'b.jpg' /> <img src = \"c.jpg\" />";
//		System.out.println(getElmentSrc(elementStr,"img"));
//	}
	
	/**
	 * 获取富文本的文本
	 * 参考：
	 * https://zhidao.baidu.com/question/750584512934695292.html
	 * @param htmlStr
	 * @return
	 */
	public static String getText(String htmlStr) {
		String text;
		//去除元素标签。参考中，测试残缺标签有bug，所以进行了修改优化
		text = htmlStr.replaceAll("<[^<>]*?>", "");
		//去除空白字符，包括空格，换行等
		text = text.replaceAll("\\s*", "");
		//去掉残缺标签。不应当出现残缺标签，因为不好处理哪些是正文哪些是残缺标签的属性或名字
		text = text.replaceAll("[<>]*", "");
		return text;
	}
	
//	public static void main(String[] args) {
//		String elementStr = "<img src = 'a.jpg' id='id' /> 测试 < 残缺 <img src = 'b.jpg' /><div>div里的</div>残缺> ";
//		System.out.println(getText(elementStr));
//	}
	
}

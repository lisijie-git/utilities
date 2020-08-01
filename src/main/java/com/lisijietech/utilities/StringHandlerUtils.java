package com.lisijietech.utilities;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 * @author lisijie
 * @date 2020年4月24日
 */
public class StringHandlerUtils {
	/**
	 * 字符串内部接口。
	 * 函数式接口，有且仅有一个抽象方法。用来给lambda表达式用。语言简洁。
	 * 当然也可以匿名内部类实现。
	 * 自定义要实现的方法是什么。
	 * @author lisijie
	 * @date 2020年6月11日
	 */
	interface StringInner {
		/**
		 * 字符串处理抽象方法
		 * @param str
		 * @return
		 */
		String stringHander(String str);
		
		/**
		 * java8特性
		 * @return
		 */
		default String test1() {return null;}
		/**
		 * java8特性
		 * @return
		 */
		static String test2() {return null;}
	}
	
	/**
	 * 判断字符串是否空
	 * @param str
	 * @return
	 */
	public static boolean isBlank(final CharSequence cs) {
//		简单写法
//		return (str == null || str.trim().length() == 0);
		if(cs == null) {
			return true;
		}
		int l = cs.length();
		if(l > 0) {
			for(int i = 0;i < l;i++) {
				if(!Character.isWhitespace(cs.charAt(i))) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 截取是以某个字符串第几次开头和某个字符串第几次结尾的字符串。
	 * @param source 截取源
	 * @param startStr 开头的字符串
	 * @param endStr 结尾的字符串
	 * @param startTimes 开头第几次
	 * @param endTimes 结尾第几次
	 * @return
	 */
	public static String betweenString(String source,String startStr,String endStr,int startTimes,int endTimes) {
		return source.substring(
				source.indexOf(startStr,startTimes),
				source.indexOf(endStr,endTimes) + endStr.length()
				);
	}
	
	/**
	 * 正则替换文本。
	 * 和replaceAll的源码实现一样，替换思路原理。
	 * https://www.jb51.net/article/110237.htm<br>
	 * @param content 源文本
	 * @param pattern 式样
	 * @param replace 替换的字符串
	 * @param matchHandler 字符串内部接口的实现对象。当参数不为null时，会在替换字符串时，把原匹配的字符串处理后，附加在替换字符串后。
	 * @return
	 */
	public static String regReplace(String content,String pattern,String replace,StringInner matchHandler) {
		//判断是否有字符处理器实现对象。
		boolean handlerFlag = false;
		if(matchHandler != null) {
			handlerFlag = true;
		}
		
		StringBuffer result = new StringBuffer();

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);
//		//上次匹配的结束字符的后一个字符下标。
//		Integer lastIndex = 0;
		while(m.find()) {
			m.appendReplacement(result, replace);
			if(handlerFlag) {
				String subStr = m.group();
				result.append(matchHandler.stringHander(subStr));
			}
			
//			下面注释掉的实现思路和上面一样，只是使用方法不一样。
			
//			//匹配开始字符的下标和结束字符的后一个字符下标
//			Integer startIndex = m.start();
//			Integer endIndex = m.end();
//			//截取content上次匹配字符的后一个下标到本次匹配的字符下标的字符串。附加到返回字符串中，要替换的字符串也附加到其中。
//			result.append(content.substring(lastIndex, startIndex))
//			.append(replace);
//			//处理匹配到的字符串，并拼接到替换字符串后。
//			if(handlerFlag) {
//				//匹配的分组字符串。group()没有分组下标或者下标为0代表整个正则匹配到的。
//				String subStr = m.group();
//				result.append(matchHandler.StringHander(subStr));
//			}
//			//修改最后匹配字符的后一个字符下标。
//			lastIndex = endIndex;
		}
		
		m.appendTail(result);
		
//		//截取最后匹配结束字符的后一个下标，到content结束的字符串，附加到返回字符串中。
//		result.append(content.substring(lastIndex));
		
		return result.toString();
	}
	
	/**
	 * 正则分组替换文本。
	 * 分组替换方法思路，但可能效率不高，每次替换后都重头从新匹配。如果替换字符再次符合正则匹配，会出现死循环。
	 * https://blog.csdn.net/qq_42055737/article/details/93204887<br>
	 * 和replaceAll的源码实现一样，替换思路原理。
	 * https://www.jb51.net/article/110237.htm<br>
	 * @param content 源文本
	 * @param pattern 式样
	 * @param group 分组map，key为分组序号，value为替换的字符串
	 * @param matchHandler 字符串内部接口的实现对象。当此参数不为null时，会在替换字符串时，把原匹配的字符串处理，附加在替换字符串后。
	 * @return
	 */
	public static String regGroupReplace(String content,String pattern,Map<Integer,String> group,StringInner matchHandler) {
		//判断是否有字符处理器实现对象。
		boolean handlerFlag = false;
		if(matchHandler != null) {
			handlerFlag = true;
		}
		
		StringBuffer result = new StringBuffer();

//		String pattern = "<img\\s*([^>]*)\\s*src=\\\"(http://.*?/)(.*?)\\\"\\s*([^>]*)>";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);
		//上次匹配的结束字符的后一个字符下标。
		Integer lastIndex = 0;
		while(m.find()) {
			for(Entry<Integer,String> entry : group.entrySet()) {
				Integer groupNumber = entry.getKey();
				String str = entry.getValue();
				//匹配的分组的开始字符的下标和结束字符的后一个字符下标
				Integer startIndex = m.start(groupNumber);
				Integer endIndex = m.end(groupNumber);
				//截取content上次匹配字符的后一个下标到本次匹配的字符下标的字符串。附加到返回字符串中，要替换的字符串也附加到其中。
				result.append(content.substring(lastIndex, startIndex))
				.append(str);
				//处理匹配到的字符串，并拼接到替换字符串后。
				if(handlerFlag) {
					//匹配的分组字符串。group()没有分组下标或者下标为0代表整个正则匹配到的。
					String subStr = m.group(groupNumber);
					result.append(matchHandler.stringHander(subStr));
				}
				//修改最后匹配字符的后一个字符下标。
				lastIndex = endIndex;
			}
		}
		//截取最后匹配结束字符的后一个下标，到content结束的字符串，附加到返回字符串中。
		result.append(content.substring(lastIndex));
		
		return result.toString();
	}
	
	/**
	 * 分割符命名转驼峰命名
	 * @param name 名称
	 * @param sep 分割符
	 * @return
	 */
	public static String separatorToCamel(String name,String sep) {
		if(isBlank(name)) {
			return "";
		}
		//sep是""时，会分割每一个字符，所以为空白时不做处理。或者name不包含sep，代表用户可能就是驼峰命名，或是不想转换命名方式。
		if(isBlank(sep) || !name.contains(sep)) {
			return name;
		}
		
		StringBuilder result = new StringBuilder();
		//匹配多个连续大写字母，匹配字符串转换第一个字母大写其他小写。处理分割符驼峰混用，或者字母全是大写的情况。
		name = regReplace(name, "[A-Z]{2,}", "", x -> x = x.substring(0,1) + x.substring(1).toLowerCase());
		//按分割符分裂成数组，第一个元素首字母小写，其他元素首字母大写。
		String[] split = name.split(sep);
		Arrays.stream(split).filter(x -> !isBlank(x)).forEach(
			x -> {
				if(result.length() == 0) {
					//驼峰命名第一个片段，首字母小写
					String first = firstLowerCase(x);
					result.append(first);
				}else {
					//驼峰命名其他片段，首字母大写
					String other = firstUpperCase(x);
					result.append(other);
				}
			}
				
		);
		return result.toString();
	}
	
	/**
	 * 驼峰命名转分割符命名。
	 * 目前方法不做驼峰命名的校验和标准格式化，默认为都是标准驼峰命名。
	 * @param name 名称
	 * @param sep 分割符
	 * @return
	 */
	public static String camelToSeparator(String name,String sep) {
		if(isBlank(name)) {
			return "";
		}
		//分割符为空，代表不进行分割。
		if(isBlank(sep)) {
			return name;
		}
		StringBuilder result = new StringBuilder();
		char[] c = name.toCharArray();
		
		//首个字符如果是大写字母，转换为小写。如果不是大写字母(如数字下划线等)，则不做处理。
		if(Character.isUpperCase(c[0])) {
			result.append(Character.toLowerCase(c[0]));
		}else {
			result.append(c[0]);
		}
		//其他位置字符，如果是大写字母，就要转成sep分割符+小写字母的形式。否则不做处理。
		for(int i = 1;i < c.length;i++) {
			if(Character.isUpperCase(c[i])) {
				result.append(sep).append(Character.toLowerCase(c[i]));
			}else {
				result.append(c[i]);
			}
		}
		return result.toString();
	}
	
	/**
	 * 首字母小写。
	 * 字符串第一个字符可能是数字，符号等。
	 * @param str
	 * @return
	 */
	public static String firstLowerCase(String str) {
		int l = str.length();
		for(int i = 0;i < l;i++) {
			char c = str.charAt(i);
			if(is26Letter(c)) {
				return Character.isUpperCase(str.charAt(i)) ? 
						str.substring(0,i) + str.substring(i,i+1).toLowerCase() + str.substring(i+1) : str;
			}
		}
		return str;
	}
	
	/**
	 * 首字母大写。
	 * 字符串第一个字符可能是数字，符号等。
	 * @param str
	 * @return
	 */
	public static String firstUpperCase(String str) {
		int l = str.length();
		for(int i = 0;i < l;i++) {
			char c = str.charAt(i);
			if(is26Letter(c)) {
				return Character.isLowerCase(str.charAt(i)) ? 
						str.substring(0,i) + str.substring(i,i+1).toUpperCase() + str.substring(i+1) : str;
			}
		}
		return str;
	}
	
	/**
	 * 判断是不是26个英文字母。
	 * 不能用Character.isLetter(ch)来判断，中文等其他字符也会返回ture。
	 * @param ch
	 * @return
	 */
	public static boolean is26Letter(char ch) {
//		ascii码方式。&&优先级高于||
//		return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
		return Character.isLowerCase(ch) || Character.isUpperCase(ch);
	}
	
	/**
	 * 字符串转换成简单类型
	 * @param <T>
	 * @param s 字符串
	 * @param cls 要转换成的类型。不是简单类型就返回null。
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertSimple(String s,Class<?> cls) {
		if(s == null) {
			return null;
		}
		//简单类型转换
		switch(cls.getTypeName()) {
		case "java.lang.String":
			return (T) s;
		case "byte":
			//如果接受值的变量是byte等基本数据类型，泛型类型推导会为Byte等包装类，泛型类型属于Object类型。
			//但是在泛型方法中，(T) byte基本类型强转会编译报错，并不会自动装箱。
			//我理解为因为不确定T是什么类型，基本类型不能直接强转。
			//所以这里不能用Byte.parseByte(s)方法。
			return (T) Byte.valueOf(s);
		case "short":
			return (T) Short.valueOf(s);
		case "char":
			return (T) Character.valueOf(s.charAt(0));
		case "int":
			return (T) Integer.valueOf(s);
		case "long":
			return (T) Long.valueOf(s);
		case "float":
			return (T) Float.valueOf(s);
		case "double":
			return (T) Double.valueOf(s);
		case "boolean":
			return (T) Boolean.valueOf(s);
		case "java.lang.Byte":
			return (T) Byte.valueOf(s);
		case "java.lang.Short":
			return (T) Short.valueOf(s);
		case "java.lang.Character":
			return (T) Character.valueOf(s.charAt(0));
		case "java.lang.Integer":
			return (T) Integer.valueOf(s);
		case "java.lang.Long":
			return (T) Long.valueOf(s);
		case "java.lang.Float":
			return (T) Float.valueOf(s);
		case "java.lang.Double":
			return (T) Double.valueOf(s);
		case "java.lang.Boolean":
			return (T) Boolean.valueOf(s);
		default:
			return null;
		}
		
	}
	
	/**
	 * 字符串转换成枚举类型。
	 * @param <T>
	 * @param s 字符串
	 * @param cls 要转换的枚举类型。不是枚举类型就返回null
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertEnum(String s,Class<?> cls) {
		if(s == null) {
			return null;
		}
		//枚举类型转换
		T t = null;
		//通过反射调用枚举类型的valueOf静态方法，获取对应名字的枚举对象
		if(cls.isEnum()) {
			try {
				Method m = cls.getDeclaredMethod("valueOf", String.class);
				m.setAccessible(true);
				//静态方法invoke第一个参数可以写为null
				t =  (T) m.invoke(null, s);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return t;
	}
	
	/**
	 * 字符串转简单数组类型。
	 * 简单数组类型，数组组件是基本类型和基本类型包装类的数组。
	 * 这里用T[]类型，无法表示基本类型数组int[]，只能表示int[][]，Integer[]等
	 * 因为T泛指属于Object的类型，int不属于Object类型，int[]属于Object类型。
	 * @param <T>
	 * @param s 字符串
	 * @param cls 要转换的简单数组类型。不是简单数组类型就返回null
	 * @param separator 分割符字符串。不能与正则匹配特殊字符冲突。因为s.split(separator)，split是用正则匹配。
	 * @return 返回类型是T泛型。
	 * 如果用T[]类型，无法表示基本类型数组int[]，只能表示int[][]，Integer[]等。
	 * 因为T泛指属于Object的类型，int不属于Object类型，int[]属于Object类型。
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertArraySimple(String s,Class<?> cls,String separator) {
		//字符串为null，返回null。
		if(s == null) {
			return null;
		}
		String[] sa = null;
		//简单数组类型转换
		switch(cls.getTypeName()) {
		case "java.lang.String[]":
			//字符串length==0的话，使用split(str)方法会生成数组，且有一个length==0的字符串元素。
			return (T) s.split(separator);
		case "byte[]":
			//字符串length==0的话，使用split(str)方法会生成数组，且有一个length==0的字符串元素。
			sa = s.split(separator);
			//这里用T[]类型，无法表示基本类型数组int[]，只能表示int[][]，Integer[]等
			//因为T泛指属于Object的类型，int不属于Object类型，int[]属于Object类型。
			byte[] bytes = (byte[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				//包装类数据赋值给基本类型，编译时自动拆箱。当然，基本类型数据赋值给包装类，也会自动装箱。
				//但是自动装箱拆箱会有忽略不计的一点性能损耗，相当于多一步类型转换。
				//所以不用Byte.valueOf(s)方法，用Byte.parseByte(s)方法。
				bytes[i] = Byte.parseByte(sa[i]);
			}
			return (T) bytes;
		case "short[]":
			sa = s.split(separator);
			short[] shorts = (short[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				shorts[i] = Short.parseShort(sa[i]);
			}
			return (T) shorts;
		case "char[]":
			//char[]数组一般就是把String每个字符转换为char，不需要分割符。
			//或者把split(separator)的分割符参数separator设为""空字符串，split会分割字符串中每一个字符。
			return (T) s.toCharArray();
		case "int[]":
			sa = s.split(separator);
			int[] ints = (int[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				ints[i] = Integer.parseInt(sa[i]);
			}
			return (T) ints;
		case "long[]":
			sa = s.split(separator);
			long[] longs = (long[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				longs[i] = Long.parseLong(sa[i]);
			}
			return (T) longs;
		case "float[]":
			sa = s.split(separator);
			float[] floats = (float[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				floats[i] = Float.parseFloat(sa[i]);
			}
			return (T) floats;
		case "double[]":
			sa = s.split(separator);
			double[] doubles = (double[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				doubles[i] = Double.parseDouble(sa[i]);
			}
			return (T) doubles;
		case "boolean[]":
			sa = s.split(separator);
			boolean[] booleans = (boolean[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				booleans[i] = Boolean.parseBoolean(sa[i]);
			}
			return (T) booleans;
		case "java.lang.Byte[]":
			sa = s.split(separator);
			Byte[] wrapBytes = (Byte[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				//包装类赋值，直接用Byte.valueOf(s)方法。用Byte.parseByte(s)方法会自动装箱，一样的。
				wrapBytes[i] = Byte.valueOf(sa[i]);
			}
			return (T) wrapBytes;
		case "java.lang.Short[]":
			sa = s.split(separator);
			Short[] wrapShorts = (Short[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				wrapShorts[i] = Short.valueOf(sa[i]);
			}
			return (T) wrapShorts;
		case "java.lang.Character[]":
			//char[]数组一般就是把String每个字符转换为char，不需要分割符。
			//或者把split(separator)的分割符参数separator设为""空字符串，split会分割字符串中每一个字符。
			char[] cs = s.toCharArray();
			
			Character[] wrapCharacters = (Character[]) Array.newInstance(cls.getComponentType(), cs.length);
			for(int i = 0;i < cs.length;i++) {
				wrapCharacters[i] = Character.valueOf(cs[i]);
			}
			return (T) wrapCharacters;
		case "java.lang.Integer[]":
			sa = s.split(separator);
			Integer[] wrapIntegers = (Integer[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				wrapIntegers[i] = Integer.valueOf(sa[i]);
			}
			return (T) wrapIntegers;
		case "java.lang.Long[]":
			sa = s.split(separator);
			Long[] wrapLongs = (Long[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				wrapLongs[i] = Long.valueOf(sa[i]);
			}
			return (T) wrapLongs;
		case "java.lang.Float[]":
			sa = s.split(separator);
			Float[] wrapFloats = (Float[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				wrapFloats[i] = Float.valueOf(sa[i]);
			}
			return (T) wrapFloats;
		case "java.lang.Double[]":
			sa = s.split(separator);
			Double[] wrapDoubles = (Double[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				wrapDoubles[i] = Double.valueOf(sa[i]);
			}
			return (T) wrapDoubles;
		case "java.lang.Boolean[]":
			sa = s.split(separator);
			Boolean[] wrapBooleans = (Boolean[]) Array.newInstance(cls.getComponentType(), sa.length);
			for(int i = 0;i < sa.length;i++) {
				wrapBooleans[i] = Boolean.valueOf(sa[i]);
			}
			return (T) wrapBooleans;
		default:
			return null;
		}
	}
	
//	public static void main(String[] args) {
//		Character[] wrapI = "".toCharArray();
//		int[] i = new Integer[] {};
//		
//		String s = "sss";
//		String s = "ssssss";
//		String s = "sssa";
//		String s = "";
//		System.out.println(Arrays.toString(s.split("sss")));
//		System.out.println(s.split("sss").length);
//		System.out.println(s.split("").length);
//		
//		int i = (Integer) null;
//		System.out.println(i);
//		
//		System.out.println(String.class.getTypeName());
//		System.out.println(String[].class.getTypeName());
//		System.out.println(char.class.getTypeName());
//		System.out.println(char[].class.getName());
//		System.out.println(int.class.getName());
//		int i = (Integer)1;
//		System.out.println((int)i);
//		
//		String a = "aaaa ";
//		String[] array = a.split("");
//		System.out.println(Arrays.toString(array));
//		
//		String content = "12312312313abcabc111:sdfsd00012312:sdfs2323:aafafadas";
//		String pattern = "\\d";
//		String pattern1 = "([a-z]*)(\\d*)(:)";
//		String pattern2 = "(===)";
//		
//		String replace = "";
//		
//		System.out.println(regReplace(content, pattern, replace, x -> x = "1"));
//		
//		Map<Integer,String> map = new HashMap<>();
//		map.put(1, "");
//		map.put(2, "00000");
//		map.put(3, "===");
//		System.out.println(regGroupReplace(content,pattern1,map,x -> x = x.toUpperCase()));
//		
//		Map<Integer,String> map2 = new HashMap<>();
//		map2.put(1, "String");
//		System.out.println(regGroupReplace(content,pattern2,map2,null));
//		
//		String str = "AA";
//		System.out.println(Character.isLowerCase(str.charAt(0)));
//		System.out.println(firstLowerCase(str));
//		System.out.println(firstUpperCase(str));
//		
//		System.out.println(Character.isLetter('呵'));
//		char ch = 'a';
//		System.out.println(ch >= 'A');
//		System.out.println(ch <= 'Z');
//		System.out.println(ch >= 'a' && ch <= 'z' || ch > 'z' && ch < 'a');
//		
//		String hyphen = "Happy-day--a-aHappyDAy";
//		String camel = "HappyADay";
//		String separator = "-";
//		System.out.println(separatorToCamel(hyphen,separator));
//		System.out.println(separatorToCamel(hyphen,"!"));
//		System.out.println(separatorToCamel(camel,"!"));
//		
//		System.out.println(camelToseparator(camel,separator));
//	}
}

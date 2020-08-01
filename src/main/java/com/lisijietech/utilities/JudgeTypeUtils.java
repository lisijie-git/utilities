package com.lisijietech.utilities;

import java.util.Collection;
import java.util.Map;

/**
 * 判断类型工具类。<br>
 * https://www.iteye.com/blog/marshzg-1565647<br>
 * https://blog.csdn.net/hekewangzi/article/details/51969774<br>
 * https://www.cnblogs.com/javJoker/p/12123045.html<br>
 * https://www.cnblogs.com/ysocean/p/8486500.html<br>
 * https://blog.csdn.net/l1028386804/article/details/80508540<br>
 * @author lisijie
 *
 */
public class JudgeTypeUtils {
	
	/**
	 * 是否是相同类型
	 * @param source
	 * @param target
	 * @return
	 */
	public static boolean isSameType(Class<?> source,Class<?> target) {
		return source == target;
	}
	
	/**
	 * 判断类型类是否是八种基本类型(byte,char,short,int,long,float,double,boolean)，和void。
	 * void.class.isPrimitive()  true
	 * Integer.class.isPrimitive() false
	 * @param c
	 * @return
	 */
	public static boolean isBasicType(Class<?> c) {
		return c.isPrimitive();
	}
	
	/**
	 * 判断是否是简单类型数据的对象。
	 * 字符串类型，基本类型的包装类，以及对象是null。
	 * 对象如果是null，用 obj instanceof Object等类型都时返回false。
	 * @param obj
	 * @return
	 */
	public static boolean isSimpleObject(Object obj) {
		boolean b = obj instanceof String || obj instanceof Number || obj instanceof Boolean 
				|| obj instanceof Character || obj == null;
		return b;
	}
	
	/**
	 * 判断是否是简单类型。
	 * 字符串，基本类型包装类。
	 * @param c
	 * @return
	 */
	public static boolean isSimpleType(Class<?> c) {
		boolean b = String.class.isAssignableFrom(c) || Number.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c) 
				|| Character.class.isAssignableFrom(c);
		return b;
	}
	
	/**
	 * 判断是否是简单数组类型。
	 * 数组的第一层组件类型是基础数据类型，或者简单类型。
	 * @param c
	 * @return
	 */
	public static boolean isArraySimpleType(Class<?> c) {
		boolean b = false;
		if(c.isArray()) {
			Class<?> component = c.getComponentType();
			b = component.isPrimitive() || 
					(
							String.class.isAssignableFrom(component) 
							|| Number.class.isAssignableFrom(component) 
							|| Boolean.class.isAssignableFrom(component) 
							|| Character.class.isAssignableFrom(component)
					);
		}
		return b;
	}
	
	/**
	 * 判断是否是复杂类型。集合，Map，数组
	 * @param c
	 * @return
	 */
	public static boolean isComplexType(Class<?> c) {
		boolean b = Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c) || c.isArray();
		return b;
	}
}

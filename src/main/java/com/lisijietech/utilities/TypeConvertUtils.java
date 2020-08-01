package com.lisijietech.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * 类型转换。
 * https://blog.godiscoder.cn/hong/article/5af505f329023064843b6ffd,转换警告优雅的解决方式
 * @author lisijie
 *
 */
public class TypeConvertUtils {
	
	/**
	 * Object转换成List。相当于浅克隆，里面的元素还是原来的元素(元素引用地址不变)
	 * @param <T>
	 * @param obj
	 * @param clazz
	 * @return 返回一个新的list
	 */
	public static <T> List<T> castNewList(Object obj,Class<T> clazz){
		List<T> result = new ArrayList<T>();
		if(obj instanceof List<?>) {
			for(Object o : (List<?>) obj) {
				result.add(clazz.cast(o));
			}
			return result;
		}
		return null;
	}
	
}

package com.lisijietech.utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 数组，集合等工具类。
 * @author lisijie
 * @date 2020-6-24
 */
public class ArraysUtils {
	/**
	 * 排除数组null元素。
	 * 如果元素全为null，则直接给数组赋值为null。
	 * 可以不用list中间存储对象，主要是了解list.toArray方法。
	 * @param <T>
	 * @param array 数组第一层元素是T泛型，所以T[]要是对象类型数组Object[]，不能是基本类型int[]，可以为int[][]，int[]是Object类型。
	 * @return 新的数组
	 */
	public static <T> T[] arrayNullExclude(T[] array) {
		if(array == null) {
			return null;
		}
		
		List<T> list = new ArrayList<>();
		for(T e : array) {
			if(e != null) {
				list.add(e);
			}
		}
		
		if(list.size() == 0) {
			return null;
		}
		//运行时根据参数类型生成数组，达到生成泛型数组的效果。
		Object obj = Array.newInstance(array.getClass().getComponentType(), list.size());
		//强转为泛型类型
		@SuppressWarnings("unchecked")
		T[] result = (T[])obj;
		//list的元素转换到泛型数组中。由于result的length等于list的size，所以不会重新生成数组，元素复制到参数数组中。不需要接收返回值。
		list.toArray(result);
		return result;
	}
	
	/**
	 * 数组除重。
	 * list.contains(e)方法比较元素相等是否能满足所有类型。
	 * 或者可以自定义比较元素是否相等。
	 * 以后优化设计。
	 * 可以不用list中间存储对象，主要是了解list.toArray方法。
	 * @param <T>
	 * @param array 数组第一层元素是T泛型，所以T[]要是对象类型数组Object[]，不能是基本类型int[]，可以为int[][]，int[]是Object类型。
	 * @return
	 */
	public static <T> T[] elementUnique(T[] array) {
		if(array == null) {
			return null;
		}
		List<T> list = new ArrayList<>();
		for(T e : array) {
			if(!list.contains(e)) {
				list.add(e);
			}
		}
		//运行时根据参数类型生成数组，达到生成泛型数组的效果。
		Object obj = Array.newInstance(array.getClass().getComponentType(), list.size());
		@SuppressWarnings("unchecked")
		T[] result = (T[])obj;
		list.toArray(result);
		return result;
	}
	
	/**
	 * 
	 * 得到两个字符串数组元素内容的交集，或者如果有参数为空,则得到并集。
	 * 这里的数组参数，需要做null判断处理，数组里元素做null处理。
	 * 一般来时null值处理越早处理校验越好。
	 * 如果返回值不能为null，或者元素的null有意义。则修改方法，不做null处理。
	 * 以后可以优化设计成通用类型的。
	 * @param a
	 * @param b
	 * @return
	 */
	public static String[] IntersectionOrBlankUnion(String[] a,String[] b) {
		//简单点的做法就是先遍历数组，排除null元素。或者全null元素直接给数组赋值null。
		//之后的参数都不用进行元素null处理。
		//a =  arrayNullExclude(a);
		//b =  arrayNullExclude(b);
		
		//数组除重
		//由于prefix业务关系。重复元素可不处理。因为，数据注入，仅匹配第一次前缀符合的。范围约束，重复范围的效果都一样。
		//cache的key规则有prefixes相关，是前缀最终效果相关的。所以要进行除重。
		a = elementUnique(a);
		b = elementUnique(b);
		
		//如果两个数组，其中有为null的情况，则返回不为null的数组。或者两个数组都是null，则返回null。
		if(a == null || a.length == 0) {
			return arrayNullExclude(b);
		}else if(b == null || b.length == 0) {
			return arrayNullExclude(a);
		}
		
		//数组里的元素有null就略过。但是其中一个数组全元素全是null，就返回另一个数组。或者两个数据里的元素全是null，就返回null
		//把元素null处理放在交集业务中，数组元素只要有非null元素，就不用判断当前数组全null而返回另一个数组。
		//不是最有效率解决方法，只是一种解决思路。还是要把元素null处理在最开始就处理了才是高效率。
		//交集处理效率也可以优化。
		List<String> list = new ArrayList<>();
		boolean aFlag = true;
		boolean bFlag = true;
		for(int i = 0;i < a.length;i++) {
			if(a[i] == null) {
				continue;
			}else {
				aFlag = false;
			}
			for(int j = 0;j < b.length;j++) {
				if(b[j] == null) {
					continue;
				}else {
					bFlag = false;
					//判断两个数据的元素内容是否相同，相同就添加入list。
					//由于prefix业务关系。重复元素可不处理。因为，数据注入，仅匹配第一次前缀符合的。范围约束，重复范围的效果都一样。
					//cache的key规则有prefixes相关，是前缀最终效果相关的。所以要进行除重。
					//最好在prefixes参数获取到时就除重。从逻辑上来说，之后就不用除重了。
					if(a[i].equals(b[j])) {
						list.add(b[i]);
						break;
					}
				}
			}
		}
		
		//如果有数组元素全为null，则返回另一个素组，或者null。
		//只是用一下flag写法。不推荐这样使用。
		//最好在获取数组参数时，就排除null就不用写这么多冗余代码。
		if(aFlag) {
			return arrayNullExclude(b);
		}
		if(bFlag) {
			return arrayNullExclude(a);
		}
		
		//如果交集为0，则赋值null。因为prefixes数组的null有意义，虽然数组length == 0也表示同样意思。
		//所以以后要设计好参数null代表什么，空值代表什么，不要发生重复作用。
		//或者不能为null，但空值有意义时，就无法满足缺省意义，需要自己约定或者设计。
		String[] result = list.toArray(new String[0]);
		if(result.length == 0) {
			result = null;
		}
		return result;
	}
}

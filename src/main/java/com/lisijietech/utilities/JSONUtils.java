package com.lisijietech.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json工具类
 * @author lisijie
 *
 */
public class JSONUtils {
	
	//Jackson组件方法===============================
	
	public static ObjectMapper getJackson() {
		return new ObjectMapper();
	}
	
	/**
	 * json字符串反序列化。
	 * 通过泛型的类型推导
	 * @param <T>
	 * @param s
	 * @return
	 */
	public static <T> T deserialize(String s) {
		ObjectMapper mapper = getJackson();
		T t = null;
		try {
			t = (T) mapper.readValue(s, new TypeReference<T>(){});
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	/**
	 * json字符串反序列化。
	 * 指定泛型，以及泛型参数
	 * @param <T>
	 * @param s
	 * @param parametrized
	 * @param parameterClasses
	 * @return
	 */
	public static <T> T deserialize(String s,Class<?> parametrized,Class<?>[] parameterClasses) {
		ObjectMapper mapper = getJackson();
		T t = null;
		
		JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
		try {
			t = mapper.readValue(s,javaType);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return t;
	}

}

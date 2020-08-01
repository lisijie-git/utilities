package com.lisijietech.utilities;

import javax.servlet.http.HttpServletRequest;

/**
 * ip地址工具
 * @author lisijie
 *
 */
public class IpAddressUtils {
	
	/**
	 * 获取真实ip地址<br>
	 * https://blog.csdn.net/chwshuang/article/details/71940858
	 * https://www.cnblogs.com/zhi-hao/articles/4630427.html
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request){
		String Xip = request.getHeader("X-Real-IP");
		String XFor = request.getHeader("X-Forwarded-For");
		
		//StringUtils.isNotEmpty(XFor)方法用不了，可能没有jar包，只能自己写了
		if(XFor != null && XFor.length() > 0 && !(XFor.matches("^[ ]*$")) && 
				!("unknown".equalsIgnoreCase(XFor))){
			//多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = XFor.indexOf(",");
			if(index != -1){
				return XFor.substring(0, index);
			}else{
				return XFor;
			}
		}
		
		XFor = Xip;
		if(XFor != null && XFor.length() > 0 && !(XFor.matches("^[ ]*$")) && 
				!("unknown".equalsIgnoreCase(XFor))){
			return XFor;
		}
		
		//上面没找到ip地址就依次向下找
		
		if(XFor == null || XFor.length() == 0 || XFor.matches("^[ ]*$") || 
				"unknown".equalsIgnoreCase(XFor)){
			XFor = request.getHeader("Proxy-Client-IP");
		}
		if(XFor == null || XFor.length() == 0 || XFor.matches("^[ ]*$") || 
				"unknown".equalsIgnoreCase(XFor)){
			XFor = request.getHeader("WL-Proxy-Client-IP");
		}
		if(XFor == null || XFor.length() == 0 || XFor.matches("^[ ]*$") || 
				"unknown".equalsIgnoreCase(XFor)){
			XFor = request.getHeader("HTTP_CLIENT_IP");
		}
		if(XFor == null || XFor.length() == 0 || XFor.matches("^[ ]*$") || 
				"unknown".equalsIgnoreCase(XFor)){
			XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if(XFor == null || XFor.length() == 0 || XFor.matches("^[ ]*$") || 
				"unknown".equalsIgnoreCase(XFor)){
			XFor = request.getRemoteAddr();
		}
		return XFor;
	}

}

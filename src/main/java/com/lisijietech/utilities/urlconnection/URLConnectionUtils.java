package com.lisijietech.utilities.urlconnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * URLConnection工具类
 * 参考：<br>
 * https://blog.csdn.net/pengjunlee/article/details/85257375<br>
 * https://blog.csdn.net/qq_35716892/article/details/82847211<br>
 * https://www.cnblogs.com/aspirant/p/11240502.html<br>
 * https://www.jianshu.com/p/29e38bcc8a1d<br>
 * https://www.cnblogs.com/JonaLin/p/11269465.html<br>
 * https://blog.csdn.net/zmx729618/article/details/51190090<br>
 * https://blog.csdn.net/ke369093457/article/details/85777613<br>
 * https://blog.csdn.net/b1480521874/article/details/89892095<br>
 * https://my.oschina.net/skyzwg/blog/850033<br>
 * https://cloud.tencent.com/developer/ask/97593<br>
 * https://www.cnblogs.com/zhao-shan/p/9049731.html<br>
 * https://www.jianshu.com/p/e2777f2e36ee<br>
 * 
 * HttpsURLConnection处理参考:<br>
 * https://www.cnblogs.com/mengen/p/9138214.html<br>
 * https://blog.csdn.net/qq_33562996/article/details/80669004<br>
 * https://blog.csdn.net/zz153417230/article/details/80271155<br>
 * https://www.cnblogs.com/adjk/p/11124345.html<br>
 * https://blog.csdn.net/suyimin2010/article/details/81025083<br>
 * https://blog.csdn.net/pengjunlee/article/details/85257375<br>
 * https://www.cnblogs.com/adjk/p/11124345.html<br>
 * <br>
 * 代理<br>
 * https://www.jianshu.com/p/9e1abe05314d<br>
 * https://www.jianshu.com/p/9e1abe05314d<br>
 * https://www.cnblogs.com/gcczhongduan/p/4802165.html<br>
 * https://blog.csdn.net/redhat456/article/details/6149774<br>
 * https://www.cnblogs.com/exmyth/p/6493285.html<br>
 * https://www.cnblogs.com/fengdeng/p/5842892.html<br>
 * https://segmentfault.com/q/1010000018473815<br>
 * 
 * @author lisijie
 *
 */
public class URLConnectionUtils {
	//字符集
	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String CHARSET_GBK = "GBK";
	public static final String CHARSET_ISO = "ISO-8859-1";
	public static final String CHARSET_UNI = "unicode";
	//请求方法
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	//内容类型
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APP_URL = "application/x-www-form-urlencoded";
	public static final String APP_JSON = "application/json";
	public static final String MULTIPART ="multipart/form-data";
	public static final String TEXT_XML ="text/xml";
	
	//响应头，内容安排
	public static final String CONTENT_DISP = "Content-Disposition";
	//附件，一般浏览器会下载文件，filename中文要进行URL编码
	public static final String CONTENT_ATTA = "attachment;filename=";
	
	//multipart/form-data内容类型的分界字符串
	public static final String BOUNDARY = "----LisijieBoundary7MA4YWxkTrZu0gW";
	//回车换行符
	public static final String CRLF = "\r\n";
	
	//连接时间，毫秒
	public static final int CONNECT_TIME = 100000;
	//读取时间，毫秒
	public static final int READ_TIME = 150000;
	
	//信任管理器。忽略证书验证，即信任全部证书。匿名内部类的形式重写验证过程。并且作为静态成员变量，方便使用。
	//如果要指定证书，重新创建一个，用到再说。
	public static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] {
			new X509TrustManager() {
				//检查客户端证书
				@Override
				public void checkClientTrusted(X509Certificate[] certs,String authType) throws CertificateException {
					
				}
				//检查服务端证书
				@Override
				public void checkServerTrusted(X509Certificate[] certs,String authType) throws CertificateException {
					
				}
				//返回受信任的X509证书数组
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}	
			}		
	};
	
	/**
	 * 通用URLConnection方法。
	 * 也可以设置代理连接，参考网络，java系统设置为代理，或者灵活到每次连接代理，如：<br>
	 * https://blog.csdn.net/zmx729618/article/details/51190090<br>
	 * https://blog.csdn.net/scwfly2011/article/details/83302645<br>
	 * https://www.jianshu.com/p/cddd5f8db166<br>
	 * https://ask.csdn.net/questions/200964<br>
	 * https://segmentfault.com/q/1010000018473815<br>
	 * @param urlStr url地址，GET方法时，参数格式要预先处理好
	 * @param proxyVO 代理数据对象
	 * @param charset 字符集。此方法参数字符集自己处理，并且返回值是byte数组。所以暂时用不到这个参数
	 * @param method 请求方法
	 * @param config 连接配置
	 * @param headers 消息头
	 * @param contentType 内容类型。其额外字符数据拼接格式预先处理好。处理方法在本工具类有。如果headers中也有，会被此参数覆盖。
	 * @param datas post方法的消息体数据。其数据格式预先处理好转换成字节数组（一般是三种格式，处理方法在本工具类有）。
	 * 在OutputStream写出时，按List集合中正序遍历write。
	 * @return 返回值是byte数组，可能是字符串，也可能是文件，或者文件字符串混合拼接，混合拼接需要自己解析数据。
	 * 拼接规则可以参考http的form表单上传，文件下载的报文。或者简单点List<byte[]>这种规则。或者明确定义一个返回类。
	 */
	public static byte[] generalConnection(String urlStr,ProxyVO proxyVO,String charset,String method,
			Map<String,String> config,Map<String,String> headers,String contentType,List<byte[]> datas) {
		//校验字符集，默认为UTF-8。暂时用不到这个参数
//		if(charset == null || "".equals(charset.trim())) {
//			charset = CHARSET_UTF8;
//		}
		//请求方法校验，判空，转换为大写
		if(method == null || "".equals(charset.trim())) {
			method = METHOD_GET;
		}else {
			method = method.toUpperCase();
		}
		
		byte[] result = null;
		URL url = null;
		
		//由于URLConnection是HttpURLConnection父类，HttpURLConnection是HttpsURLConnection的父类。
		//URLConnection类型的引用,在获取响应时,缺少常用的方法如getRequestMethod(),在使用方法时要强制类型转换为HttpURLConnection
		//而https,只是在请求连接前，一些设置方法http中没有，如setSSLSocketFactory()，而在获取响应数据的方法基本和HttpURLConnection一样
		//所以声明HttpURLConnection类型引用，减少因某些方法需要判断对象类型，强制类型转换，才能使用的步骤。
		HttpURLConnection conn = null;
		OutputStream os = null;
		InputStream in = null;
		
		try {
			//1.创建对象URL，通过URL对象调用openConnection方法创建连接对象
			url = new URL(urlStr);
			//1.1判断是否需要代理
			if(proxyVO == null) {
				conn = (HttpURLConnection) url.openConnection();
			}else {
				conn = (HttpURLConnection) url.openConnection(getProxy(proxyVO));
				//判断是否需要设置proxy代理认证。
				String user = proxyVO.getUser();
				if(user != null && user.trim().length() > 0) {
					setProxyHttpAuth(proxyVO, conn, charset);
				}
			}
			//1.2判断是http还是https协议的连接。如果是https需要相应设置。
			if(urlStr.startsWith("https:")) {
				setHttpsTrustAll((HttpsURLConnection) conn);
			}
			
			//2.设置连接参数和请求数据
			//2.1设置连接参数
			conn.setRequestMethod(method);//默认为GET，所以不设置也行
//			conn.setDoInput(true);//是否从连接读入数据，默认为true
			//如果为post请求，需要开启输出设置为true
			if(method.equalsIgnoreCase(METHOD_POST)) conn.setDoOutput(true);//POST请求需要数据输出，默认为false
			//通用连接设置，一般不需要动态修改
			conn.setUseCaches(false);//不使用缓存
			conn.setConnectTimeout(CONNECT_TIME);//设置连接主机超时
			conn.setReadTimeout(READ_TIME);//设置从主机读取数据超时
			
			//2.2设置请求数据
			//通用请求头，没有特殊要求可以不设置
//			conn.setRequestProperty("accept","");这里的value值是个坑,里面是两个星号，在方法注释的时候，这里会被认为注释的结尾，要注意
//			conn.setRequestProperty("connection","Keep-Alive");
//			conn.setRequestProperty("Charset","UTF-8");
//			conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			
			//遍历设置消息头
			if(headers != null && !(headers.isEmpty())) {
				for(Entry<String,String> entry : headers.entrySet()) {
					conn.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				}
			}
			
			//设置Content-Type消息头，如果上面遍历设置消息头中也设置了将会被更新覆盖。
			//无论是GET，POST方法，不一定要设置。通常GET没有内容类型设置，POST需要指明内容类型设置，我理解一般POST没参数就不需要设置。
			if(contentType != null && contentType.trim().length() > 0) {
				conn.setRequestProperty(CONTENT_TYPE,contentType);
			}
			
			//3.使用connect方法建立远程连接,可以不用，conn的一系列getXxx方法会隐式调用远程连接方法
//			conn.connect();
			
			//4.设置POST请求正文，并发送数据。
			//OutputStream会把数据进行缓冲，据资料说明在调用getInputStream时才会真正发送数据，
			//这样可以计算输出数据大小等，程序才能自动设置Content-Length。
			//请求正文设置要在获取响应前设置。
			
			//POST请求的消息体数据，不同内容类型，其消息体中的数据格式不同。目前先分三种，key-value字符串，json字符串，二进制流。
			
			//datas参数是已经处理好数据格式的byte数组，由于字符串的字节数据和文件的字节数据不好拼接，要进行byte[]扩容复制，
			//直接用List顺序保存，然后顺序写入OutPutStream中比较方便。
			//GET方法的参数直接在连接地址后拼接url编码数据，不在消息体里。
			
			//这是原来处理字符串key-value使用的字符流，由于有文件，就直接自己处理数据统一成byte[]，就不用包装流和字节流混合用了
//			OutputStream os = conn.getOutputStream();
//			OutputStreamWriter osw = new OutputStreamWriter(os,charset);//注意数据字符编码，更具需要进行设置
//			PrintWriter pw = new PrintWriter(osw);
//			pw.print(params);
//			pw.flush();
//			pw.close();
			
			if(method.equals(METHOD_POST) && datas != null && datas.size() > 0) {
				os = conn.getOutputStream();
				for(byte[] b : datas) {
					os.write(b);
				}
				//如果os被缓冲流包装，则必须要flush
				os.flush();
			}
			
			//4.获取响应数据
			//判断响应是否成功，一般http用200状态码表示成功。不成功则返回默认数据null
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				//获取响应头信息，一般不需要返回响应头，如果需要，那方法返回结果需要修改返回类型，或者就拼接在byte[]中，自己解析
				//Map<String,List<String>> heards = conn.getHeaderFields();
				//如果有文件属性消息头，获取文件名称。如果需要，如消息头一样处理
				//String fileName = getRespFileName(conn,CHARSET_UTF8,true);
				//获取响应消息体数据
				in = conn.getInputStream();
				result = inputToByte(in);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//空闲时间断开底层tcp socket连接
		conn.disconnect();
		
		return result;
	}
	
	/**
	 * GET请求获取byte[]响应体数据。
	 * 响应体数据保存在byte[] datas中。
	 * 如果有文件名称，存储在fileName中。
	 * 获取String的相应体内容也可以，把byte[]转换成字符串就行。
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param params
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doGetByte(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			Map<String,String> params,List<String> respHeaders){
		if(charset == null || "".equals(charset.trim())) {
			charset = CHARSET_UTF8;
		}
		
		if(params != null && params.size() > 0) {
			if(urlStr.indexOf("?") == -1) {
				//GET请求参数，需要URL编码。
				urlStr = urlStr + "?" + concatUrlKeyValue(params, true, charset);
			}else {
				//GET请求参数，需要URL编码。
				urlStr = urlStr + "&" + concatUrlKeyValue(params, true, charset);;
			}
		}
		
		HttpRespVO result = null;
		URL url = null;
		HttpURLConnection conn = null;
		InputStream in = null;
		
		try {
			url = new URL(urlStr);
			
			if(proxyVO == null) {
				conn = (HttpURLConnection) url.openConnection();
			}else {
				conn = (HttpURLConnection) url.openConnection(getProxy(proxyVO));
				String user = proxyVO.getUser();
				if(user != null && user.trim().length() > 0) {
					setProxyHttpAuth(proxyVO, conn, charset);
				}
			}
			
			if(urlStr.startsWith("https:")) {
				setHttpsTrustAll((HttpsURLConnection) conn);
			}
			
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNECT_TIME);
			conn.setReadTimeout(READ_TIME);
			
			if(headers != null && !(headers.isEmpty())) {
				for(Entry<String,String> entry : headers.entrySet()) {
					conn.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				}
			}
			
			conn.connect();
						
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = new HttpRespVO();
				//获取响应消息头
				if(respHeaders != null && respHeaders.size() > 0) {
					Map<String,String> respMap = new HashMap<>();
					for(String name : respHeaders) {
						String value = conn.getHeaderField(name);
						respMap.put(name, value);
					}
					result.setHeaders(respMap);
				}
				
				//如果有文件属性消息头，获取文件名称
				String fileName = getRespFileName(conn,charset,true);
				result.setFileName(fileName);
				
				in = conn.getInputStream();
				byte[] datas = inputToByte(in);
				result.setDatas(datas);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		conn.disconnect();
		
		return result;
	}
	
	/**
	 * GET请求获取字符串响应数据。
	 * 其实可以把doGetbyte方法返回的byte[]转换成String就行了。这样写可能会快一步，也比较明确。
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param params
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doGetStr(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			Map<String,String> params,List<String> respHeaders) {
		if(charset == null || "".equals(charset.trim())) {
			charset = CHARSET_UTF8;
		}
		
		if(params != null && params.size() > 0) {
			if(urlStr.indexOf("?") == -1) {
				//GET请求参数，需要URL编码。
				urlStr = urlStr + "?" + concatUrlKeyValue(params, true, charset);
			}else {
				//GET请求参数，需要URL编码。
				urlStr = urlStr + "&" + concatUrlKeyValue(params, true, charset);;
			}
		}
		
		HttpRespVO result = null;
		URL url = null;
		HttpURLConnection conn = null;
		InputStream in = null;
		
		try {
			url = new URL(urlStr);
			
			if(proxyVO == null) {
				conn = (HttpURLConnection) url.openConnection();
			}else {
				conn = (HttpURLConnection) url.openConnection(getProxy(proxyVO));
				String user = proxyVO.getUser();
				if(user != null && user.trim().length() > 0) {
					setProxyHttpAuth(proxyVO, conn, charset);
				}
			}
			
			if(urlStr.startsWith("https:")) {
				setHttpsTrustAll((HttpsURLConnection) conn);
			}
			
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNECT_TIME);
			conn.setReadTimeout(READ_TIME);
			
			if(headers != null && !(headers.isEmpty())) {
				for(Entry<String,String> entry : headers.entrySet()) {
					conn.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				}
			}
			
			conn.connect();
						
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = new HttpRespVO();
				//获取响应消息头
				if(respHeaders != null && respHeaders.size() > 0) {
					Map<String,String> respMap = new HashMap<>();
					for(String name : respHeaders) {
						String value = conn.getHeaderField(name);
						respMap.put(name, value);
					}
					result.setHeaders(respMap);
				}
				
				in = conn.getInputStream();
				String content = inputToString(in,charset);
				result.setContent(content);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		conn.disconnect();
		
		return result;
	}
	
	/**
	 * POST请求获取byte[]响应数据。
	 * 响应体数据保存在byte[] datas中。
	 * 如果有文件名称，存储在fileName中。
	 * 一般用来获取文件数据。
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param contentType
	 * @param datas
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doPostByte(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			String contentType,List<byte[]> datas,List<String> respHeaders) {
		if(charset == null || "".equals(charset.trim())) {
			charset = CHARSET_UTF8;
		}
		
		HttpRespVO result = null;
		URL url = null;
		
		HttpURLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;
		try {
			url = new URL(urlStr);
			
			if(proxyVO == null) {
				conn = (HttpURLConnection) url.openConnection();
			}else {
				conn = (HttpURLConnection) url.openConnection(getProxy(proxyVO));
				String user = proxyVO.getUser();
				if(user != null && user.trim().length() > 0) {
					setProxyHttpAuth(proxyVO, conn, charset);
				}
			}
			
			if(urlStr.startsWith("https:")) {
				setHttpsTrustAll((HttpsURLConnection) conn);
			}
			
			conn.setRequestMethod(METHOD_POST);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNECT_TIME);
			conn.setReadTimeout(READ_TIME);
			
			if(headers != null && !(headers.isEmpty())) {
				for(Entry<String,String> entry : headers.entrySet()) {
					conn.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				}
			}
			
			if(contentType != null && contentType.trim().length() > 0) {
				conn.setRequestProperty(CONTENT_TYPE,contentType);
			}
			
			if(datas != null && datas.size() > 0) {
				os = conn.getOutputStream();
				for(byte[] b : datas) {
					os.write(b);
				}
				os.flush();
			}
			
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = new HttpRespVO();
				//获取响应消息头
				if(respHeaders != null && respHeaders.size() > 0) {
					Map<String,String> respMap = new HashMap<>();
					for(String name : respHeaders) {
						String value = conn.getHeaderField(name);
						respMap.put(name, value);
					}
					result.setHeaders(respMap);
				}
				//如果有文件属性消息头，获取文件名称
				String fileName = getRespFileName(conn,charset,true);
				result.setFileName(fileName);
				
				//获取响应消息体数据
				is = conn.getInputStream();
				byte[] respDatas = inputToByte(is);
				result.setDatas(respDatas);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		conn.disconnect();
		
		return result;
	}
	
	/**
	 * POST请求获取字符串响应数据。
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param contentType
	 * @param datas
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doPostStr(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			String contentType,List<byte[]> datas,List<String> respHeaders) {
		if(charset == null || "".equals(charset.trim())) {
			charset = CHARSET_UTF8;
		}
		
		HttpRespVO result = null;
		URL url = null;
		
		HttpURLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;
		try {
			url = new URL(urlStr);
			
			if(proxyVO == null) {
				conn = (HttpURLConnection) url.openConnection();
			}else {
				conn = (HttpURLConnection) url.openConnection(getProxy(proxyVO));
				String user = proxyVO.getUser();
				if(user != null && user.trim().length() > 0) {
					setProxyHttpAuth(proxyVO, conn, charset);
				}
			}
			
			if(urlStr.startsWith("https:")) {
				setHttpsTrustAll((HttpsURLConnection) conn);
			}
			
			conn.setRequestMethod(METHOD_POST);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNECT_TIME);
			conn.setReadTimeout(READ_TIME);
			
			if(headers != null && !(headers.isEmpty())) {
				for(Entry<String,String> entry : headers.entrySet()) {
					conn.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				}
			}
			
			if(contentType != null && contentType.trim().length() > 0) {
				conn.setRequestProperty(CONTENT_TYPE,contentType);
			}
			
			if(datas != null && datas.size() > 0) {
				os = conn.getOutputStream();
				for(byte[] b : datas) {
					os.write(b);
				}
				os.flush();
			}
			
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = new HttpRespVO();
				//获取响应消息头
				if(respHeaders != null && respHeaders.size() > 0) {
					Map<String,String> respMap = new HashMap<>();
					for(String name : respHeaders) {
						String value = conn.getHeaderField(name);
						respMap.put(name, value);
					}
					result.setHeaders(respMap);
				}
				//获取响应消息体数据
				is = conn.getInputStream();
				String content = inputToString(is,charset);
				result.setContent(content);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		conn.disconnect();
		
		return result;
	}
	
	/**
	 * POST请求获取字符串，application/x-www-form-urlencoded类型内容参数
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param contentType
	 * @param params
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doPostUrlStr(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			String contentType,Map<String,String> params,List<String> respHeaders) {
		HttpRespVO result = null;
		//contentType不要拼接字符集，由使用者自定义。
		if(contentType == null || contentType.length() < 1) {
			contentType = APP_URL;
		}
		List<byte[]> bytes = null;
		if(params != null && params.size() > 0) {
			try {
				//POST请求方法体内的参数，不需要URL编码。如需要，自行预先处理。
				byte[] b = concatUrlKeyValue(params,false,charset).getBytes(charset);
				bytes = new ArrayList<byte[]>();
				bytes.add(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		result = doPostStr(urlStr, proxyVO, charset, headers, contentType, bytes, respHeaders);
		return result;
	}
	
	/**
	 * POST请求获取文件数据，application/x-www-form-urlencoded类型内容参数
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param contentType
	 * @param params
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doPostUrlFile(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			String contentType,Map<String,String> params,List<String> respHeaders) {
		HttpRespVO result = null;
		//contentType不要拼接字符集，由使用者自定义。
		if(contentType == null || contentType.length() < 1) {
			contentType = APP_URL;
		}
		List<byte[]> bytes = null;
		if(params != null && params.size() > 0) {
			try {
				//POST请求方法体内的参数，不需要URL编码。如需要，自行预先处理。
				byte[] b = concatUrlKeyValue(params,false,charset).getBytes(charset);
				bytes = new ArrayList<byte[]>();
				bytes.add(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		result = doPostByte(urlStr, proxyVO, charset, headers, contentType, bytes, respHeaders);
		return result;
	}
	
	/**
	 *  POST请求获取字符串数据，application/json内容类型参数
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param jsonStr
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doPostJsonStr(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			String jsonStr,List<String> respHeaders) {
		HttpRespVO result = null;
		String contentType = concatenateCharset(APP_JSON,charset);
		List<byte[]> bytes = null;
		if(jsonStr != null && jsonStr.trim().length() > 0) {
			try {
				byte[] b = jsonStr.getBytes(charset);
				bytes = new ArrayList<byte[]>();
				bytes.add(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		result = doPostStr(urlStr, proxyVO, charset, headers, contentType, bytes, respHeaders);
		return result;
	}
	
	/**
	 *  POST请求获取文件数据，application/json内容类型参数
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param jsonStr
	 * @param respHeaders
	 * @return
	 */
	public static HttpRespVO doPostJsonFile(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			String jsonStr,List<String> respHeaders) {
		HttpRespVO result = null;
		String contentType = concatenateCharset(APP_JSON,charset);
		List<byte[]> bytes = null;
		if(jsonStr != null && jsonStr.trim().length() > 0) {
			try {
				byte[] b = jsonStr.getBytes(charset);
				bytes = new ArrayList<byte[]>();
				bytes.add(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		result = doPostByte(urlStr, proxyVO, charset, headers, contentType, bytes, respHeaders);
		return result;
	}
	
	/**
	 * POST请求multipart/form-data内容类型的数据。
	 * @param urlStr
	 * @param proxyVO
	 * @param charset
	 * @param headers
	 * @param params
	 * @param files
	 * @param respHeaders
	 * @return 根据响应内容设置返回对象的相应属性
	 */
	public static HttpRespVO doPostMulti(String urlStr,ProxyVO proxyVO,String charset,Map<String,String> headers,
			Map<String,String> params,List<MultipartFileVO> files,List<String> respHeaders) {
		if(charset == null || "".equals(charset.trim())) {
			charset = CHARSET_UTF8;
		}
		
		//POST提交数据内容类型为multipart/form-data的格式处理
		List<byte[]> datas = new ArrayList<>();
		//普通数据格式处理
		if(params != null && params.size() > 0) {
			//POST请求方法体内的参数，不需要URL编码。如需要，自行预先处理。
			datas.add(concatMultiKeyValue(params,false,charset));
		}
		//文件数据格式处理
		if(files != null && files.size() > 0) {
			for(MultipartFileVO file : files) {
				//POST请求方法体内的参数，不需要URL编码。如需要，自行预先处理。
				datas.addAll(concatMultiFile(file,false,charset));
			}
		}
		//结束分界符，不管有没有参数，都要有结束分界符
		datas.add(endBoundary(charset));
		
		//multipart内容类型
		String contentType = concatBoundary(MULTIPART);
		
		HttpRespVO result= null;
		URL url = null;
		HttpURLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;
		try {
			url = new URL(urlStr);
			
			if(proxyVO == null) {
				conn = (HttpURLConnection) url.openConnection();
			}else {
				conn = (HttpURLConnection) url.openConnection(getProxy(proxyVO));
				String user = proxyVO.getUser();
				if(user != null && user.trim().length() > 0) {
					setProxyHttpAuth(proxyVO, conn, charset);
				}
			}
			
			if(urlStr.startsWith("https:")) {
				setHttpsTrustAll((HttpsURLConnection) conn);
			}
			
			conn.setRequestMethod(METHOD_POST);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNECT_TIME);
			conn.setReadTimeout(READ_TIME);
			
			if(headers != null && !(headers.isEmpty())) {
				for(Entry<String,String> entry : headers.entrySet()) {
					conn.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				}
			}
			
			conn.setRequestProperty(CONTENT_TYPE,contentType);
			
			os = conn.getOutputStream();
			for(byte[] b : datas) {
				os.write(b);
			}
			os.flush();
			
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = new HttpRespVO();
				//获取响应消息头
				if(respHeaders != null && respHeaders.size() > 0) {
					Map<String,String> respMap = new HashMap<>();
					for(String name : respHeaders) {
						String value = conn.getHeaderField(name);
						respMap.put(name, value);
					}
					result.setHeaders(respMap);
				}
				//获取文件名。没有为null
				String fileName = getRespFileName(conn,charset,true);
				
				//获取响应消息体数据
				is = conn.getInputStream();
				
				//根据响应内容类型，设置返回数据格式
				if(fileName != null) {
					//响应数据是文件类型
					result.setFileName(fileName);
					result.setDatas(inputToByte(is));
				}else {
					//响应数据是其他类型。一般就是字符串类型
					result.setContent(inputToString(is,charset));
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		conn.disconnect();
		
		return result;
	}
	
	/**
	 * 设置当前HttpsURLConnection的SSL上下文，并且信任所有证书(不验证任何证书)。
	 * 以后再扩展可以获得指定证书的https，目前信任全部证书。
	 * 也可以全局默认设置忽略注验证，信任所有证书。这样可以用http连接对象进行网络访问，不用必须使用https连接对象设置SSL。
	 * <br>
	 * https://www.cnblogs.com/mengen/p/9138214.html<br>
	 * https://blog.csdn.net/zz153417230/article/details/80271155<br>
	 * 
	 * @param httpsConn 这个方法必须要用HttpsURLConnection类型对象
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws KeyManagementException
	 */
	public static void setHttpsTrustAll(HttpsURLConnection httpsConn) 
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
		//创建SSLContext对象
		SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
		sslContext.init(null, TRUST_ALL_CERTS, new SecureRandom());
		//从上述SSLContext对象中得到SSLSocketFactory对象
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		//给HttpsURLConnection设置SSLSocketFactory
		httpsConn.setSSLSocketFactory(ssf);
	}
	
	/**
	 * 全局默认设置忽略https验证，信任所有证书(不验证任何证书)。
	 * 可以用http连接对象进行网络访问，不用必须使用https连接对象。
	 * 这个方法可以用http连接对象进行网络访问，因为不需要使用https连接对象设置SSL。不过暂时不用这个方法。
	 * <br>
	 * https://www.cnblogs.com/mengen/p/9138214.html<br>
	 * https://blog.csdn.net/zz153417230/article/details/80271155<br>
	 * 
	 * @throws KeyManagementException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void setHttpsIgnoreVerify() 
			throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
		//创建SSLContext对象
		SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
		sslContext.init(null, TRUST_ALL_CERTS, new SecureRandom());
		HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				//System.out.println("WARNING:HostName is not matched for cert");
				return true;
			}
			
		};
		HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	}
	
	/**
	 * 获取代理对象。
	 * 使用Proxy设置代理。
	 * @param vo
	 * @return
	 */
	public static Proxy getProxy(ProxyVO vo) {
		SocketAddress sa = new InetSocketAddress(vo.getHost(),vo.getPort());
		Proxy proxy = new Proxy(Proxy.Type.HTTP,sa);
		return proxy;
	}
	
	/**
	 * 设置http请求头的代理认证。
	 * 不同代理服务，认证方式可能不同。
	 * @param vo
	 * @param conn
	 * @throws UnsupportedEncodingException 
	 */
	public static void setProxyHttpAuth(ProxyVO vo,HttpURLConnection conn,String charset) 
			throws UnsupportedEncodingException {
		String auth = vo.getUser() + ":" + vo.getPassword();
		byte[] b = auth.getBytes(charset);
		String encode = Base64.getEncoder().encodeToString(b);
		conn.setRequestProperty("Proxy-authorization", "Basic " + encode);
	}
	
	/**
	 * 请求内容类型连接字符集
	 * @param type
	 * @param charset
	 * @return
	 */
	public static String concatenateCharset(String content,String charset) {
		return content + ";charset=" + charset;
	}
	
	/**
	 * 内容类型form/data的分界字符串拼接
	 * @param content
	 * @return
	 */
	public static String concatBoundary(String content) {
		return content + ";boundary=" + BOUNDARY;
	}
	
	/**
	 * 键值对参数拼接
	 * @param params
	 * @param isUrl 是否要URL编码，主要是GET方法的url地址参数的value值编码，POST消息体参数value值也可以编码。
	 * @param charset URL编码设置字符集
	 * @return
	 */
	public static String concatUrlKeyValue(Map<String,String> params,boolean isUrl,String charset) {
		StringBuilder sb = new StringBuilder();
		for(Entry<String,String> entry : params.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			//URLEncode编码。在for循环里判断url编码，效率可能慢一些，但结构清晰。
			if(isUrl) {
				if(charset == null || charset.trim().length() == 0) {
					charset = CHARSET_UTF8;
				}
				try {
					value = URLEncoder.encode(value, charset);
				} catch (UnsupportedEncodingException e) {
					value = "";
				}
			}
			
			sb.append(key).append("=").append(value).append("&");
		}
		//删除最后一个&
		sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
	
	/**
	 * 拼接multipart/form-data内容类型的字符数据,并获得byte数组。
	 * 注意，没有结束分界符，因为不确定还有没有文件类型等其他数据的添加。
	 * multipart/form-data的数据格式参考网络就行。
	 * @param map 参数key-value
	 * @param isUrl 是否需要url编码
	 * @param charset 字符集。按字符集获取字符byte数据，按字符集编码字符串
	 * @return
	 */
	public static byte[] concatMultiKeyValue(Map<String,String> map,boolean isUrl,String charset) {
		StringBuffer sb = new StringBuffer();
		byte[] result = null;
		if(map != null && map.size() > 0) {
			//判断value是否需要url编码
			if(isUrl) {
				for(Entry<String,String> entry : map.entrySet()) {
					String value = "";
					try {
						value = URLEncoder.encode(entry.getValue(),charset);
					} catch (UnsupportedEncodingException e) {
						value = "UrlEncodeError";
					}
					
					sb.append(BOUNDARY).append(CRLF)
					.append("Content-Disposition:form-data;name=\"").append(entry.getKey()).append("\"").append(CRLF)
					.append(CRLF)
					.append(value).append(CRLF);
				}
			}else {
				for(Entry<String,String> entry : map.entrySet()) {
					sb.append(BOUNDARY).append(CRLF)
					.append("Content-Disposition:form-data;name=\"").append(entry.getKey()).append("\"").append(CRLF)
					.append(CRLF)
					.append(entry.getValue()).append(CRLF);
				}
			}
			
		}
		
		try {
			result = sb.toString().getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 拼接multipart/form-data内容类型的文件数据,按顺序放入List<byte[]>。
	 * 注意，没有结束分界符，因为不确定还有没有文件类型等其他数据的添加。
	 * @param file 文件对象
	 * @param isUrl 是否给文件名称URL编码
	 * @param charset 字符集。按字符集获取字符byte数据，按字符集编码字符串
	 * @return
	 */
	public static List<byte[]> concatMultiFile(MultipartFileVO file,boolean isUrl,String charset){
		List<byte[]> result = new ArrayList<>();
		String name = file.getName();
		String fileName = file.getFileName();
		byte[] data = file.getData();
		
		if(isUrl) {
			try {
				fileName = URLEncoder.encode(fileName,charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(BOUNDARY).append(CRLF)
		.append("Content-Disposition:form-data;name=\"").append(name).append("\"")
		.append(";filename=\"").append(fileName).append("\"").append(CRLF)
		.append("Content-Type:application/octet-stream").append(CRLF)
		.append("Content-Transfer-Encoding:binary").append(CRLF)
		.append(CRLF);
		
		byte[] b = null;
		byte[] b1 = null;
		try {
			b = sb.toString().getBytes(charset);
			b1 = CRLF.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		
		result.add(b);
		result.add(data);
		result.add(b1);

		return result;
	}
	
	/**
	 * 结束分界符。
	 * 网络上的结束分界符是这样的格式，--boundary--，多了--后缀，postman上没有，我估计后缀不需要
	 * @param charset
	 * @return
	 */
	public static byte[] endBoundary(String charset) {
		String s = BOUNDARY + CRLF;
		byte[] b = null;
		try {
			b = s.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return b;
	}
	
	/**
	 * 获取文件名称
	 * @param conn HttpURLconnection对象
	 * @param charset 字符集
	 * @param isUrl 是否URL编码
	 * @return
	 */
	public static String getRespFileName(HttpURLConnection conn,String charset,boolean isUrl) {
		//如果有文件属性消息头，获取文件名称
		String disp = conn.getHeaderField(CONTENT_DISP);
		String fn = "filename=";
		String fileName = null;
		if(disp != null && disp.contains(fn)) {
			fileName = disp.substring(disp.indexOf(fn) + fn.length());
			
			if(fileName.trim().length() > 0 && isUrl) {
				try {
					if(charset == null) {
						charset = CHARSET_UTF8;
					}
					fileName = URLDecoder.decode(fileName,charset);
				} catch (UnsupportedEncodingException e) {
					fileName = "decodeCharsetError";
				}
			}
		}
		
		return fileName;
	}
	
	/**
	 * InputStream转byte[]
	 * @param in
	 * @return
	 */
	public static byte[] inputToByte(InputStream in) {
		byte[] result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len;
		try {
			while((len = in.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			result = baos.toByteArray();
		} catch (IOException e) {
			return null;
		} finally {
			//输入输出流养成关闭习惯。
			//虽然ByteArrayOutputStream/ByteInputStream是对内存访问的流，关闭是由JVM管理的，无需手动close()，close()也是空实现。
			if(baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
		
	}
	
	/**
	 * 输入流转字符串
	 * @param in
	 * @param charset
	 * @return
	 */
	public static String inputToString(InputStream in,String charset) {
		String result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len;
		try {
			while((len = in.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			result = baos.toString(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			return null;
		} finally {
			if(baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * 字节转输入流<br>
	 * 作用直接操作byte内存数据，还可以通过DataInputStream包装类读取成各种数据类型<br>
	 * https://zhidao.baidu.com/question/380762193.html<br>
	 * https://blog.csdn.net/miachen520/article/details/51584305<br>
	 * https://www.xuebuyuan.com/3257270.html<br>
	 * @param b
	 * @return
	 */
	public static InputStream byteToInput(byte[] b) {
		return new ByteArrayInputStream(b);
	}
	
	/**
	 * 字节转字符串
	 * @param b
	 * @param charset
	 * @return
	 */
	public static String byteToString(byte[] b,String charset) {
		String s = null;
		try {
			s = new String(b,charset);
		} catch (UnsupportedEncodingException e) {
			s = "charsetError";
		}
		return s;
	}
}

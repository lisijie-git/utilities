package com.lisijietech.utilities;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * RSA非对称加密算法<br>
 * https://blog.csdn.net/u013314786/article/details/80324461<br>
 * https://www.cnblogs.com/nick-huang/p/5066574.html
 * @author lisijie
 *
 */
public class RSAUtils {
	//非对称加密算法
	private static final String KEY_ALGORITHM = "RSA";
	//密钥长度，在512到65536位之间，建议不要太长，否则速度很慢，生成的加密数据很长。
	//虽然512位不建议使用了，但是仅学习而且服务器配置低，尽量少消耗性能
	private static final int KEY_SIZE = 512;
	//字符编码
	private static final String CHARSET = "UTF-8";
	//签名的相关算法，就是用了md5 和 rsa
	private static final String SIGNATURE_ALGORITHM = "MD5withRSA";
	
	/**
	 * 生成密钥对
	 * @return
	 * @throws Exception 
	 */
	public static KeyPair getKeyPair() throws Exception {
		return getKeyPair(null);
	}
	
	/**
	 * 生成密钥对
	 * @param seed 随机数种子，密钥的随机源。最好系统生成，不建议自定义随机种子
	 * @return
	 * @throws Exception
	 */
	public static KeyPair getKeyPair(String seed) throws Exception {
		//实例化密钥生成器
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		//初始化密钥生成器
		if(seed == null) {
			keyPairGenerator.initialize(KEY_SIZE);
		}else {
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
//			SecureRandom secureRandom = new SecureRandom();
			secureRandom.setSeed(seed.getBytes(CHARSET));
			keyPairGenerator.initialize(KEY_SIZE, secureRandom);
		}
		return keyPairGenerator.generateKeyPair();
	}
	
	/**
	 * 获得私钥
	 * @param keyPair
	 * @return 字节数组
	 */
	public static byte[] getPrivateKeyBytes(KeyPair keyPair) {
		return keyPair.getPrivate().getEncoded();
	}
	
	/**
	 * 获得私钥
	 * @param keyPair
	 * @return Base64编码
	 */
	public static String getPrivateKey(KeyPair keyPair) {
		return Base64.getEncoder().encodeToString(getPrivateKeyBytes(keyPair));
	}
	
	/**
	 * 获得公钥
	 * @param keyPair
	 * @return 字节数组
	 */
	public static byte[] getPublicKeyBytes(KeyPair keyPair) {
		return keyPair.getPublic().getEncoded();
	}
	
	/**
	 * 获得公钥
	 * @param keyPair
	 * @return Base64编码
	 */
	public static String getPublicKey(KeyPair keyPair) {
		return Base64.getEncoder().encodeToString(getPublicKeyBytes(keyPair));
	}
	
	/**
	 * 私钥加密。分段加密<br>
	 * 数据加密是按块加密，数据长度超过限制会报错，数据长度少于会默认填充（密钥位长度和padding填充模式决定数据加密长度）
	 * 所以长数据需要分段加密，然后按顺序累加<br>
	 * 加密后返回的密文数据是定长的，就是密钥位长度<br>
	 * 分段加密代码主要参照 https://blog.csdn.net/baidu_22254181/article/details/82594072<br>
	 * https://www.cnblogs.com/foxting/p/9896325.html<br>
	 * https://blog.csdn.net/luoluo_onion/article/details/78354799<br>
	 * https://www.cnblogs.com/lzl-sml/p/3501447.html<br>
	 * http://blog.sina.com.cn/s/blog_c58f4ee50102x7ml.html
	 * @param data 字节数组
	 * @param privateKey 字节数组
	 * @return 返回字节数组
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data,byte[] privateKey) throws Exception {
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//生成私钥对象,强转RSAPublicKey是为了可以获取key的密钥长度（模值）
		RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
		
		//数据加密，多组加密解密处理，算法会限制单次加密数据长度，超过会报错
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
				
		//Cipher类的padding默认填充占11字节，所以分段大小要减去11字节
		int size = key.getModulus().bitLength() / 8 - 11;
		
		int dataLen = data.length;
		
		//给字节数组输出流初始化缓冲区，
		//(dataLen + size - 1)这段确定data能分多少组，
		//dataLen/size刚好除尽，+size-1被忽略，有余数，+size-1刚好并且最多能让商多1
		ByteArrayOutputStream baos = 
				new ByteArrayOutputStream((dataLen + size - 1) / size * (size + 11));
		
		int remainder = 0;
		for(int i = 0;i < dataLen;) {
			remainder = dataLen - i;
			if(remainder > size) {
				cipher.update(data, i, size);
				i += size;
			}else {
				cipher.update(data, i, remainder);
				i += remainder;
			}
			baos.write(cipher.doFinal());
		}
		return baos.toByteArray();
	}
	
	/**
	 * 私钥加密
	 * @param data
	 * @param privateKey Base64编码
	 * @return Base64编码
	 * @throws Exception
	 */
	public static String encryptByPrivateKey(String data,String privateKey) throws Exception {
		byte[] key = Base64.getDecoder().decode(privateKey);
		return Base64.getEncoder().encodeToString(encryptByPrivateKey(data.getBytes(CHARSET),key));
	}
	
	/**
	 * 公钥加密。分段加密
	 * @param data 字节数组
	 * @param publicKey 字节数组
	 * @return 字节数组
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data,byte[] publicKey) throws Exception {
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//生成私钥对象
		RSAPublicKey key = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
		//数据加密，多组加密解密处理，算法会限制单次加密数据长度，超过会报错
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		int size = key.getModulus().bitLength() / 8 - 11;
		
		int dataLen = data.length;
		
		ByteArrayOutputStream baos = 
				new ByteArrayOutputStream((dataLen + size - 1) / size * (size + 11));
		
		int remainder = 0;
		for(int i = 0;i < dataLen;) {
			remainder = dataLen - i;
			if(remainder > size) {
				cipher.update(data, i, size);
				i += size;
			}else {
				cipher.update(data, i, remainder);
				i += remainder;
			}
			baos.write(cipher.doFinal());
		}
		return baos.toByteArray();
	}
	
	/**
	 * 公钥加密
	 * @param data
	 * @param publicKey Base64编码
	 * @return Base64编码
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String data,String publicKey) throws Exception {
		byte[] key = Base64.getDecoder().decode(publicKey);
		return Base64.getEncoder().encodeToString(encryptByPublicKey(data.getBytes(CHARSET),key));
	}
	
	/**
	 * 私钥解密。分段解密，由于密文长度是可确定的，和密钥长度（模值）相等，所以解密时分段判断有所变化
	 * @param data 字节数组
	 * @param privateKey 字节数组
	 * @return 字节数组
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data,byte[] privateKey) throws Exception {
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//生成私钥对象
		RSAPrivateKey key = (RSAPrivateKey)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
		//数据解密，多组加密解密处理，算法会限制单次加密数据长度，超过会报错
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		//加密数据分段大小和密钥长度一样，只需转换成字节长度
		int size = key.getModulus().bitLength() / 8;
		
		int dataLen = data.length;
		
		//加密数据由于有padding模式填充，所以比原来数据长，字节缓冲初始化和加密时的不一样
		ByteArrayOutputStream baos = 
				new ByteArrayOutputStream((dataLen + size - 1) / size * (size - 11));
		
		int remainder = 0;
		for(int i = 0;i < dataLen;) {
			remainder = dataLen - i;
			if(remainder > size) {
				cipher.update(data, i, size);
				i += size;
			}else {
				cipher.update(data, i, remainder);
				i += remainder;
			}
			baos.write(cipher.doFinal());
		}
		return baos.toByteArray();
	}
	
	/**
	 * 私钥解密
	 * @param data Base64编码
	 * @param privateKey Base64编码
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String data,String privateKey) throws Exception {
		byte[] key = Base64.getDecoder().decode(privateKey);
		return new String(decryptByPrivateKey(Base64.getDecoder().decode(data),key),CHARSET);
	}
	
	/**
	 * 公钥解密。分段解密，由于密文长度是可确定的，和密钥长度（模值）相等，所以解密时分段判断有所变化
	 * @param data 字节数组
	 * @param privateKey 字节数组
	 * @return 字节数组
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data,byte[] publicKey) throws Exception {
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//生成公钥对象
		RSAPublicKey key = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
		
		//数据解密，多组加密解密处理，算法会限制单次加密数据长度，超过会报错
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		int size = key.getModulus().bitLength() / 8;
		
		int dataLen = data.length;
		
		ByteArrayOutputStream baos = 
				new ByteArrayOutputStream((dataLen + size - 1) / size * (size - 11));
		
		int remainder = 0;
		for(int i = 0;i < dataLen;) {
			remainder = dataLen - i;
			if(remainder > size) {
				cipher.update(data, i, size);
				i += size;
			}else {
				cipher.update(data, i, remainder);
				i += remainder;
			}
			baos.write(cipher.doFinal());
		}
		return baos.toByteArray();
	}
	
	/**
	 * 公钥解密
	 * @param data Base64编码
	 * @param privateKey Base64编码
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPublicKey(String data,String publicKey) throws Exception {
		byte[] key = Base64.getDecoder().decode(publicKey);
		return new String(decryptByPublicKey(Base64.getDecoder().decode(data),key),CHARSET);
	}
	
	/**
	 * 对数据生成数字签名。<br>
	 * 签名一般用私钥<br>
	 * 签名实现原理：
	 * 用摘要算法对数据进行摘要处理（类似对数据进行不可逆压缩或一对一映射），
	 * 再用非对称加密算法对摘要进行私钥加密，保证数据是私钥拥有者的，且传输中没有被篡改。
	 * @param data 待签名数据，数据一般是字符串，如文章，在方法外转换成byte[]就行
	 * @param keyStr 一般是Base64编码的私钥字符串
	 * @return Base64编码的数字签名
	 * @throws Exception
	 */
	public static String signature(byte[] data,String keyStr) throws Exception {
		//Base64解码密钥
		byte[] keyBtyes = Base64.getDecoder().decode(keyStr);
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//生成私钥对象
		PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBtyes));
		
		//用私钥对信息生成数字签名
		Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
		sign.initSign(privateKey);
		sign.update(data);
		return Base64.getEncoder().encodeToString(sign.sign());
	}
	
	/**
	 * 校验数字签名。<br>
	 * 一般用公钥校验<br>
	 * 校验原理：
	 * 对校验数据进行摘要算法处理得到摘要sign1，再对签名用公钥解密得到摘要sign2，sign1和sign2对比相等则校验通过
	 * @param data 待校验数据
	 * @param keyStr Base64编码的公钥字符串
	 * @param signature Base64编码的数字签名
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data,String keyStr,String signature) throws Exception {
		//Base64解码密钥
		byte[] keyBtyes = Base64.getDecoder().decode(keyStr);
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//生成公钥对象
		PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBtyes));
		
		//用公钥对信息和签名进行验证
		Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
		sign.initVerify(publicKey);
		sign.update(data);
		return sign.verify(Base64.getDecoder().decode(signature));
	}
	
//	public static void main(String[] args) throws Exception {
////		得到密钥对
//		KeyPair keyPair = getKeyPair();
////		得到私钥base64字符串
//		String privateKey = getPrivateKey(keyPair);
//		System.out.println("privateKey:"+privateKey);
//		System.out.println(privateKey.length());
//		System.out.println(Base64.getDecoder().decode(privateKey).length);
////		得到公钥base64字符串
//		String publicKey = getPublicKey(keyPair);
//		System.out.println("publicKey:"+publicKey);
//		System.out.println(publicKey.length());
//		System.out.println(Base64.getDecoder().decode(publicKey).length);
//		
////		密钥加密
//		String data = "sldjfsldfldslkfjdlskjflsjfldsjfe是两块多家分店来看所肩负的萨菲罗斯登录类似地方了上岛咖啡老师的发"
//				+ "lsdjflksdjflsdlkfdjlkjk谁离开的解放路口甲方第十六届非asd我鞋机为容量看上课了坚实的哦违法啊啊啊啊啊啊";
////		data = "2";
////		私钥分段加密
//		String enPrivateData = encryptByPrivateKey(data, privateKey);
//		System.out.println("enPrivateData:"+enPrivateData);
//		System.out.println(enPrivateData.length());
//		System.out.println(Base64.getDecoder().decode(enPrivateData).length);
////		公钥分段加密
//		String enPublicData = encryptByPublicKey(data, publicKey);
//		System.out.println("enPublicData:"+enPublicData);
//		System.out.println(enPublicData.length());
//		System.out.println(Base64.getDecoder().decode(enPublicData).length);
//		
////		私钥分段解密
//		String dePrivateData = decryptByPrivateKey(enPublicData, privateKey);
//		System.out.println("dePrivateData:"+dePrivateData);
//		
////		公钥分段解密
//		String dePublicData = decryptByPublicKey(enPrivateData, publicKey);
//		System.out.println("dePublicData:"+dePublicData);
//		
////		私钥数据签名
//		String sign = signature(data.getBytes(),privateKey);
//		System.out.println("sign:"+sign);
////		验证签名
//		String falseDate = "sdfsdfdf";
//		boolean signVerify = verify(falseDate.getBytes(),publicKey, sign);
//		System.out.println("signVerify:"+signVerify);
//	}

}

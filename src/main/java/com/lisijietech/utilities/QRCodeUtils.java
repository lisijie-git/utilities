package com.lisijietech.utilities;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 二维码工具类，zxing的二维码<br>
 * 二维码生成:https://www.jianshu.com/p/89c8c24281bd<br>
 * https://www.cnblogs.com/lanxiamo/p/6293580.html<br>
 * https://www.cnblogs.com/haha12/p/6198104.html<br>
 * https://blog.csdn.net/weixin_39220472/article/details/81953121<br>
 * 很好的抽象分离规范：<br>
 * https://www.cnblogs.com/ruiati/p/5757407.html<br>
 * https://www.cnblogs.com/lanxiamo/p/6293580.html<br>
 * 详解:https://www.jianshu.com/p/6607e69b1121
 * @author lisijie
 *
 */
public class QRCodeUtils {
	private static final String CHARSET = "UTF-8";
	private static final String FORMAT = "JPG";
	//二维码尺寸
	private static final int QRCODE_SIZE = 300;
	//logo宽度
	private static final int LOGO_WIDTH = 60;
	//logo高度
	private static final int LOGO_HEIGHT = 60;
	
	/**
	 * 生成二维码缓冲图像，可带logo
	 * @param content 二维码内容
	 * @param logoPath logo地址
	 * @param needCompress 是否压缩logo
	 * @return 缓存图像
	 * @throws WriterException
	 * @throws IOException 
	 */
	public static BufferedImage createImage(String content,String logoPath,boolean needCompress) throws WriterException, IOException {
		//Hashtable是否可以用ConcurrentHashMap替代
		Hashtable<EncodeHintType,Object> hints = new Hashtable<>();
		hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET,CHARSET);
		hints.put(EncodeHintType.MARGIN,1);
//		BitMatrix bitMatrix = new QRCodeWriter().encode(content,BarcodeFormat.QR_CODE,QRCODE_SIZE,
//				QRCODE_SIZE,hints);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE,QRCODE_SIZE,
				QRCODE_SIZE,hints);
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int x = 0;x < width;x++) {
			for(int y = 0;y < height;y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
			}
		}
		if(logoPath == null || logoPath.length() == 0) {
			return image;
		}
		//插入logo图片
		QRCodeUtils.insertImage(image, logoPath, needCompress);
		return image;
	}
	
	/**
	 * 生成二维码缓冲图像，不带logo，自定义宽高<br>
	 * 就是上面的方法，内部再抽象分离方法
	 * @param content
	 * @param width
	 * @param height
	 * @return
	 * @throws WriterException
	 */
	public static BufferedImage createImage(String content,Integer width,Integer height) throws WriterException {
		BitMatrix bitMatrix = createBitMatrix(content,width,height);
		BufferedImage image = toBufferedImage(bitMatrix);
//		insertImage(image, null, false);
		return image;
	}
	
	/**
	 * 生成二维码位矩阵
	 * @param content
	 * @param width
	 * @param height
	 * @return
	 * @throws WriterException
	 */
	public static BitMatrix createBitMatrix(String content,Integer width,Integer height) throws WriterException {
		if(width == null || width < 300) {
			width = 300;
		}
		if(height == null || height < 300) {
			height = 300;
		}
		//Hashtable可以用ConcurrentHashMap替代，这里并没有涉及到多线程操作map，hashMap代替也可以
		//二维码配置
		Hashtable<EncodeHintType,Object> hints = new Hashtable<>();
		hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET,CHARSET);
		hints.put(EncodeHintType.MARGIN,1);
//		BitMatrix bitMatrix = new QRCodeWriter().encode(content,BarcodeFormat.QR_CODE,width,height,hints);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE,width,height,hints);
		return bitMatrix;
	}
	
	/**
	 * 根据位矩阵，画出黑白二维码缓冲图片
	 * @param bitMatrix
	 * @return
	 */
	public static BufferedImage toBufferedImage(BitMatrix bitMatrix) {
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int x = 0;x < width;x++) {
			for(int y = 0;y < height;y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
			}
		}
		return image;
	}
	
	/**
	 * 插入logo
	 * @param source 二维码图片
	 * @param logoPath logo图片地址
	 * @param needCompress 是否压缩
	 * @throws IOException
	 */
	public static void insertImage(BufferedImage source,String logoPath,boolean needCompress) throws IOException {
		File file = new File(logoPath);
		if(!file.exists()) {
			return;
		}
		Image src = ImageIO.read(file);
		int width = src.getWidth(null);
		int height = src.getHeight(null);
		if(needCompress) {//压缩logo
			if(width > LOGO_WIDTH) {
				width = LOGO_WIDTH;
			}
			if(height > LOGO_HEIGHT) {
				height = LOGO_HEIGHT;
			}
			Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			src = image;
		}
		//插入logo
		Graphics2D graph = source.createGraphics();
		int x = (QRCODE_SIZE - width) / 2;
		int y = (QRCODE_SIZE - height) / 2;
		graph.drawImage(src,x,y,width,height,null);
		Shape shape = new RoundRectangle2D.Float(x, y, width, height, 6, 6);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}
	
	/**
	 * 将字符串内容生成二维码图像，写入到输出流中（可带logo）
	 * @param content
	 * @param needCompress
	 * @param output 是文件输出流，就生成文件，如new FileOutputStream(new File("d:\\qrcode.jpg"))<br>
	 * 	是OutputStream out = httpServletResponse.getOutputStream()就会返回给前端
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public static boolean createToStream(String content,String logoPath,OutputStream output) throws WriterException, IOException {
		BufferedImage image = createImage(content,logoPath,true);
//		MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, path);zxing自带生成文件
		ImageIO.write(image, FORMAT, output);
		return true;
	}
	/**
	 * 将字符串内容生成二维码图像，写入到输出流中（不带logo）
	 * @param content
	 * @param output
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public static boolean createToStream(String content,OutputStream output) throws WriterException, IOException {
		BufferedImage image = createImage(content,QRCODE_SIZE,QRCODE_SIZE);
		ImageIO.write(image, FORMAT, output);
		return true;
	}
	
	/**
	 * 解析二维码
	 * @param image 缓存图片流
	 * @return
	 * @throws NotFoundException
	 */
	public static String decode(BufferedImage image) throws NotFoundException{
		if(image == null) {
			return null;
		}
		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Map<DecodeHintType, String> hints = new HashMap<>();
		hints.put(DecodeHintType.CHARACTER_SET,CHARSET);
		Result result = new MultiFormatReader().decode(bitmap,hints);
		String resultStr = result.getText();
//		String encode = result.getBarcodeFormat().toString();
		return resultStr;
	}
	
	/**
	 * 从输入流中解码，得到原始字符串
	 * @param content
	 * @param input 获取二维码文件，进行解码，new FileInputStream(new File("d:\\qrcode.jpg"))
	 * @return
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public static String decodeFromStream(InputStream input) throws IOException, NotFoundException {
		BufferedImage image = ImageIO.read(input);
		String str = decode(image);
		return str;
	}
	
//	public static void main(String[] args) throws WriterException, IOException, NotFoundException {
//		String s = "http://www.lisijietech.com";
//		String logoPath = "C:\\Users\\ASUS\\Desktop\\owner.jpg";
//		String qrcodeLogo = "C:\\Users\\ASUS\\Desktop\\logo二维码.jpg";
//		String qrcode = "C:\\Users\\ASUS\\Desktop\\二维码.jpg";
//		File logoFile = new File(qrcodeLogo);
//		File file = new File(qrcode);
////		FileOutputStream fosLogo = new FileOutputStream(logoFile);
////		FileOutputStream fos = new FileOutputStream(file);
////		createToStream(s,logoPath,fosLogo);
////		createToStream(s,fos);
////		fosLogo.close();
////		fos.close();
//		
//		FileInputStream fisLogo = new FileInputStream(logoFile);
//		FileInputStream fis = new FileInputStream(file);
//		String logoStr = decodeFromStream(fisLogo);
//		System.out.println("带有logo的二维码解码："+logoStr);
//		String str = decodeFromStream(fis);
//		System.out.println("二维码解码："+str);
//		fisLogo.close();
//		fis.close();
//	}

}

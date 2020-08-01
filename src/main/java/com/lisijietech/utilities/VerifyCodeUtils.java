package com.lisijietech.utilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * 验证码工具类<br>
 * 参考:<br>
 * https://www.cnblogs.com/feiyun126/p/4081254.html<br>
 * https://www.cnblogs.com/nanyangke-cjz/p/7049281.html<br>
 * https://www.cnblogs.com/jianlun/articles/5553452.html<br>
 * 绘图drawString位置的确定：<br>
 * https://www.cnblogs.com/hdwang/p/10157419.html，这个网站的FontDesignMetrics类不存在，原理参考<br>
 * https://www.cnblogs.com/tianzhijiexian/p/4297664.html<br>
 * https://blog.csdn.net/w410589502/article/details/72898184<br>
 * https://blog.csdn.net/yangwy012210/article/details/78867830<br>
 * @author lisijie
 *
 */
public class VerifyCodeUtils {
	// 图片宽度
	private int width = 120;
	// 图片高度
	private int height = 40;
	// 验证码字符个数
	private int codeCount = 4;
	// 验证码干扰线数
	private int lineCount = 20;
	// 验证码
	private String code = null;
	// 验证码图片Buffer
	private BufferedImage buffImg = null;
	Random random = new Random();

	public VerifyCodeUtils() {
		createImg();
	}

	public VerifyCodeUtils(int width, int height) {
		this.width = width;
		this.height = height;
		createImg();
	}

	public VerifyCodeUtils(int width, int height, int codeCount) {
		this.width = width;
		this.height = height;
		this.codeCount = codeCount;
		createImg();
	}

	public VerifyCodeUtils(int width, int height, int codeCount, int lineCount) {
		this.width = width;
		this.height = height;
		this.codeCount = codeCount;
		this.lineCount = lineCount;
		createImg();
	}

	private void createImg() {
//		int fontWidth = width / codeCount;// 字体宽度
//		int fontHeight = height - 5;// 字体高度，-5应该是给图片上下留白，参考中写的代码应该是fontSize
		//画字时要上下居中对齐要注意codeY是代表字符基线y坐标不是字符左上角y坐标，所以要修正。
		//但优化下，得到基线坐标计算对齐，不用一点一点调，codeY = topWhite + leading + ascent
//		int codeY = height - 8;
		
		//字体大小，应该由图像高度决定，字体大小值与字体高度值比例大约3:4，字体像素高度不能大于图像的像素高度
		int fontSize = height - 10;

		// 图像buffer
		buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Graphics g = buffImg.getGraphics();
		Graphics2D g = buffImg.createGraphics();
		// 设置背景颜色
		Color c = getRandColor(235, 250);
		g.setColor(c);
		g.fillRect(0, 0, width, height);

		// 设置字体
		Font font = getFont(fontSize);// 随机字体
		g.setFont(font);

		// 设置干扰线
		for (int i = 0; i < lineCount; i++) {
			int xs = random.nextInt(width);
			int ys = random.nextInt(height);
			int xe = xs + random.nextInt(width);
			int ye = ys + random.nextInt(height);
			g.setColor(getRandColor(180, 200));
			g.drawLine(xs, ys, xe, ye);
		}

		// 添加噪点
		float yawpRate = 0.01f;// 噪声率
		int area = (int) (yawpRate * width * height);
		for (int i = 0; i < area; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			buffImg.setRGB(x, y, random.nextInt(100) + 100);
		}

		// 是图片扭曲
		shear(g, width, height, c);

		// 画字
		String str = randomStr(codeCount);// 得到随机字符
		this.code = str;
		
		FontMetrics metrics = g.getFontMetrics(font);//得到相应字体衡量标准
		int leading = metrics.getLeading();//行距
		int ascent = metrics.getAscent();//字体基线上部分长度，用来修正字符左上角y坐标
		int descent = metrics.getDescent();//字体基线下部长度
		int fontHeight = metrics.getHeight();//字体高度，其实就等于leading + ascent + descent
		int strWidth = metrics.stringWidth(code);//字符串在当前字体设置下的advace width，即字符串宽度
//		System.out.println("设置的是："+fontHeight+" 字体高："+metrics.getHeight());
//		System.out.println("验证码字符串的宽度："+strWidth+" 第一个字符宽度："+metrics.getWidths()[0]);
//		System.out.println("metrics获取到的上坡长度："+ ascent +" 下坡长度："+ descent);
//		System.out.println("metrics获取到的行距："+ metrics.getLeading());
//		System.out.println("metrics获取"+code.charAt(0)+"的advance width："+ metrics.charWidth(code.charAt(0)));
		
		//字符水平居中的左边均分插入空白，如4个字符，有5处水平空白
		int leftAvgWhite = (width - strWidth) / (codeCount + 1);
		int topWhite = (height - fontHeight) / 2;//字符垂直居中的顶边空白
		int codeY = topWhite + leading + ascent;//垂直居中，修正基线y坐标
		for (int i = 0; i < codeCount; i++) {
			//总体水平居中，获取当前字符x坐标位置
			int codeX = leftAvgWhite * (i + 1) + (i > 0 ? metrics.stringWidth(code.substring(0, i)) : 0);
			// 字符变换
			AffineTransform affine = new AffineTransform();
//			affine.setToRotation(
//					Math.PI / 4 * random.nextDouble()
//							* (random.nextBoolean() ? 1 : -1),
//					(width / codeCount) * i + fontHeight / 2, height / 2);
			//旋转
			affine.setToRotation(
			Math.PI / 4 * random.nextDouble()
					* (random.nextBoolean() ? 1 : -1),
					codeX + 4, height / 2);
			g.setTransform(affine);
			// 画出不同颜色的字符
			String strRand = str.substring(i, i + 1);
			g.setColor(getRandColor(1,150));
			
			// g.drawString(a,x,y);
			// a为要画出来的东西，x和y表示要画的东西最左侧字符位置，y是文字基线位于此图形上下文坐标系y轴处
			g.drawString(strRand,codeX,codeY);
		}
	}

	/**
	 * 获得随机字符码
	 * 
	 * @param n 字符码长度
	 * @return
	 */
	private String randomStr(int n) {
		String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		StringBuffer strbuff = new StringBuffer("");
		int len = str.length();
		double r;
		for (int i = 0; i < n; i++) {
			r = (Math.random()) * len;
			strbuff.append(str.charAt((int) r));
		}
		return strbuff.toString();
	}

	/**
	 * 得到随即颜色
	 * 
	 * @param fc 颜色范围
	 * @param bc 颜色范围
	 * @return
	 */
	private Color getRandColor(int fc, int bc) {
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	/**
	 * 产生随机字体。
	 * 简化为一种字体，其他太难看，或者直观视觉重复
	 * 字体大小和字体高度关系：<br>
	 * https://bbs.csdn.net/topics/380075218<br>
	 * https://blog.csdn.net/kerwin612/article/details/24958761<br>
	 * https://zhidao.baidu.com/question/585109684.html<br>
	 * @param size 字体大小。字体大小值与字体高度值大约比例3/4，具体自己试验，做修正
	 * @return
	 */
	private Font getFont(int size) {
//		Random random = new Random();
//		Font[] font = new Font[4];
//		font[0] = new Font("Ravie", Font.PLAIN, size);
//		font[1] = new Font("Antique Olive Compact", Font.PLAIN, size);
//		font[2] = new Font("Fixedsys", Font.PLAIN, size);
//		font[3] = new Font("Wide Latin", Font.PLAIN, size);
//		font[4] = new Font("Gill Sans Ultra Bold",Font.PLAIN,size);
//		return font[random.nextInt(4)];
		return new Font("Fixedsys", Font.PLAIN, size);
	}

	private void shearX(Graphics g, int w1, int h1, Color color) {
		int period = random.nextInt(2);
		boolean borderGap = true;
		int frames = 1;
		int phase = random.nextInt(2);

		for (int i = 0; i < h1; i++) {
			double d = (double) (period >> 1)
					* Math.sin((double) i / (double) period
							+ (6.2831853071795862D * (double) phase)
							/ (double) frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}
	}

	private void shearY(Graphics g, int w1, int h1, Color color) {
		int period = random.nextInt(40) + 10;
		boolean borderGap = true;
		int frames = 20;
		int phase = 7;

		for (int i = 0; i < w1; i++) {
			double d = (double) (period >> 1)
					* Math.sin((double) i / (double) period
							+ (6.2831853071795862D * (double) phase)
							/ (double) frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}
		}
	}

	/**
	 * 扭曲方法
	 * 
	 * @param g 制图
	 * @param w1 图片宽
	 * @param h1 图片高
	 * @param color 背景颜色
	 */
	private void shear(Graphics g, int w1, int h1, Color color) {
		shearX(g, w1, h1, color);
		shearY(g, w1, h1, color);
	}

	/**
	 * buffImg写到输出流中
	 * 
	 * @param sos
	 * @throws IOException
	 */
	public void write(OutputStream sos) throws IOException {
		// ImageIO的write()方法写入操作完成后不会关闭提供的OutputStream，需要调用负责者关闭
		ImageIO.write(buffImg, "png", sos);
		sos.close();
	}

	public BufferedImage getBuffImg() {
		return buffImg;
	}

	/**
	 * 得到小写的验证码字符串
	 * 
	 * @return
	 */
	public String getLowerCode() {
		return code.toLowerCase();
	}

}

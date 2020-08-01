package com.lisijietech.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 反射工具类<br>
 * 反射通过类名获取class<br>
 * https://www.cnblogs.com/it-taosir/p/10635978.html<br>
 * https://www.cnblogs.com/interdrp/p/7071927.html<br>
 * 反射获取实例对象<br>
 * https://www.cnblogs.com/wilwei/p/10242618.html<br>
 * https://blog.csdn.net/qq_37465638/article/details/86631726<br>
 * https://blog.csdn.net/snake568904758/article/details/60141860<br>
 * https://blog.csdn.net/weixin_30439031/article/details/97469383<br>
 * 反射操作field，method,参数化类型(泛型)等<br>
 * https://www.jianshu.com/p/4538b1adf02a<br>
 * https://blog.csdn.net/qq_32718869/article/details/81288076<br>
 * https://blog.csdn.net/xiaozaq/article/details/52329321<br>
 * https://blog.csdn.net/JustBeauty/article/details/81116144<br>
 * https://www.jianshu.com/p/0f3eda48d611<br>
 * https://blog.csdn.net/u011572579/article/details/46395667<br>
 * 类型动态转换<br>
 * https://blog.csdn.net/andyhan_1001/article/details/35568253<br>
 * https://zhidao.baidu.com/question/537410239.html<br>
 * 获取包名下所有类名<br>
 * https://blog.csdn.net/aust_glj/article/details/53385651<br>
 * @author lisijie
 *
 */
public class ReflectUtils {
	/**
	 * 创建对象，通过类全名。
	 * 此方法是调用无参构造方法获取到实例对象，因此类必须要有无参构造函数，否则会报异常。
	 * @param className 类全名
	 * @return
	 */
	public static Object createObject(String className) {
		Object obj = null;
		try {
			Class<?> c = Class.forName(className);
			obj = c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * 创建对象，通过类全名和构造器参数对象数组
	 * @param className
	 * @param params
	 * @return
	 */
	public static Object createObject(String className,Object[] params) {
		if(params == null || params.length == 0) {
			return null;
		}
		
		Object obj = null;
		int len = params.length;
		
		Class<?>[] paramsTypes = new Class[len];
		for(int i = 0;i < len;i++) {
			paramsTypes[i] = params[i].getClass();
		}
		
		try {
			Class<?> c = Class.forName(className);
			//获所有取构造器，返回数组
			//Constructor<?>[] constructors = c.getDeclaredConstructors();
			//通过遍历构造器数组，并对比参数列表类型，确定获取哪一个构造器。类似于重载方法运行时才确定一样。
			//Class<?> consClazzs = constructor.getParameterTypes();
			//consClazzs和paramsTypes比较。用各自的class.getName()比较是不是同一种类型。
			//但是效率可能比较低，而且java也给出了根据参数类型获取构造器的方法。所以只是一种解决思路。
			
			Constructor<?> constructor = c.getDeclaredConstructor(paramsTypes);
			obj = constructor.newInstance(params);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
//	https://www.jianshu.com/p/4538b1adf02a
//	对上面的代码做了一些改造，和一些错误的修改
//	https://www.cnblogs.com/maokun/p/6773203.html
//	当实体对象的超类Class是表示Object类，一个接口，一个基本类型或 void，就会返回null。超类Class是数组类，会返回Object的Class对象。
	
	/**
	 * 获取类的Field。无论什么访问修饰符。
	 * 如果当前类型没有，就循环获取父类的DeclaredField。
	 * @param c 类型类
	 * @param name 属性名称
	 * @return
	 */
	public static Field getDeclaredField(Class<?> c,String name) {
		Class<?> currentClass = c;
		//不能仅仅用currentClass != Object.class判断是否已经是没有父类了。
		//只有数组类型调用getSuperclass方法会返回Object.class。
		while(currentClass == null || currentClass != Object.class) {
			try {
				return currentClass.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			//不在当前Class，赋值为父类Class，继续查询。
			currentClass = currentClass.getSuperclass();
		}
		//访问父类到Object.class，就代表没有此name的field。
		return null;
	}
	
	/**
	 * 设field变为可访问的。
	 * 不是public修饰的需要设置可访问，不然会抛异常
	 * @param f 属性
	 */
	public static void makeAccessible(Field f) {
		//判断field修饰符是否是公开访问的
		if(!Modifier.isPublic(f.getModifiers())) {
			f.setAccessible(true);
		}
	}
	
	/**
	 * 获取对象属性的值，无视访问修饰符(private,default,protected)。
	 * @param obj
	 * @param name
	 * @return
	 */
	public static Object getFieldValue(Object obj,String name) {
		Field f = getDeclaredField(obj.getClass(), name);
		
		if(f == null) {
			//没有此属性，要么抛异常上层处理，要么就在这里处理。
			return null;
		}
		
		makeAccessible(f);
		
		Object result = null;
		try {
			result = f.get(obj);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	/**
	 * 设置对象属性的值，无视访问修饰符(private,default,protected)。
	 * @param obj
	 * @param name
	 * @param value
	 */
	public static void setFieldValue(Object obj,String name,Object value) {
		Field f = getDeclaredField(obj.getClass(), name);
		
		if(f == null) {
			//没有此属性，要么抛异常上层处理，要么就在这里处理。
			throw new IllegalArgumentException("Could find field ["+name+"]");
		}
		
		makeAccessible(f);
		
		try {
			f.set(obj, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 获取类的method。无论什么访问修饰符。
	 * 如果当前类型没有，就循环获取父类的DeclaredMethod。
	 * @param c 类型类
	 * @param name 方法名称
	 * @param parameterTypes 方法参数类型数组
	 * @return
	 */
	public static Method getDeclaredMethod(Class<?> c,String name,Class<?>[] parameterTypes) {
		Class<?> currentClass = c;
		while(currentClass == null || currentClass != Object.class) {
			try {
				return currentClass.getDeclaredMethod(name, parameterTypes);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			//不在当前Class，赋值为父类Class，继续查询
			currentClass = currentClass.getSuperclass();
		}
		//访问父类到Object.class，就代表没有此name和parameterTypes的Method。
		return null;
	}
	
	/**
	 * 调用对象的方法，无视访问修饰符(private,default,protected)。
	 * 如果obj对象参数为null,则是调用静态方法。
	 * @param obj 对象实例
	 * @param methodName 方法名
	 * @param parameterTypes 方法参数类型数组
	 * @param parameters 方法参数对象数组
	 * @return
	 */
	public static Object invokeMethod(Object obj,String methodName,Class<?>[] parameterTypes,Object[] parameters) {
		
		Method m = getDeclaredMethod(obj.getClass(), methodName, parameterTypes);
		
		if(m == null) {
			//没有此方法，要么抛异常上层处理，要么就在这里处理。
			//throw new IllegalArgumentException("Could find method ["+methodName+"]");这是运行时异常，可以不被捕获，不声明
			return null;
		}
		//设置为可访问，就不被访问修饰符限制
		m.setAccessible(true);
		
		Object result = null;
		try {
			result = m.invoke(obj, parameters);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 获得具体继承的父类泛型参数Class数组。
	 * 是extends Parent<String,String>中的泛型参数Class数组，而非编写定义的父类Parent<K,V>中的泛型
	 * @param c
	 * @return
	 */
	public static Class<?>[] getSuperClassGenericity(Class<?> c){
		//获取泛型父类，必须用getGenericSuperclass方法。
		//获取具体继承的父类，泛型类型有可能已经确定了Son extends Parent<String>。而非编写定义的父类如Parent<T>。
		Type t = c.getGenericSuperclass();
		
		//class1.isAssignableFrom(class2)，判断class2是class1的子类
		//也可以用 if(!(t instanceof ParameterizedType))关键字
		if(!(t.getClass().isAssignableFrom(ParameterizedType.class))) {
			return null;
		}
		
		//获取参数化类型的实际类型参数。就是获取Type类的泛型参数数组，数组也是Type类。
		Type[] ts = ((ParameterizedType)t).getActualTypeArguments();
		
		Class<?>[] result = null;
		//不知道getActualTypeArguments方法会不会返回null
		if(ts != null && ts.length > 0) {
			//其他泛型new后面都不能加<?>，只能加<String>等确定类型，new Class<?>()就行，很神奇。
			result = new Class<?>[ts.length];
			for(int i = 0;i < ts.length;i++) {
				//这里要注意，Type可变参数 T,K,V是无法强转成Class，会报错。
				result[i] = (Class<?>)(ts[i]);
			}
		}
		
		return result;
	}
	
	/**
	 * 获得成员属性的泛型参数Class数组。
	 * @param c
	 * @return
	 */
	public static Class<?>[] getFieldGenericity(Field f){
		Type t = f.getGenericType();
		
		if(!(t.getClass().isAssignableFrom(ParameterizedType.class))) {
			return null;
		}
		
		Type[] ts = ((ParameterizedType)t).getActualTypeArguments();
		
		Class<?>[] result = null;
		if(ts != null && ts.length > 0) {
			result = new Class<?>[ts.length];
			for(int i = 0;i < ts.length;i++) {
				result[i] = (Class<?>)(ts[i]);
			}
		}
		
		return result;
	}
	
//	https://blog.csdn.net/hekewangzi/article/details/51969774
	/**
	 * 判断field是否是基础类型和void类型
	 * @param f
	 * @return
	 */
	public static boolean isBasicClassType(Field f) {
		return f.getType().isPrimitive();
	}
	
	/**
	 * 判断field是否是指定的类型
	 * @param f
	 * @param targetClass
	 * @return
	 */
	public static boolean isTargetClassType(Field f,Class<?> targetClass) {
		return f.getType() == targetClass;
	}
	
//	public static void main(String[] args) {
//		Object str = "sdf";
//		Object num = 123;
//		ScheduleInfoEntity info = new ScheduleInfoEntity();
//		Object entity = info;
//		System.out.println(str.getClass().getName());
//		System.out.println(num.getClass().getName());
//		System.out.println(entity.getClass().getName());
//		
//		try {
//			Class<?> c = Class.forName("com.lisijietech.crawler.utils.ParameterizedTest");
//			Field f = c.getDeclaredField("t");
//			Type t = f.getGenericType();
//			Type t = c.getGenericSuperclass();
//			System.out.println(t.getTypeName());
//			System.out.println(t.getClass().getName());
//			Type[] ts = ((ParameterizedType)t).getActualTypeArguments();
//			for(Type a : ts) {
//				System.out.println(a.getTypeName());
//				System.out.println(((Class<?>)a).getName());
//				System.out.println(a.getClass().getName());
//				System.out.println(a);
//			}
//			
//		} catch (ClassNotFoundException | SecurityException | NoSuchFieldException e) {
//			e.printStackTrace();
//		}
//	}

}

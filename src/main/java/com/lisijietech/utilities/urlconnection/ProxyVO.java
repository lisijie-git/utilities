package com.lisijietech.utilities.urlconnection;

/**
 * 代理请求数据对象。
 * @author lisijie
 *
 */
public class ProxyVO {
	//代理主机ip地址
	private String host;
	//代理主机端口
	private Integer port;
	//如果需要认证，认证用户名
	private String user;
	//如果需要认证，认证密码
	private String password;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}

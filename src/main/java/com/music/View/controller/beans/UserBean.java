package com.music.View.controller.beans;

public class UserBean {
	
	private String email = null;
	
	private final String ip;
	
	public UserBean(String ip) {
		this.ip = ip;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}


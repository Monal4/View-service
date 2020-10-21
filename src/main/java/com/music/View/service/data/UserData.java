package com.music.View.service.data;

import com.music.View.domain.User;

public class UserData {
	private long id;
	private String firstname;
	private String lastname;
	private String emailAddress;

	public UserData() {};
	
	public UserData(User u) {
		id = u.getId();
		firstname = u.getFirstname();
		lastname = u.getLastname();
		emailAddress = u.getEmailAddress();
	}
	
	public long getId() {
		return id;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
}

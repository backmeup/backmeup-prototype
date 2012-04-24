package org.backmeup.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
	@Id	
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long userId;
	private String username;
	private String password;
	private String keyRing;
	private String email;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKeyRing() {
		return keyRing;
	}

	public void setKeyRing(String keyRing) {
		this.keyRing = keyRing;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public User() {
	}
	
	public User(String username, String password, String keyRing, String email) {
		this(null, username, password, keyRing, email);
	}
	
	public User(Long userId, String username, String password, String keyRing, String email) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.keyRing = keyRing;
		this.email = email;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}

package com.cgi.training.bookmarksws;

import java.time.LocalDateTime;

public class Bookmark {
	private int id;
	private String description;
	private LocalDateTime timeStamp;
	
	private User user;
	private Site site;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "\nBookmark [id=" + id + ", user=" + user + ", site=" + site + ", description=" + description
				+ ", timeStamp=" + timeStamp + "]";
	}

}

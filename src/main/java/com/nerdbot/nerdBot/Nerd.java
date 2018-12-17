package com.nerdbot.nerdBot;

import net.dv8tion.jda.core.entities.User;

public class Nerd {
	public String user;
	public String id;
	public int points;
	
	public Nerd(String myUser, String myId, int myPoints){
		user = myUser;
		points = myPoints;
		id = myId;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	
	public String toString(){
		return this.getUser() + ":" + this.getPoints()+ ":" + this.getId() + "\r";
		
	}
}

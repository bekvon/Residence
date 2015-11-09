package com.bekvon.bukkit.residence.shopStuff;

public class ShopVote {

    String name = null;
    int vote = -1;

    public ShopVote(String name, int vote) {
	this.name = name;
	this.vote = vote;
    }

    public String getName() {
	return this.name;
    }

    public int getVote() {
	return this.vote;
    }

    public void setVote(int vote) {
	this.vote = vote;
    }
}

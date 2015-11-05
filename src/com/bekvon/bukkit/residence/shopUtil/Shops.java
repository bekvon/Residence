package com.bekvon.bukkit.residence.shopUtil;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class Shops {

    ClaimedResidence res = null;
    Location loc = null;

    public Shops(ClaimedResidence res) {
	this.res = res;
    }

    public ClaimedResidence getRes() {
	return this.res;
    }
}

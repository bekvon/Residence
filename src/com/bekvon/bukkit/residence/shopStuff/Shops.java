package com.bekvon.bukkit.residence.shopStuff;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class Shops {

    ClaimedResidence res = null;

    public Shops(ClaimedResidence res) {
	this.res = res;
    }

    public ClaimedResidence getRes() {
	return this.res;
    }
}

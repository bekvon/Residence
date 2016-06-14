package com.bekvon.bukkit.residence.signsStuff;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class Signs {

    int Category = 0;
    ClaimedResidence Residence = null;

    Location loc = null;

    public Signs() {
    }

    public void setLocation(Location loc) {
	this.loc = loc;
    }

    public Location GetLocation() {
	return this.loc;
    }

    public void setCategory(int Category) {
	this.Category = Category;
    }

    public int GetCategory() {
	return this.Category;
    }

    public void setResidence(ClaimedResidence Residence) {
	this.Residence = Residence;
    }

    public ClaimedResidence GetResidence() {
	return this.Residence;
    }

}

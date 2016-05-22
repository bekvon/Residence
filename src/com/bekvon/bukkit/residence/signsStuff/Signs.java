package com.bekvon.bukkit.residence.signsStuff;

import org.bukkit.Location;

public class Signs {

    int Category = 0;
    String Residence = null;

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

    public void setResidence(String Residence) {
	this.Residence = Residence;
    }

    public String GetResidence() {
	return this.Residence;
    }

}

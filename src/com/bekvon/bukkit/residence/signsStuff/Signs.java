package com.bekvon.bukkit.residence.signsStuff;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Signs {

    int Category = 0;
    String Residence = null;
    String world = null;
    double x = 0.01;
    double y = 0.01;
    double z = 0.01;

    Location loc = null;

    public Signs() {
    }

    public void setLocation(Location Location) {
	this.loc = Location;
    }

    public Location GetLocation() {
	if (this.loc == null) {
	    World w = Bukkit.getWorld(this.world);
	    if (w != null)
		this.loc = new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
	}
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

    public void setWorld(String World) {
	this.world = World;
    }

    public String GetWorld() {
	return this.world;
    }

    public void setX(double x) {
	this.x = x;
    }

    public double GetX() {
	return this.x;
    }

    public void setY(double y) {
	this.y = y;
    }

    public double GetY() {
	return this.y;
    }

    public void setZ(double z) {
	this.z = z;
    }

    public double GetZ() {
	return this.z;
    }

}

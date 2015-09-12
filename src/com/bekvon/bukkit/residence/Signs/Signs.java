package com.bekvon.bukkit.residence.Signs;

import org.bukkit.Location;

public class Signs {

    int Category = 0;
    String Residence = null;
    String World = null;
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
	this.World = World;
    }

    public String GetWorld() {
	return this.World;
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

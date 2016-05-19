package com.bekvon.bukkit.residence.shopStuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Board {

    String world = null;
    Integer tx = 0;
    Integer ty = 0;
    Integer tz = 0;
    Integer bx = 0;
    Integer by = 0;
    Integer bz = 0;

    Location TopLoc = null;
    Location BottomLoc = null;

    int StartPlace = 0;

    List<Location> Locations = new ArrayList<Location>();
    HashMap<String, Location> SignLocations = new HashMap<String, Location>();

    public Board() {
    }

    public void clearSignLoc() {
	SignLocations.clear();
    }

    public void addSignLoc(String resName, Location loc) {
	SignLocations.put(resName, loc);
    }

    public HashMap<String, Location> getSignLocations() {
	return SignLocations;
    }

    public Location getSignLocByName(String resName) {
	return SignLocations.get(resName);
    }

    public String getResNameByLoc(Location location) {
	for (Entry<String, Location> One : SignLocations.entrySet()) {
	    Location loc = One.getValue();
	    if (!loc.getWorld().getName().equalsIgnoreCase(location.getWorld().getName()))
		continue;
	    if (loc.getBlockX() != location.getBlockX())
		continue;
	    if (loc.getBlockY() != location.getBlockY())
		continue;
	    if (loc.getBlockZ() != location.getBlockZ())
		continue;
	    return One.getKey();
	}
	return null;
    }

    public List<Location> GetLocations() {
	Locations.clear();
	GetTopLocation();
	GetBottomLocation();

	if (TopLoc == null || BottomLoc == null)
	    return null;

	if (Bukkit.getWorld(world) == null)
	    return null;

	int xLength = tx - bx;
	int yLength = ty - by;
	int zLength = tz - bz;

	if (xLength < 0)
	    xLength = xLength * -1;
	if (zLength < 0)
	    zLength = zLength * -1;

	for (int y = 0; y <= yLength; y++) {
	    for (int x = 0; x <= xLength; x++) {
		for (int z = 0; z <= zLength; z++) {

		    int tempx = 0;
		    int tempz = 0;

		    if (tx > bx)
			tempx = tx - x;
		    else
			tempx = tx + x;

		    if (tz > bz)
			tempz = tz - z;
		    else
			tempz = tz + z;

		    Locations.add(new Location(Bukkit.getWorld(world), tempx, ty - y, tempz));
		}
	    }
	}

	return this.Locations;
    }

    public Location GetTopLocation() {
	if (this.TopLoc == null) {
	    World w = Bukkit.getWorld(this.world);
	    if (w != null)
		this.TopLoc = new Location(Bukkit.getWorld(this.world), this.tx, this.ty, this.tz);
	}
	return this.TopLoc;
    }

    public Location GetBottomLocation() {
	if (this.BottomLoc == null) {
	    World w = Bukkit.getWorld(this.world);
	    if (w != null)
		this.BottomLoc = new Location(Bukkit.getWorld(this.world), this.bx, this.by, this.bz);
	}
	return this.BottomLoc;
    }

    public void setStartPlace(int StartPlace) {
	this.StartPlace = StartPlace;
    }

    public int GetStartPlace() {
	return this.StartPlace == 0 ? 0 : (StartPlace - 1);
    }

    public void setWorld(String World) {
	this.world = World;
    }

    public String GetWorld() {
	return this.world;
    }

    public void setTX(Integer x) {
	this.tx = x;
    }

    public void setTY(Integer y) {
	this.ty = y;
    }

    public void setTZ(Integer z) {
	this.tz = z;
    }

    public void setBX(Integer x) {
	this.bx = x;
    }

    public void setBY(Integer y) {
	this.by = y;
    }

    public void setBZ(Integer z) {
	this.bz = z;
    }
}

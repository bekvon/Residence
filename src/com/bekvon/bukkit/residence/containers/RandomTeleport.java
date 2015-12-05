package com.bekvon.bukkit.residence.containers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class RandomTeleport {
    String WorldName;
    int MaxCord;
    int MinCord;
    int centerX;
    int centerZ;
    Location loc;

    public RandomTeleport(String WorldName, int MaxCord, int MinCord, int centerX, int centerZ) {
	this.WorldName = WorldName;
	this.MaxCord = MaxCord;
	this.MinCord = MinCord;
	this.centerX = centerX;
	this.centerZ = centerZ;
    }

    public Location getCenter() {
	if (loc == null) {
	    World w = Bukkit.getWorld(WorldName);
	    this.loc = new Location(w, centerX, 63, centerZ);
	}
	return this.loc;
    }

    public String getWorld() {
	return this.WorldName;
    }

    public int getMaxCord() {
	return this.MaxCord;
    }

    public int getMinCord() {
	return this.MinCord;
    }

    public int getCenterX() {
	return this.centerX;
    }

    public int getCenterZ() {
	return this.centerZ;
    }
}

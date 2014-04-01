package net.t00thpick1.residence.protection;

import net.t00thpick1.residence.api.areas.CuboidArea;

import org.bukkit.Location;

public interface CuboidAreaFactory {
    public CuboidArea createArea(Location loc1, Location loc2);
}

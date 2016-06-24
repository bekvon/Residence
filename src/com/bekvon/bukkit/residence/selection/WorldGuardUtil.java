package com.bekvon.bukkit.residence.selection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardUtil {
    public static ProtectedRegion isSelectionInRegion(Player player) {

	if (Residence.wg == null)
	    return null;

	if (Residence.wep == null)
	    return null;

	if (!Residence.getSelectionManager().hasPlacedBoth(player.getName()))
	    return null;
	Location loc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
	Location loc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());

	String id = "icp__tempregion";
	try {
	    BlockVector min = new BlockVector(loc1.getX(), loc1.getY(), loc1.getZ());
	    BlockVector max = new BlockVector(loc2.getX(), loc2.getY(), loc2.getZ());
	    ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
	    RegionContainer cn = Residence.wg.getRegionContainer();
	    RegionManager mgr = cn.get(loc1.getWorld());
	    ApplicableRegionSet regions = mgr.getApplicableRegions(region);

	    for (ProtectedRegion one : regions) {
		if (!player.hasPermission("residence.worldguard." + one.getId()))
		    return one;
	    }
	} catch (Exception e) {
	}
	return null;
    }
}

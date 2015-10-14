/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.selection;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class WorldEditSelectionManager extends SelectionManager {

    public WorldEditSelectionManager(Server serv) {
	super(serv);
    }

    @Override
    public boolean worldEdit(Player player) {
	WorldEditPlugin wep = (WorldEditPlugin) this.server.getPluginManager().getPlugin("WorldEdit");
	Selection sel = wep.getSelection(player);
	if (sel != null) {
	    Location pos1 = sel.getMinimumPoint();
	    Location pos2 = sel.getMaximumPoint();
	    try {
		CuboidRegion region = (CuboidRegion) sel.getRegionSelector().getRegion();
		pos1 = new Location(player.getWorld(), region.getPos1().getX(), region.getPos1().getY(), region.getPos1().getZ());
		pos2 = new Location(player.getWorld(), region.getPos2().getX(), region.getPos2().getY(), region.getPos2().getZ());
	    } catch (Exception e) {
	    }
	    this.playerLoc1.put(player.getName(), pos1);
	    this.playerLoc2.put(player.getName(), pos2);
	    afterSelectionUpdate(player);
	    return true;
	}
	return false;
    }

    @Override
    public void placeLoc1(Player player, Location loc) {
//	this.worldEdit(player);
	super.placeLoc1(player, loc);
    }

    @Override
    public void placeLoc2(Player player, Location loc) {
//	this.worldEdit(player);
	super.placeLoc2(player, loc);
    }

    @Override
    public void sky(Player player, boolean resadmin) {
//	this.worldEdit(player);
	super.sky(player, resadmin);
	afterSelectionUpdate(player);
    }

    @Override
    public void bedrock(Player player, boolean resadmin) {
//	this.worldEdit(player);
	super.bedrock(player, resadmin);
	afterSelectionUpdate(player);
    }

    @Override
    public void modify(Player player, boolean shift, int amount) {
//	this.worldEdit(player);
	super.modify(player, shift, amount);
	afterSelectionUpdate(player);
    }

    @Override
    public void selectChunk(Player player) {
//	this.worldEdit(player);
	super.selectChunk(player);
	afterSelectionUpdate(player);
    }

    @Override
    public void showSelectionInfo(Player player) {
//	this.worldEdit(player);
	super.showSelectionInfo(player);
    }
}

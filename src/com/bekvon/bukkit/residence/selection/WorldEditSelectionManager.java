package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class WorldEditSelectionManager extends SelectionManager {

    public WorldEditSelectionManager(Server serv, Residence plugin) {
	super(serv, plugin);
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
    public boolean worldEditUpdate(Player player) {
	if (!hasPlacedBoth(player.getName()))
	    return false;
	CuboidSelection selection = new CuboidSelection(player.getWorld(), getPlayerLoc1(player.getName()), getPlayerLoc2(player.getName()));
	Residence.wep.setSelection(player, selection);
	return true;
    }

    @Override
    public void placeLoc1(Player player, Location loc, boolean show) {
	super.placeLoc1(player, loc, show);
	this.worldEditUpdate(player);
    }

    @Override
    public void placeLoc2(Player player, Location loc, boolean show) {
	super.placeLoc2(player, loc, show);
	this.worldEditUpdate(player);
    }

    @Override
    public void sky(Player player, boolean resadmin) {
	super.sky(player, resadmin);
	this.worldEditUpdate(player);
	afterSelectionUpdate(player);
    }

    @Override
    public void bedrock(Player player, boolean resadmin) {
	super.bedrock(player, resadmin);
	this.worldEditUpdate(player);
	afterSelectionUpdate(player);
    }

    @Override
    public void modify(Player player, boolean shift, double amount) {
	super.modify(player, shift, amount);
	this.worldEditUpdate(player);
	afterSelectionUpdate(player);
    }

    @Override
    public void selectChunk(Player player) {
	super.selectChunk(player);
	this.worldEditUpdate(player);
	afterSelectionUpdate(player);
    }

    @Override
    public void showSelectionInfo(Player player) {
	super.showSelectionInfo(player);
	this.worldEditUpdate(player);
    }
}

package com.bekvon.bukkit.residence.selection;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
//import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
//import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import com.sk89q.worldedit.world.World;

public class WorldEdit7SelectionManager extends SelectionManager {

    public WorldEdit7SelectionManager(Server serv, Residence plugin) {
	super(serv, plugin);
    }

    @Override
    public boolean worldEdit(Player player) {
	WorldEditPlugin wep = (WorldEditPlugin) this.server.getPluginManager().getPlugin("WorldEdit");
	Region sel = null;
	try {
	    World w = wep.getSession(player).getSelectionWorld();
	    if (w != null)
		sel = wep.getSession(player).getSelection(w);
	    if (sel != null) {
		try {
		    Location pos1 = new Location(player.getWorld(), sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(), sel.getMinimumPoint().getZ());
		    Location pos2 = new Location(player.getWorld(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(), sel.getMaximumPoint().getZ());
		    this.updateLocations(player, pos1, pos2);
		} catch (Exception e) {
		}
		return true;
	    }
	} catch (IncompleteRegionException e1) {
	    e1.printStackTrace();
	}
	return false;
    }

    @Override
    public boolean worldEditUpdate(Player player) {
	if (!hasPlacedBoth(player))
	    return false;

	World w = BukkitAdapter.adapt(player.getWorld());

	plugin.getWorldEdit().getSession(player).getRegionSelector(w).selectPrimary(new Vector(getPlayerLoc1(player).getBlockX(), getPlayerLoc1(player).getBlockY(), getPlayerLoc1(player).getBlockZ()),
	    PermissiveSelectorLimits.getInstance());
	plugin.getWorldEdit().getSession(player).getRegionSelector(w).selectSecondary(new Vector(getPlayerLoc2(player).getBlockX(), getPlayerLoc2(player).getBlockY(), getPlayerLoc2(player).getBlockZ()),
	    PermissiveSelectorLimits.getInstance());

//	CuboidSelection selection = new CuboidSelection(player.getWorld(), getPlayerLoc1(player), getPlayerLoc2(player));

//	plugin.getWorldEdit().getSession(player).setRegionSelector(plugin.getWorldEdit().getSession(player).getSelectionWorld(), selection.getRegionSelector());
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
    }

    @Override
    public void bedrock(Player player, boolean resadmin) {
	super.bedrock(player, resadmin);
	this.worldEditUpdate(player);
    }

    @Override
    public void modify(Player player, boolean shift, double amount) {
	super.modify(player, shift, amount);
	this.worldEditUpdate(player);
    }

    @Override
    public void selectChunk(Player player) {
	super.selectChunk(player);
	this.worldEditUpdate(player);
    }

    @Override
    public void showSelectionInfo(Player player) {
	super.showSelectionInfo(player);
	this.worldEditUpdate(player);
    }

    @Override
    public void regenerate(CuboidArea area) {
	// Create new selector
	CuboidRegionSelector sellection = new CuboidRegionSelector(BukkitAdapter.adapt(area.getWorld()));

	// set up selector
	sellection.selectPrimary(new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ()), PermissiveSelectorLimits.getInstance());
	sellection.selectSecondary(new Vector(area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ()), PermissiveSelectorLimits.getInstance());

	// set up CuboidSelection
	CuboidRegion cuboid = sellection.getIncompleteRegion();

//	    Region region = selection..getRegionSelector().getRegion();
	cuboid.getWorld().regenerate(cuboid, WorldEdit.getInstance().getEditSessionFactory().getEditSession(cuboid.getWorld(), -1));
    }
}

package com.bekvon.bukkit.residence.selection;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.AutoSelector;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class AutoSelection {

    private HashMap<String, AutoSelector> list = new HashMap<String, AutoSelector>();
    private Residence plugin;

    public AutoSelection(Residence residence) {
	this.plugin = residence;
    }

    public void switchAutoSelection(Player player) {
	if (!list.containsKey(player.getName().toLowerCase())) {
	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    PermissionGroup group = rPlayer.getGroup(player.getWorld().getName());
	    list.put(player.getName().toLowerCase(), new AutoSelector(group, System.currentTimeMillis()));
	    plugin.msg(player, lm.Select_AutoEnabled);
	} else {
	    list.remove(player.getName().toLowerCase());
	    plugin.msg(player, lm.Select_AutoDisabled);
	}
    }

    public void UpdateSelection(Player player) {

	if (!getList().containsKey(player.getName().toLowerCase()))
	    return;

	AutoSelector AutoSelector = getList().get(player.getName().toLowerCase());

	int Curenttime = (int) (System.currentTimeMillis() - AutoSelector.getTime()) / 1000;

	if (Curenttime > 270) {
	    list.remove(player.getName().toLowerCase());
	    plugin.msg(player, lm.Select_AutoDisabled);
	    return;
	}

	String name = player.getName();

	Location cloc = player.getLocation();

	Location loc1 = plugin.getSelectionManager().getPlayerLoc1(name);
	Location loc2 = plugin.getSelectionManager().getPlayerLoc2(name);

	if (loc1 == null) {
	    plugin.getSelectionManager().placeLoc1(player, cloc, false);
	    loc1 = player.getLocation();
	    return;
	}

	if (loc2 == null) {
	    plugin.getSelectionManager().placeLoc2(player, cloc, true);
	    loc2 = player.getLocation();
	    return;
	}

	boolean changed = false;

	CuboidArea area = new CuboidArea(loc1, loc2);
	Location hloc = area.getHighLoc();
	Location lloc = area.getLowLoc();

	if (cloc.getBlockX() < lloc.getBlockX()) {
	    lloc.setX(cloc.getBlockX());
	    changed = true;
	}

	if (cloc.getBlockY() <= lloc.getBlockY()) {
	    lloc.setY(cloc.getBlockY() - 1);
	    changed = true;
	}

	if (cloc.getBlockZ() < lloc.getBlockZ()) {
	    lloc.setZ(cloc.getBlockZ());
	    changed = true;
	}

	if (cloc.getBlockX() > hloc.getBlockX()) {
	    hloc.setX(cloc.getBlockX());
	    changed = true;
	}

	if (cloc.getBlockY() >= hloc.getBlockY()) {
	    hloc.setY(cloc.getBlockY() + 1);
	    changed = true;
	}

	if (cloc.getBlockZ() > hloc.getBlockZ()) {
	    hloc.setZ(cloc.getBlockZ());
	    changed = true;
	}

	PermissionGroup group = AutoSelector.getGroup();

	if (area.getXSize() > group.getMaxX()) {
	    return;
	}

	if (area.getYSize() > group.getMaxY() && !plugin.getConfigManager().isSelectionIgnoreY()) {
	    return;
	}

	if (area.getZSize() > group.getMaxZ()) {
	    return;
	}

	if (changed) {
	    plugin.getSelectionManager().placeLoc1(player, hloc, false);
	    plugin.getSelectionManager().placeLoc2(player, lloc, true);
	    plugin.getSelectionManager().showSelectionInfoInActionBar(player);
	}
    }

    public HashMap<String, AutoSelector> getList() {
	return list;
    }
}

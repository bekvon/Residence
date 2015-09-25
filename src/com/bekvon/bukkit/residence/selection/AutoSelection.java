package com.bekvon.bukkit.residence.selection;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.NewLanguage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class AutoSelection {

    private static HashMap<String, PermissionGroup> list = new HashMap<String, PermissionGroup>();

    public static void switchAutoSelection(Player player) {
	if (!list.containsKey(player.getName().toLowerCase())) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player.getName(), player.getLocation().getWorld().getName());
	    list.put(player.getName().toLowerCase(), group);
	    player.sendMessage(NewLanguage.getMessage("Language.AutoSelection.Enabled"));
	} else {
	    list.remove(player.getName().toLowerCase());
	    player.sendMessage(NewLanguage.getMessage("Language.AutoSelection.Disabled"));
	}
    }

    public static void UpdateSelection(Player player) {

	if (getList().size() == 0)
	    return;

	if (!getList().containsKey(player.getName().toLowerCase()))
	    return;

	String name = player.getName();

	Location cloc = player.getLocation();

	Location loc1 = Residence.getSelectionManager().getPlayerLoc1(name);
	Location loc2 = Residence.getSelectionManager().getPlayerLoc2(name);

	if (loc1 == null) {
	    Residence.getSelectionManager().placeLoc1(player, cloc);
	    loc1 = player.getLocation();
	    return;
	}

	if (loc2 == null) {
	    Residence.getSelectionManager().placeLoc2(player, cloc);
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

	PermissionGroup group = getList().get(player.getName().toLowerCase());

	if (area.getXSize() > group.getMaxX()) {
	    return;
	}

	if (area.getYSize() > group.getMaxY()) {
	    return;
	}

	if (area.getZSize() > group.getMaxZ()) {
	    return;
	}

	if (changed) {
	    Residence.getSelectionManager().placeLoc1(player, hloc);
	    Residence.getSelectionManager().placeLoc2(player, lloc);
	    Residence.getSelectionManager().showSelectionInfoInActionBar(player);
	}
    }

    public static HashMap<String, PermissionGroup> getList() {
	return list;
    }
}

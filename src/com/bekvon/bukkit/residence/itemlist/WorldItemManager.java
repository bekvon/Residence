/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.itemlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Administrator
 */
public class WorldItemManager {
    protected List<WorldItemList> lists;

    public WorldItemManager(FileConfiguration config) {
	lists = new ArrayList<WorldItemList>();
	this.readLists(config);
    }

    public boolean isAllowed(Material mat, String group, String world) {
	for (WorldItemList list : lists) {
	    if (!list.isAllowed(mat, world, group)) {
		return false;
	    }
	}
	return true;
    }

    public boolean isIgnored(Material mat, String group, String world) {
	for (WorldItemList list : lists) {
	    if (list.isIgnored(mat, world, group)) {
		return true;
	    }
	}
	return false;
    }

    private void readLists(FileConfiguration config) {
	Set<String> keys = config.getConfigurationSection("ItemList").getKeys(false);
	if (keys != null) {
	    for (String key : keys) {
		try {
		    WorldItemList list = WorldItemList.readList(config.getConfigurationSection("ItemList." + key));
		    lists.add(list);
		    //System.out.println("Debug: read list " + key + " world: " + list.getWorld() + " group: " + list.getGroup() + " itemcount:" + list.getListSize());
		} catch (Exception ex) {
		    System.out.println("Failed to load item list:" + key);
		}
	    }
	}
    }
}

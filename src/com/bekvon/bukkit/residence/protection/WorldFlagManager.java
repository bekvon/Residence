/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class WorldFlagManager {
    protected Map<String, Map<String, FlagPermissions>> groupperms;
    protected Map<String, FlagPermissions> worldperms;
    protected FlagPermissions globaldefaults;

    public WorldFlagManager() {
	globaldefaults = new FlagPermissions();
	worldperms = new HashMap<>();
	groupperms = new HashMap<>();
    }

    public WorldFlagManager(FileConfiguration config, FileConfiguration groups) {
	this();
	this.parsePerms(config, groups);
    }

    public FlagPermissions getPerms(Player player) {
	return this.getPerms(player.getWorld().getName(), Residence.getPermissionManager().getGroupNameByPlayer(player));
    }

    public FlagPermissions getPerms(String world, String group) {
	world = world.toLowerCase();
	group = group.toLowerCase();
	Map<String, FlagPermissions> groupworldperms = groupperms.get(group);
	if (groupworldperms == null) {
	    return this.getPerms(world);
	}
	FlagPermissions list = groupworldperms.get(world);
	if (list == null) {
	    list = groupworldperms.get("global." + world);
	    if (list == null) {
		list = groupworldperms.get("global");
	    }
	    if (list == null) {
		return this.getPerms(world);
	    }
	}
	return list;
    }

    public FlagPermissions getPerms(String world) {
	world = world.toLowerCase();
	FlagPermissions list = worldperms.get(world);
	if (list == null) {
	    if (globaldefaults == null)
		return new FlagPermissions();
	    else
		return globaldefaults;
	}
	return list;
    }

    public final void parsePerms(FileConfiguration config, FileConfiguration groups) {
	try {

	    Set<String> keys = config.getConfigurationSection("Global.Flags").getKeys(false);
	    if (keys != null) {
		for (String key : keys) {
		    if (key.equalsIgnoreCase("Global")) {
			globaldefaults = FlagPermissions.parseFromConfigNode(key, config.getConfigurationSection("Global.Flags"));
		    } else {
			worldperms.put(key.toLowerCase(), FlagPermissions.parseFromConfigNode(key, config.getConfigurationSection("Global.Flags")));
		    }
		}
	    }
	    for (Entry<String, FlagPermissions> entry : worldperms.entrySet()) {
		entry.getValue().setParent(globaldefaults);
	    }
	    keys = groups.getConfigurationSection("Groups").getKeys(false);
	    if (keys != null) {
		for (String key : keys) {
		    Set<String> worldkeys = groups.getConfigurationSection("Groups." + key + ".Flags.World").getKeys(false);
		    if (worldkeys != null) {
			Map<String, FlagPermissions> perms = new HashMap<>();
			for (String wkey : worldkeys) {
			    FlagPermissions list = FlagPermissions.parseFromConfigNode(wkey, groups.getConfigurationSection("Groups." + key + ".Flags.World"));
			    if (wkey.equalsIgnoreCase("global")) {
				list.setParent(globaldefaults);
				perms.put(wkey.toLowerCase(), list);
				for (Entry<String, FlagPermissions> worldperm : worldperms.entrySet()) {
				    list = FlagPermissions.parseFromConfigNode(wkey, groups.getConfigurationSection("Groups." + key + ".Flags.World"));
				    list.setParent(worldperm.getValue());
				    perms.put("global." + worldperm.getKey().toLowerCase(), list);
				}
			    } else {
				perms.put(wkey.toLowerCase(), list);
			    }
			}
			for (Entry<String, FlagPermissions> entry : perms.entrySet()) {
			    String wkey = entry.getKey();
			    FlagPermissions list = entry.getValue();
			    if (!wkey.startsWith("global.")) {
				list.setParent(perms.get("global." + wkey));
				if (list.getParent() == null) {
				    list.setParent(worldperms.get(wkey));
				}
				if (list.getParent() == null) {
				    list.setParent(globaldefaults);
				}
			    }
			}
			groupperms.put(key.toLowerCase(), perms);
		    }
		}
	    }
	} catch (Exception ex) {
	    Logger.getLogger(WorldFlagManager.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
}

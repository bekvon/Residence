package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

public class PlayerGroup {

    String playerName = null;
    Player player = null;
    long lastCheck = 0L;
    HashMap<String, String> groups = new HashMap<String, String>();

    public PlayerGroup(String playerName) {
	this.playerName = playerName;
	this.player = Bukkit.getPlayer(playerName);
    }

    public PlayerGroup(Player player) {
	this.playerName = player.getName();
	this.player = player;
    }

    public void setLastCkeck(Long time) {
	this.lastCheck = time;
    }

    public void addGroup(String world, String group) {
	groups.put(world, group);
    }

    public String getGroup(String world) {
	updateGroup(world, false);
	return this.groups.get(world);
    }

    public void updateGroup(String world, boolean force) {
	if (!force && this.lastCheck != 0L && System.currentTimeMillis() - this.lastCheck < 60 * 1000)
	    return;

	this.lastCheck = System.currentTimeMillis();
	String group;
	if (Residence.getPermissionManager().getPlayersGroups().containsKey(playerName.toLowerCase())) {
	    group = Residence.getPermissionManager().getPlayersGroups().get(playerName.toLowerCase());
	    if (group != null) {
		group = group.toLowerCase();
		if (group != null && Residence.getPermissionManager().getGroups().containsKey(group)) {
		    this.groups.put(world, group);
		}
	    }
	}

	String permGroup = getPermissionGroup();
	if (permGroup != null) {
	    this.groups.put(world, permGroup);
	}

	group = Residence.getPermissionManager().getPermissionsGroup(playerName.toLowerCase(), world);

	if (group == null || !Residence.getPermissionManager().getGroups().containsKey(group)) {
	    this.groups.put(world, Residence.getConfigManager().getDefaultGroup().toLowerCase());
	} else {
	    this.groups.put(world, group);
	}
    }

    private String getPermissionGroup() {
	String group = null;
	for (Entry<String, PermissionGroup> one : Residence.getPermissionManager().getGroups().entrySet()) {
	    if (player != null) {
		if (this.player.hasPermission("residence.group." + one.getKey()))
		    group = one.getKey();
	    } else {
		OfflinePlayer offlineP = Residence.getOfflinePlayer(playerName);
		if (offlineP != null)
		    if (ResidenceVaultAdapter.hasPermission(offlineP, "residence.group." + one.getKey(), Residence.getConfigManager().getDefaultWorld()))
			group = one.getKey();
	    }
	}
	return group;
    }

}

package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	List<PermissionGroup> posibleGroups = new ArrayList<PermissionGroup>();
	String group;
	if (Residence.getPermissionManager().getPlayersGroups().containsKey(playerName.toLowerCase())) {
	    group = Residence.getPermissionManager().getPlayersGroups().get(playerName.toLowerCase());
	    if (group != null) {
		group = group.toLowerCase();
		if (group != null && Residence.getPermissionManager().getGroups().containsKey(group)) {
		    PermissionGroup g = Residence.getPermissionManager().getGroups().get(group);
		    posibleGroups.add(g);
		    this.groups.put(world, group);
		}
	    }
	}

	posibleGroups.add(getPermissionGroup());

	group = Residence.getPermissionManager().getPermissionsGroup(playerName, world);

	PermissionGroup g = Residence.getPermissionManager().getGroupByName(group);

	if (g != null)
	    posibleGroups.add(g);

	PermissionGroup finalGroup = null;
	if (posibleGroups.size() == 1)
	    finalGroup = posibleGroups.get(0);

	for (int i = 0; i < posibleGroups.size(); i++) {
	    if (finalGroup == null) {
		finalGroup = posibleGroups.get(i);
		continue;
	    }

	    if (finalGroup.getPriority() < posibleGroups.get(i).getPriority())
		finalGroup = posibleGroups.get(i);
	}

	if (finalGroup == null || !Residence.getPermissionManager().getGroups().containsValue(finalGroup)) {
	    this.groups.put(world, Residence.getConfigManager().getDefaultGroup().toLowerCase());
	} else {
	    this.groups.put(world, finalGroup.getGroupName());
	}
    }

    private PermissionGroup getPermissionGroup() {
	if (this.player == null)
	    this.player = Bukkit.getPlayer(playerName);
	PermissionGroup group = Residence.getPermissionManager().getGroupByName(Residence.getConfigManager().getDefaultGroup());
	for (Entry<String, PermissionGroup> one : Residence.getPermissionManager().getGroups().entrySet()) {
	    if (player != null) {
		if (this.player.hasPermission("residence.group." + one.getKey()))
		    group = one.getValue();
	    } else {
		OfflinePlayer offlineP = Residence.getOfflinePlayer(playerName);
		if (offlineP != null)
		    if (ResidenceVaultAdapter.hasPermission(offlineP, "residence.group." + one.getKey(), Residence.getConfigManager().getDefaultWorld()))
			group = one.getValue();
	    }
	}
	return group;
    }

}

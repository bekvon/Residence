package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidencePlayerInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

public class PlayerManager implements ResidencePlayerInterface {
    private ConcurrentHashMap<String, ResidencePlayer> players = new ConcurrentHashMap<String, ResidencePlayer>();
    private Residence plugin;

    public PlayerManager(Residence plugin) {
	this.plugin = plugin;
    }

    public void playerJoin(OfflinePlayer player) {
	ResidencePlayer resPlayer = players.get(player.getName());
	if (resPlayer == null) {
	    resPlayer = new ResidencePlayer(player.getName());
	    resPlayer.recountRes();
	    players.put(player.getName(), resPlayer);
	} else
	    resPlayer.RecalculatePermissions();
	return;
    }

    public ResidencePlayer playerJoin(String player) {
	if (!players.containsKey(player)) {
	    ResidencePlayer resPlayer = new ResidencePlayer(player);
	    resPlayer.recountRes();
	    players.put(player, resPlayer);
	    return resPlayer;
	}
	return null;
    }

    public void fillList() {
	players.clear();
	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
		    playerJoin(player);
		}
		return;
	    }
	});
    }

    public ArrayList<String> getResidenceList(String player) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResidencePlayer resPlayer = players.get(player);
	if (resPlayer != null) {
	    for (ClaimedResidence one : resPlayer.getResList()) {
		temp.add(one.getName());
	    }
	    return temp;
	}
	return temp;
    }

    public ArrayList<String> getResidenceList(String player, boolean showhidden) {
	return getResidenceList(player, showhidden, false);
    }

    public ArrayList<String> getResidenceList(String player, boolean showhidden, boolean onlyHidden) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResidencePlayer resPlayer = players.get(player);
	if (resPlayer == null)
	    return temp;
	for (ClaimedResidence one : resPlayer.getResList()) {
	    boolean hidden = one.getPermissions().has("hidden", false);
	    if (!showhidden && hidden)
		continue;

	    if (onlyHidden && !hidden)
		continue;

	    temp.add(Residence.getLM().getMessage("Residence.List", "", one.getName(), one.getWorld()) +
		(hidden ? Residence.getLM().getMessage("Residence.Hidden") : ""));
	}
	Collections.sort(temp, String.CASE_INSENSITIVE_ORDER);
	return temp;
    }

    public PermissionGroup getGroup(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getGroup();
	}
	return null;
    }

    public int getMaxResidences(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRes();
	}
	return -1;
    }

    public int getMaxSubzones(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxSubzones();
	}
	return -1;
    }

    public int getMaxRents(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRents();
	}
	return -1;
    }

    public ResidencePlayer getResidencePlayer(Player player) {
	return getResidencePlayer(player.getName());
    }

    public ResidencePlayer getResidencePlayer(String player) {
	ResidencePlayer resPlayer = null;
	if (players.containsKey(player))
	    resPlayer = players.get(player);
	else {
	    resPlayer = playerJoin(player);
	}
	return resPlayer;
    }

    public void renameResidence(String player, String oldName, String newName) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    resPlayer.renameResidence(oldName, newName);
	}
	return;
    }

    public void addResidence(String player, ClaimedResidence residence) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    resPlayer.addResidence(residence);
	}
	return;
    }

    public void removeResFromPlayer(OfflinePlayer player, String residence) {
	removeResFromPlayer(player.getName(), residence);
    }

    public void removeResFromPlayer(Player player, String residence) {
	removeResFromPlayer(player.getName(), residence);
    }

    public void removeResFromPlayer(String player, String residence) {
	ResidencePlayer resPlayer = players.get(player);
	if (resPlayer != null) {
	    resPlayer.removeResidence(residence);
	}
	return;
    }
}

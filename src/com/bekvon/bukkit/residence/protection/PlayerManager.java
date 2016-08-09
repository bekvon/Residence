package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidencePlayerInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

public class PlayerManager implements ResidencePlayerInterface {
    private ConcurrentHashMap<String, ResidencePlayer> players = new ConcurrentHashMap<String, ResidencePlayer>();

    public PlayerManager() {
    }

    public void playerJoin(Player player) {
	ResidencePlayer resPlayer = players.get(player.getName().toLowerCase());
	if (resPlayer == null) {
	    resPlayer = new ResidencePlayer(player);
	    resPlayer.recountRes();
	    players.put(player.getName().toLowerCase(), resPlayer);
	} else
	    resPlayer.RecalculatePermissions();
	return;
    }

    public ResidencePlayer playerJoin(String player) {
	if (!players.containsKey(player.toLowerCase())) {
	    ResidencePlayer resPlayer = new ResidencePlayer(player);
	    resPlayer.recountRes();
	    players.put(player.toLowerCase(), resPlayer);
	    return resPlayer;
	}
	return null;
    }

    public void fillList() {
	players.clear();
	for (Player player : Bukkit.getOnlinePlayers()) {
	    playerJoin(player);
	}
    }

    @Override
    public ArrayList<String> getResidenceList(String player) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResidencePlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer != null) {
	    for (ClaimedResidence one : resPlayer.getResList()) {
		temp.add(one.getName());
	    }
	    return temp;
	}
	return temp;
    }

    @Override
    public ArrayList<String> getResidenceList(String player, boolean showhidden) {
	return getResidenceList(player, showhidden, false);
    }

    public ArrayList<String> getResidenceList(String player, boolean showhidden, boolean onlyHidden) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResidencePlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer == null)
	    return temp;
	for (ClaimedResidence one : resPlayer.getResList()) {
	    boolean hidden = one.getPermissions().has("hidden", false);
	    if (!showhidden && hidden)
		continue;

	    if (onlyHidden && !hidden)
		continue;

	    temp.add(Residence.msg(lm.Residence_List, "", one.getName(), one.getWorld()) +
		(hidden ? Residence.msg(lm.Residence_Hidden) : ""));
	}
	Collections.sort(temp, String.CASE_INSENSITIVE_ORDER);
	return temp;
    }

    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden) {
	return getResidences(player, showhidden, false);
    }

    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden, boolean onlyHidden) {
	return getResidences(player, showhidden, onlyHidden, null);
    }

    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden, boolean onlyHidden, World world) {
	ArrayList<ClaimedResidence> temp = new ArrayList<ClaimedResidence>();
	playerJoin(player);
	ResidencePlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer == null)
	    return temp;
	for (ClaimedResidence one : resPlayer.getResList()) {
	    boolean hidden = one.getPermissions().has("hidden", false);
	    if (!showhidden && hidden)
		continue;
	    if (onlyHidden && !hidden)
		continue;
	    if (world != null && !world.getName().equalsIgnoreCase(one.getWorld()))
		continue;
	    temp.add(one);
	}
	return temp;
    }

    @Override
    public PermissionGroup getGroup(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getGroup();
	}
	return null;
    }

    @Override
    public int getMaxResidences(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRes();
	}
	return -1;
    }

    @Override
    public int getMaxSubzones(String player) {
	ResidencePlayer resPlayer = getResidencePlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxSubzones();
	}
	return -1;
    }

    @Override
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

    @Override
    public ResidencePlayer getResidencePlayer(String player) {
	ResidencePlayer resPlayer = null;
	if (players.containsKey(player.toLowerCase())) {
	    resPlayer = players.get(player.toLowerCase());
	} else {
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
	ResidencePlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer != null) {
	    resPlayer.removeResidence(residence);
	}
	return;
    }
}

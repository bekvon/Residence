package com.bekvon.bukkit.residence;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.containers.ResPlayer;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class PlayerManager {
    private ConcurrentHashMap<String, ResPlayer> players = new ConcurrentHashMap<String, ResPlayer>();
    private Residence plugin;

    public PlayerManager(Residence plugin) {
	this.plugin = plugin;
    }

    public void playerJoin(OfflinePlayer player) {
	ResPlayer resPlayer = players.get(player.getName());
	if (resPlayer == null) {
	    resPlayer = new ResPlayer(player.getName());
	    resPlayer.recountRes();
	    players.put(player.getName(), resPlayer);
	} else
	    resPlayer.RecalculatePermissions();
	return;
    }

    public ResPlayer playerJoin(String player) {
	if (!players.containsKey(player)) {
	    ResPlayer resPlayer = new ResPlayer(player);
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
	ResPlayer resPlayer = players.get(player);
	if (resPlayer != null) {
	    for (Entry<String, String> one : resPlayer.getResList().entrySet()) {
		temp.add(one.getKey());
	    }
	    return temp;
	}
	return temp;
    }

    public ArrayList<String> getResidenceListString(String player, boolean showhidden) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResPlayer resPlayer = players.get(player);
	if (resPlayer != null) {
	    for (Entry<String, String> one : resPlayer.getResList().entrySet()) {
		if (!showhidden) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(one.getKey());
		    boolean hidden = res.getPermissions().has("hidden", false);
		    if (hidden)
			continue;
		}

		temp.add(Residence.getLanguage().getPhrase("ResidenceList", "|" + one.getKey() + "|" + Residence.getLanguage().getPhrase("World") + "|" + one
		    .getValue()));
	    }
	    return temp;
	}
	return temp;
    }

    public PermissionGroup getGroup(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getGroup();
	}
	return null;
    }

    public int getMaxResidences(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRes();
	}
	return -1;
    }

    public int getMaxSubzones(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxSubzones();
	}
	return -1;
    }

    public int getMaxRents(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRents();
	}
	return -1;
    }

    public ResPlayer getResPlayer(String player) {
	ResPlayer resPlayer = null;
	if (players.containsKey(player))
	    resPlayer = players.get(player);
	else {
	    resPlayer = playerJoin(player);
	}
	return resPlayer;
    }

    public void renameResidence(String player, String oldName, String newName) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    resPlayer.renameResidence(oldName, newName);
	}
	return;
    }

    public void addResidence(String player, ClaimedResidence residence) {
	ResPlayer resPlayer = getResPlayer(player);
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
	ResPlayer resPlayer = players.get(player);
	if (resPlayer != null) {
	    resPlayer.removeResidence(residence);
	}
	return;
    }
}

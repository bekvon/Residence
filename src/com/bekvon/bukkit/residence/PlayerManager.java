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
    private static ConcurrentHashMap<String, ResPlayer> players = new ConcurrentHashMap<String, ResPlayer>();

    public static void playerJoin(OfflinePlayer player) {
	ResPlayer resPlayer = players.get(player.getName().toLowerCase());
	if (resPlayer == null) {
	    resPlayer = new ResPlayer(player.getName());
	    resPlayer.recountRes();
	    players.put(player.getName().toLowerCase(), resPlayer);
	} else
	    resPlayer.RecalculatePermissions();
	return;
    }

    public static void playerJoin(String player) {
	ResPlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer == null) {
	    resPlayer = new ResPlayer(player);
	    resPlayer.recountRes();
	    players.put(player.toLowerCase(), resPlayer);
	}
	return;

    }

    public static void fillList() {
	players.clear();
	Bukkit.getScheduler().runTaskAsynchronously(Residence.instance, new Runnable() {
	    @Override
	    public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
		    PlayerManager.playerJoin(player);
		}
		return;
	    }
	});
    }

    public static ArrayList<String> getResidenceList(String player) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResPlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer != null) {
	    for (Entry<String, String> one : resPlayer.getResList().entrySet()) {
		temp.add(one.getKey());
	    }
	    return temp;
	}
	return temp;
    }

    public static ArrayList<String> getResidenceListString(String player, boolean showhidden) {
	ArrayList<String> temp = new ArrayList<String>();
	playerJoin(player);
	ResPlayer resPlayer = players.get(player.toLowerCase());
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

    public static PermissionGroup getGroup(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getGroup();
	}
	return null;
    }

    public static int getMaxResidences(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRes();
	}
	return -1;
    }

    public static int getMaxSubzones(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxSubzones();
	}
	return -1;
    }

    public static int getMaxRents(String player) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    return resPlayer.getMaxRents();
	}
	return -1;
    }

    public static ResPlayer getResPlayer(String player) {
	ResPlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer == null) {
	    playerJoin(player);
	    resPlayer = players.get(player.toLowerCase());
	}
	return resPlayer;
    }

    public static void renameResidence(String player, String oldName, String newName) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    resPlayer.renameResidence(oldName, newName);
	}
	return;
    }

    public static void addResidence(String player, ClaimedResidence residence) {
	ResPlayer resPlayer = getResPlayer(player);
	if (resPlayer != null) {
	    resPlayer.addResidence(residence);
	}
	return;
    }

    public static void removeResFromPlayer(OfflinePlayer player, String residence) {
	removeResFromPlayer(player.getName(), residence);
    }

    public static void removeResFromPlayer(Player player, String residence) {
	removeResFromPlayer(player.getName(), residence);
    }

    public static void removeResFromPlayer(String player, String residence) {
	ResPlayer resPlayer = players.get(player.toLowerCase());
	if (resPlayer != null) {
	    resPlayer.removeResidence(residence);
	}
	return;
    }
}

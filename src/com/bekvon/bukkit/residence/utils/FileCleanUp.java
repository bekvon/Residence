package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

public class FileCleanUp {

    public static void cleanFiles() {

	ArrayList<String> resNameList = Residence.getResidenceManager().getResidenceList(false, false);
	int i = 0;

	OfflinePlayer[] offplayer = Bukkit.getOfflinePlayers();

	HashMap<UUID, OfflinePlayer> playermap = new HashMap<UUID, OfflinePlayer>();

	for (OfflinePlayer one : offplayer) {
	    playermap.put(one.getUniqueId(), one);
	}

	int interval = Residence.getConfigManager().getResidenceFileCleanDays();
	long time = System.currentTimeMillis();

	for (String oneName : resNameList) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(oneName);
	    if (res == null)
		continue;

	    if (!playermap.containsKey(res.getOwnerUUID()))
		continue;

	    OfflinePlayer player = playermap.get(res.getOwnerUUID());

	    if (player == null)
		continue;

	    if (!Residence.getConfigManager().getCleanWorlds().contains(res.getWorld()))
		continue;

	    if (res.getOwner().equalsIgnoreCase("server land") || res.getOwner().equalsIgnoreCase(Residence.getServerLandname()))
		continue;

	    long lastPlayed = player.getLastPlayed();
	    int dif = (int) ((time - lastPlayed) / 1000 / 60 / 60 / 24);
	    if (dif < interval)
		continue;

	    if (ResidenceVaultAdapter.hasPermission(player, "residence.cleanbypass", res.getWorld()))
		continue;

	    Residence.getResidenceManager().removeResidence(oneName);
	    i++;
	}
	Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Auto CleanUp deleted " + i + " residences!");
    }
}

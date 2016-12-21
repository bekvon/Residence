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

    private Residence plugin;

    public FileCleanUp(Residence plugin) {
	this.plugin = plugin;
    }

    public void cleanFiles() {

	ArrayList<String> resNameList = plugin.getResidenceManager().getResidenceList(false, false);
	int i = 0;

	OfflinePlayer[] offplayer = Bukkit.getOfflinePlayers();

	HashMap<UUID, OfflinePlayer> playermap = new HashMap<UUID, OfflinePlayer>();

	for (OfflinePlayer one : offplayer) {
	    playermap.put(one.getUniqueId(), one);
	}

	int interval = plugin.getConfigManager().getResidenceFileCleanDays();
	long time = System.currentTimeMillis();

	for (String oneName : resNameList) {
	    ClaimedResidence res = plugin.getResidenceManager().getByName(oneName);
	    if (res == null)
		continue;

	    if (!playermap.containsKey(res.getOwnerUUID()))
		continue;

	    OfflinePlayer player = playermap.get(res.getOwnerUUID());

	    if (player == null)
		continue;

	    if (!plugin.getConfigManager().getCleanWorlds().contains(res.getWorld()))
		continue;

	    if (res.getOwner().equalsIgnoreCase("server land") || res.getOwner().equalsIgnoreCase(plugin.getServerLandname()))
		continue;

	    long lastPlayed = player.getLastPlayed();
	    int dif = (int) ((time - lastPlayed) / 1000 / 60 / 60 / 24);
	    if (dif < interval)
		continue;

	    if (ResidenceVaultAdapter.hasPermission(player, "residence.cleanbypass", res.getWorld()))
		continue;

	    plugin.getResidenceManager().removeResidence(oneName);
	    i++;
	}
	Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Auto CleanUp deleted " + i + " residences!");
    }
}

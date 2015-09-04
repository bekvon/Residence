package com.bekvon.bukkit.residence.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

public class VersionChecker {
    Residence plugin;

    public VersionChecker(Residence plugin) {
	this.plugin = plugin;
    }

    public void VersionCheck(final Player player) {
	if (!Residence.getConfigManager().versionCheck()) {
	    return;
	}

	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    public void run() {
		String readURL = "https://raw.githubusercontent.com/bekvon/Residence/master/src/plugin.yml";
		FileConfiguration config;
		String currentVersion = plugin.getDescription().getVersion();
		try {
		    URL url = new URL(readURL);
		    BufferedReader br = new BufferedReader(new InputStreamReader(
			url.openStream()));
		    config = YamlConfiguration.loadConfiguration(br);
		    String newVersion = config.getString("version");
		    br.close();
		    if (!newVersion.equals(currentVersion)) {
			String msg = ChatColor.GREEN + "Residence v" + newVersion + " is now available!\n" +
			    "Your version: " + currentVersion + "\n" +
			    "You can download new version from " + ChatColor.BLUE + plugin.getDescription().getWebsite();
			if (player != null) {
			    player.sendMessage(msg);
			} else {
			    plugin.consoleMessage(msg);
			}
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

}

package com.bekvon.bukkit.residence.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

import net.Zrips.CMILib.Version.Version;

public class VersionChecker {
    Residence plugin;
    private static int resource = 11480;

    public VersionChecker(Residence plugin) {
	this.plugin = plugin;
    }

    public Version getVersion() {
	return Version.getCurrent();
    }

    public Integer convertVersion(String v) {
	v = v.replaceAll("[^\\d.]", "");
	Integer version = 0;
	if (v.contains(".")) {
	    String lVersion = "";
	    for (String one : v.split("\\.")) {
		String s = one;
		if (s.length() == 1)
		    s = "0" + s;
		lVersion += s;
	    }

	    try {
		version = Integer.parseInt(lVersion);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    try {
		version = Integer.parseInt(v);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return version;
    }

    public void VersionCheck(final Player player) {
	if (!plugin.getConfigManager().versionCheck())
	    return;

	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {
		String currentVersion = plugin.getDescription().getVersion();
		String newVersion = getNewVersion();
		if (newVersion == null || newVersion.equalsIgnoreCase(currentVersion))
		    return;
		List<String> msg = Arrays.asList(
		    ChatColor.GREEN + "*********************** " + plugin.getDescription().getName() + " **************************",
		    ChatColor.GREEN + "* " + newVersion + " is now available! Your version: " + currentVersion,
		    ChatColor.GREEN + "* " + ChatColor.DARK_GREEN + plugin.getDescription().getWebsite(),
		    ChatColor.GREEN + "************************************************************");
		for (String one : msg)
		    if (player != null)
			player.sendMessage(one);
		    else
			Bukkit.getConsoleSender().sendMessage(one);
	    }
	});
    }

    public String getNewVersion() {
	try {
	    URLConnection con = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resource).openConnection();
	    String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
	    if (version.length() <= 8)
		return version;
	} catch (Exception ex) {
	    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to check for " + plugin.getDescription().getName() + " update on spigot web page.");
	}
	return null;
    }

}

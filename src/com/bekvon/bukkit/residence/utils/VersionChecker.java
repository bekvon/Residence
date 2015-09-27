package com.bekvon.bukkit.residence.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

public class VersionChecker {
    Residence plugin;
    private int resource = 11480;

    public VersionChecker(Residence plugin) {
	this.plugin = plugin;
    }

    public void VersionCheck(final Player player) {
	if (!Residence.getConfigManager().versionCheck())
	    return;

	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    public void run() {
		String currentVersion = plugin.getDescription().getVersion();
		String newVersion = getNewVersion();
		if (newVersion == null || !newVersion.equals(currentVersion))
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
			plugin.consoleMessage(one);
	    }
	});
    }

    public String getNewVersion() {
	try {
	    HttpURLConnection con = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
	    con.setDoOutput(true);
	    con.setRequestMethod("POST");
	    con.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + resource).getBytes("UTF-8"));
	    String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
	    if (version.length() <= 7)
		return version;
	} catch (Exception ex) {
	    plugin.consoleMessage(ChatColor.RED + "Failed to check for " + plugin.getDescription().getName() + " update on spigot web page.");
	}
	return null;
    }

}

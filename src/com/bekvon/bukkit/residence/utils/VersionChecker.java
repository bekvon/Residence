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
    private int cleanVersion = 0;

    public VersionChecker(Residence plugin) {
	this.plugin = plugin;
    }

    public int GetVersion() {
	if (cleanVersion == 0) {
	    String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
	    String version = v[v.length - 1];
	    // Translating version to integer for simpler use
	    try {
		cleanVersion = Integer.parseInt(version.replace("v", "").replace("V", "").replace("_", "").replace("r", "").replace("R", ""));
		cleanVersion *= 10;
	    } catch (NumberFormatException e) {
		// Fail safe if it for some reason can't translate version to integer
		if (version.contains("v1_4"))
		    cleanVersion = 1400;
		if (version.contains("v1_5"))
		    cleanVersion = 1500;
		if (version.contains("v1_6"))
		    cleanVersion = 1600;
		if (version.contains("v1_7"))
		    cleanVersion = 1700;
		if (version.contains("v1_8_R1"))
		    cleanVersion = 1810;
		if (version.contains("v1_8_R2"))
		    cleanVersion = 1820;
		if (version.contains("v1_8_R3"))
		    cleanVersion = 1830;
		if (version.contains("v1_9_R1"))
		    cleanVersion = 1910;
		if (version.contains("v1_9_R2"))
		    cleanVersion = 1920;
		if (version.contains("v1_10_R1"))
		    cleanVersion = 11010;
	    }

	    if (cleanVersion < 1400)
		cleanVersion *= 10;
	}
	return cleanVersion;
    }

    public void VersionCheck(final Player player) {
	if (!Residence.getConfigManager().versionCheck())
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
	    if (version.length() <= 9)
		return version;
	} catch (Exception ex) {
	    plugin.consoleMessage(ChatColor.RED + "Failed to check for " + plugin.getDescription().getName() + " update on spigot web page.");
	}
	return null;
    }

}

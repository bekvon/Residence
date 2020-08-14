package com.bekvon.bukkit.residence.slimeFun;

import org.bukkit.Bukkit;

import com.bekvon.bukkit.residence.Residence;

import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionManager;

public class slimeFunManager {

    private Residence plugin;

    int repeat = 0;
    // Fail safe to avoid infinite checks
    int times = 0;

    @SuppressWarnings("deprecation")
    public slimeFunManager(Residence plugin) {
	this.plugin = plugin;
	if (Bukkit.getPluginManager().getPlugin("Slimefun") != null && Bukkit.getPluginManager().getPlugin("CS-CoreLib") != null) {
	    repeat = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, () -> {
		times++;
		if (Bukkit.getPluginManager().getPlugin("Slimefun").isEnabled()) {
		    ProtectionManager manager = SlimefunPlugin.getProtectionManager();
		    manager.registerModule(Bukkit.getServer(), "Residence", pls -> new SlimeFunResidenceModule(plugin));
		    
		    plugin.consoleMessage("Enabled compatability with SlimeFun plugin");
		    Bukkit.getScheduler().cancelTask(repeat);
		}
		if (times > 60) {
		    plugin.consoleMessage("&cFailed to initialize SlimeFun support");
		    Bukkit.getScheduler().cancelTask(repeat);
		}
	    }, 20L, 20L);
	}
    }

}

package com.bekvon.bukkit.residence.slimeFun;

import org.bukkit.Bukkit;

import com.bekvon.bukkit.residence.Residence;

import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionManager;

public class SlimefunManager {

	// Fail safe to avoid infinite checks
	private final static int TRIES = 60;
	private static int times = 0;

	public static void register(Residence residence) {
		Bukkit.getServer().getScheduler().runTaskTimer(residence, task -> {

			ProtectionManager manager = SlimefunPlugin.getProtectionManager();

			// Wait for protectionManager to load (loaded on first server tick)
			if (manager != null) {
				manager.registerModule(Bukkit.getServer(), "Residence", plugin -> new SlimeFunResidenceModule(plugin));
				residence.consoleMessage("Enabled compatability with SlimeFun plugin");
				task.cancel();
			}

			if (++times >= TRIES) {
				residence.consoleMessage("&cFailed to initialize SlimeFun support");
				task.cancel();
			}
		}, 20, 20);
	}
}

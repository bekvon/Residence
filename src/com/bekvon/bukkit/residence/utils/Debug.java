package com.bekvon.bukkit.residence.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.CMIChatColor;

public class Debug {
    public static void D(Object message) {
	Player player = Bukkit.getPlayer("Zrips");
	if (player == null)
	    return; 
	player.sendMessage(ChatColor.DARK_GRAY + "[Residence Debug] " + ChatColor.DARK_AQUA + CMIChatColor.translate(message == null ? "Null" : message.toString()));
	return;
    }
}

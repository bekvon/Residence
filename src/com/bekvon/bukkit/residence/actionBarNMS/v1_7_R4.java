package com.bekvon.bukkit.residence.actionBarNMS;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.containers.ABInterface;

public class v1_7_R4 implements ABInterface {

    @Override
    public void send(CommandSender sender, String msg) {
	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @Override
    public void send(Player player, String msg) {
	player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @Override
    public void sendTitle(Player player, Object title, Object subtitle) {
	return;
    }
}

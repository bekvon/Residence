package com.bekvon.bukkit.residence.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.cmd;

public class pdel implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	String baseCmd = "res";
	if (resadmin)
	    baseCmd = "resadmin";
	if (args.length == 2) {
	    Bukkit.dispatchCommand(player, baseCmd + " pset " + args[1] + " trusted remove");
	    return true;
	}
	if (args.length == 3) {
	    Bukkit.dispatchCommand(player, baseCmd + " pset " + args[1] + " " + args[2] + " trusted remove");
	    return true;
	}
	return false;
    }

}

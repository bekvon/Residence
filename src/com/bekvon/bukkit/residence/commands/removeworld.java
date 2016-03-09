package com.bekvon.bukkit.residence.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public abstract class removeworld implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length != 2)
	    return false;

	if (sender instanceof ConsoleCommandSender) {
	    Residence.getResidenceManager().removeAllFromWorld(sender, args[1]);
	    return true;
	} else {
	    sender.sendMessage(ChatColor.RED + "MUST be run from console.");
	}

	return true;
    }
}

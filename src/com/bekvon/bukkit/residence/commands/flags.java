package com.bekvon.bukkit.residence.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.cmd;

public class flags implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}
	if (resadmin)
	    Bukkit.dispatchCommand(sender, "resadmin flags ? " + page);
	else
	    Bukkit.dispatchCommand(sender, "res flags ? " + page);
	return true;
    }
}

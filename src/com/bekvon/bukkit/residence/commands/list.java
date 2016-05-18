package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class list implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
//	if (!(sender instanceof Player))
//	    return false;
//
//	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if (args.length == 1 && sender instanceof Player) {
	    Residence.getResidenceManager().listResidences(sender);
	    return true;
	} else if (args.length == 2) {
	    try {
		Integer.parseInt(args[1]);
		Residence.getResidenceManager().listResidences(sender, page);
	    } catch (Exception ex) {
		Residence.getResidenceManager().listResidences(sender, args[1]);
	    }
	    return true;
	} else if (args.length == 3) {
	    Residence.getResidenceManager().listResidences(sender, args[1], page);
	    return true;
	}
	return false;
    }
}

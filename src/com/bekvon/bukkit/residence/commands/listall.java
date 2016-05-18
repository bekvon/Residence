package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class listall implements cmd {

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
	
	if (args.length == 1) {
	    Residence.getResidenceManager().listAllResidences(sender, 1);
	} else if (args.length == 2) {
	    try {
		Residence.getResidenceManager().listAllResidences(sender, page);
	    } catch (Exception ex) {
	    }
	} else {
	    return false;
	}
	return true;
    }
}

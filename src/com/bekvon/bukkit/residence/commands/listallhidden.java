package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class listallhidden implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}
	if (!resadmin) {
	    sender.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}
	if (args.length == 1) {
	    Residence.getResidenceManager().listAllResidences(sender, 1, true, true);
	} else if (args.length == 2) {
	    try {
		Residence.getResidenceManager().listAllResidences(sender, page, true, true);
	    } catch (Exception ex) {
	    }
	} else {
	    return false;
	}
	return true;
    }
}

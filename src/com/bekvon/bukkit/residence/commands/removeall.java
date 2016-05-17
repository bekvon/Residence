package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class removeall implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length != 2 && args.length != 1) {
	    return false;
	}
	
	String target = args.length == 2 ? args[1] : sender.getName();
	
	if (resadmin || target.equalsIgnoreCase(sender.getName())) {
	    Residence.getResidenceManager().removeAllByOwner(sender, target);
	    sender.sendMessage(Residence.getLM().getMessage("Residence.RemovePlayersResidences", target));
	} else {
	    sender.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	}
	return true;
    }

}

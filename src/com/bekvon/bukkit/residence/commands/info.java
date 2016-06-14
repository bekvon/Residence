package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class info implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length == 1 && sender instanceof Player) {
	    Player player = (Player) sender;
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		Residence.getResidenceManager().printAreaInfo(res.getName(), sender, resadmin);
	    } else {
		sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if (args.length == 2) {
	    Residence.getResidenceManager().printAreaInfo(args[1], sender, resadmin);
	    return true;
	}
	return false;
    }

}

package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class info implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length == 1 && sender instanceof Player) {
	    Player player = (Player) sender;
	    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    if (area != null) {
		Residence.getResidenceManager().printAreaInfo(area, sender, resadmin);
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

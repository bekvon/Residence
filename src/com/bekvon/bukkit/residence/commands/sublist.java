package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class sublist implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}
	if (args.length != 1 && args.length != 2 && args.length != 3)
	    return false;
	
	ClaimedResidence res;
	if (args.length == 1) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	} else {
	    res = Residence.getResidenceManager().getByName(args[1]);
	}
	if (res != null) {
	    res.printSubzoneList(player, page);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
	return true;
    }

}

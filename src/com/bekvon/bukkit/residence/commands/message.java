package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class message implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	ClaimedResidence res = null;
	int start = 0;
	boolean enter = false;
	if (args.length < 2) {
	    return false;
	}
	if (args[1].equals("enter")) {
	    enter = true;
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("leave")) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("remove")) {
	    if (args.length > 2 && args[2].equals("enter")) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, true, resadmin);
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		}
		return true;
	    } else if (args.length > 2 && args[2].equals("leave")) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, false, resadmin);
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		}
		return true;
	    }
	    player.sendMessage(Residence.getLM().getMessage("Invalid.MessageType"));
	    return true;
	} else if (args.length > 2 && args[2].equals("enter")) {
	    enter = true;
	    res = Residence.getResidenceManager().getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("leave")) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("remove")) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    if (args.length != 4) {
		return false;
	    }
	    if (args[3].equals("enter")) {
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, true, resadmin);
		}
		return true;
	    } else if (args[3].equals("leave")) {
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, false, resadmin);
		}
		return true;
	    }
	    player.sendMessage(Residence.getLM().getMessage("Invalid.MessageType"));
	    return true;
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.MessageType"));
	    return true;
	}
	if (start == 0) {
	    return false;
	}
	String message = "";
	for (int i = start; i < args.length; i++) {
	    message = message + args[i] + " ";
	}
	if (res != null) {
	    res.setEnterLeaveMessage(player, message, enter, resadmin);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
	return true;
    }
}

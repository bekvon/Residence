package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class command implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	ClaimedResidence res = null;
	String action = null;
	String cmd = null;
	if (args.length == 2 && sender instanceof Player) {
	    Player player = (Player) sender;
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    action = args[1];
	} else if (args.length == 3 && sender instanceof Player) {
	    Player player = (Player) sender;
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    action = args[1];
	    cmd = args[2];
	} else if (args.length == 4) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    action = args[2];
	    cmd = args[3];
	} else if (args.length == 3 && !(sender instanceof Player)) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    action = args[2];
	}

	if (res == null) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}

	if (!res.isOwner(sender.getName()) && !resadmin) {
	    sender.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return true;
	}

	if (action.equalsIgnoreCase("allow")) {
	    if (res.addCmdWhiteList(cmd)) {
		sender.sendMessage(Residence.getLM().getMessage("command.addedAllow", res.getName()));
	    } else
		sender.sendMessage(Residence.getLM().getMessage("command.removedAllow", res.getName()));
	} else if (action.equalsIgnoreCase("block")) {
	    if (res.addCmdBlackList(cmd)) {
		sender.sendMessage(Residence.getLM().getMessage("command.addedBlock", res.getName()));
	    } else
		sender.sendMessage(Residence.getLM().getMessage("command.removedBlock", res.getName()));
	} else if (action.equalsIgnoreCase("list")) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < res.getCmdBlackList().size(); i++) {
		sb.append("/" + res.getCmdBlackList().get(i).replace("_", " "));
		if (i + 1 < res.getCmdBlackList().size())
		    sb.append(", ");
	    }
	    sender.sendMessage(Residence.getLM().getMessage("command.Blocked", sb.toString()));

	    sb = new StringBuilder();
	    for (int i = 0; i < res.getCmdWhiteList().size(); i++) {
		sb.append("/" + res.getCmdWhiteList().get(i).replace("_", " "));
		if (i + 1 < res.getCmdWhiteList().size())
		    sb.append(", ");
	    }
	    sender.sendMessage(Residence.getLM().getMessage("command.Allowed", sb.toString()));
	} else
	    return false;

	return true;

    }

}

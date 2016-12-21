package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class message implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1000)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
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
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("leave")) {
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("remove")) {
	    if (args.length > 2 && args[2].equals("enter")) {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, true, resadmin);
		} else {
		    plugin.msg(player, lm.Invalid_Residence);
		}
		return true;
	    } else if (args.length > 2 && args[2].equals("leave")) {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, false, resadmin);
		} else {
		    plugin.msg(player, lm.Invalid_Residence);
		}
		return true;
	    }
	    plugin.msg(player, lm.Invalid_MessageType);
	    return true;
	} else if (args.length > 2 && args[2].equals("enter")) {
	    enter = true;
	    res = plugin.getResidenceManager().getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("leave")) {
	    res = plugin.getResidenceManager().getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("remove")) {
	    res = plugin.getResidenceManager().getByName(args[1]);
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
	    plugin.msg(player, lm.Invalid_MessageType);
	    return true;
	} else {
	    plugin.msg(player, lm.Invalid_MessageType);
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
	    plugin.msg(player, lm.Invalid_Residence);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage residence enter / leave messages");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res message <residence> [enter/leave] [message]",
	    "Set the enter or leave message of a residence.", "&eUsage: &6/res message <residence> remove [enter/leave]", "Removes a enter or leave message."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%enter%%leave", "enter%%leave"));
    }
}

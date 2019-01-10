package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class message implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1000)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	ClaimedResidence res = null;
	String message = null;
	Boolean enter = null;

	c: for (String one : plugin.reduceArgs(args)) {
	    if (message == null)
		switch (one.toLowerCase()) {
		case "enter":
		    if (enter == null) {
			enter = true;
			continue;
		    }
		    break;
		case "leave":
		    if (enter == null) {
			enter = false;
			continue;
		    }
		    break;
		case "remove":
		    break c;
		}

	    if (res == null && enter == null) {
		res = plugin.getResidenceManager().getByName(one);
		if (res != null)
		    continue;
	    }

	    if (message == null)
		message = "";
	    if (!message.isEmpty())
		message += " ";
	    message += one;
	}

	if (res == null && sender instanceof Player) {
	    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}

	if (res == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return true;
	}

	if (enter == null) {
	    plugin.msg(sender, lm.Invalid_MessageType);
	    return true;
	}

	if (enter && !plugin.hasPermission(sender, "residence.command.message.enter", true)) {
	    return true;
	}

	if (!enter && !plugin.hasPermission(sender, "residence.command.message.leave", true)) {
	    return true;
	}

	if (message == null && enter && !plugin.hasPermission(sender, "residence.command.message.enter.remove", true)) {
	    return true;
	}
	
	if (message == null && !enter && !plugin.hasPermission(sender, "residence.command.message.leave.remove", true)) {
	    return true;
	}
	
	res.setEnterLeaveMessage(sender, message, enter, resadmin);

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

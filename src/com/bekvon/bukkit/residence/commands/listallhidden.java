package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class listallhidden implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 4700)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}
	if (!resadmin) {
	    Residence.msg(sender, lm.General_NoPermission);
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

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "List All Hidden Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res listhidden <page>", "Lists all hidden residences on the server."));
    }
}

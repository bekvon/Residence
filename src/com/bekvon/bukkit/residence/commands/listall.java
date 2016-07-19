package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;

public class listall implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4200)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
//	if (!(sender instanceof Player))
//	    return false;
//
//	Player player = (Player) sender;

	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if (args.length == 1) {
	    Residence.getResidenceManager().listAllResidences(sender, 1, resadmin);
	} else if (args.length == 2) {
	    try {
		Residence.getResidenceManager().listAllResidences(sender, page, resadmin);
	    } catch (Exception ex) {
	    }
	} else {
	    return false;
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "List All Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res listall <page>", "Lists hidden residences for a player."));
    }
}

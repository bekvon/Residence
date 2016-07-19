package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;

public class list implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 300)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if (args.length == 1 && sender instanceof Player) {
	    Residence.getResidenceManager().listResidences(sender, resadmin);
	    return true;
	} else if (args.length == 2) {
	    try {
		Integer.parseInt(args[1]);
		Residence.getResidenceManager().listResidences(sender, page, resadmin);
	    } catch (Exception ex) {
		Residence.getResidenceManager().listResidences(sender, args[1], resadmin);
	    }
	    return true;
	} else if (args.length == 3) {
	    Residence.getResidenceManager().listResidences(sender, args[1], page, resadmin);
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "List Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res list <player> <page>",
	    "Lists all the residences a player owns (except hidden ones).",
	    "If listing your own residences, shows hidden ones as well.",
	    "To list everyones residences, use /res listall."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]"));
    }
}

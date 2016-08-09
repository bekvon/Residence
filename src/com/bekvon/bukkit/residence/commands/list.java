package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class list implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 300)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	int page = 1;
	World world = null;
	String target = null;

	c: for (int i = 1; i < args.length; i++) {
	    try {
		page = Integer.parseInt(args[i]);
		if (page < 1)
		    page = 1;
		continue;
	    } catch (Exception ex) {
	    }

	    for (World w : Bukkit.getWorlds()) {
		if (w.getName().equalsIgnoreCase(args[i])) {
		    world = w;
		    continue c;
		}
	    }
	    target = args[i];
	}
	
	if (target != null && !sender.getName().equalsIgnoreCase(target) && !sender.hasPermission("residence.command.list.others")){
	    Residence.msg(sender, lm.General_NoPermission);
	    return true;
	}

	Residence.getResidenceManager().listResidences(sender, target, page, false, false, resadmin, world);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "List Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res list <player> <page> <worldName>",
	    "Lists all the residences a player owns (except hidden ones).",
	    "If listing your own residences, shows hidden ones as well.",
	    "To list everyones residences, use /res listall."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]", "[worldname]"));
    }
}

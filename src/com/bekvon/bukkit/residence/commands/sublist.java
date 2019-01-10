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

public class sublist implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length != 1 && args.length != 2 && args.length != 3)
	    return false;

	int page = 0;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	ClaimedResidence res;
	if (args.length == 1 && sender instanceof Player) {
	    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
	} else {
	    res = plugin.getResidenceManager().getByName(args[1]);
	}

	if (page < 1)
	    page = 1;

	if (res != null) {
	    res.printSubzoneList(sender, page);
	} else {
	    plugin.msg(sender, lm.Invalid_Residence);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "List Residence Subzones");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res sublist <residence> <page>", "List subzones within a residence."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }

}

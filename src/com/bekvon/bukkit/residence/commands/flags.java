package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;

public class flags implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1200)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}
	if (resadmin)
	    Bukkit.dispatchCommand(sender, "resadmin flags ? " + page);
	else
	    Bukkit.dispatchCommand(sender, "res flags ? " + page);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "List of flags");
	c.get(path + "Info", Arrays.asList("For flag values, usually true allows the action, and false denys the action."));

	Set<String> keys = new HashSet<String>();
	if (c.getC().isConfigurationSection(path + "SubCommands")){
	    keys = c.getC().getConfigurationSection(path + "SubCommands").getKeys(false);
	}

	for (Flags fl : Flags.values()) {
	    String pt = path + "SubCommands." + fl.toString();
	    c.get(pt + ".Translated", fl.toString());
	    c.get(pt + ".Description", fl.getDesc());
	    String forSet = "set/pset";
	    switch (fl.getFlagMode()) {
	    case Group:
	    case Player:
		forSet = "pset";
		break;
	    case Residence:
		forSet = "set";
		break;
	    case Both:
	    default:
		break;
	    }

	    c.get(pt + ".Info", Arrays.asList("&eUsage: &6/res " + forSet + " <residence> " + fl.getName() + " true/false/remove"));
	    keys.remove(fl.toString());
	}

	for (String fl : keys) {
	    String pt = path + "SubCommands." + fl;
//	    No translation for custom flags for now
//	    c.get(pt + ".Translated", c.getC().getString(pt + ".Translated"));
	    c.get(pt + ".Description", c.getC().getString(pt + ".Description"));
	    c.get(pt + ".Info", c.getC().getStringList(pt + ".Info"));
	}

	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "pset"), Arrays.asList("[residence]", "[flag]",
	    "[true%%false%%remove]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "set"), Arrays.asList("[residence]", "[flag]",
	    "[true%%false%%remove]"));
    }
}

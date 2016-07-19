package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class removeall implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5100)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length != 2 && args.length != 1) {
	    return false;
	}

	String target = args.length == 2 ? args[1] : sender.getName();

	if (resadmin) {
	    Residence.getResidenceManager().removeAllByOwner(target);
	    Residence.msg(sender, lm.Residence_RemovePlayersResidences, target);
	} else {
	    Residence.msg(sender, lm.General_NoPermission);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Remove all residences owned by a player.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res removeall [owner]",
	    "Removes all residences owned by a specific player.'", "Requires /resadmin if you use it on anyone besides yourself."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]"));
    }

}

package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;

public class mirror implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3700)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 3)
	    return false;

	Residence.getResidenceManager().mirrorPerms(player, args[2], args[1], resadmin);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Mirrors Flags");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res mirror [Source Residence] [Target Residence]",
	    "Mirrors flags from one residence to another.  You must be owner of both or a admin to do this."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]", "[residence]"));
    }
}

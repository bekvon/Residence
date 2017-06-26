package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;

public class give implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3800)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	boolean includeSubzones = false;

	if (args.length != 3 && args.length != 4)
	    return false;

	for (String one : args) {
	    if (one.equalsIgnoreCase("-s"))
		includeSubzones = true;
	}

	plugin.getResidenceManager().giveResidence(player, args[2], args[1], resadmin, includeSubzones);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Give residence to player.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res give <residence name> [player] <-s>", "Gives your owned residence to target player"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]", "[playername]"));
    }
}

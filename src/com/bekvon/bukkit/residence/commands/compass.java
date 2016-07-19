package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class compass implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3200)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 2) {
	    player.setCompassTarget(player.getWorld().getSpawnLocation());
	    Residence.msg(player, lm.General_CompassTargetReset);
	    return true;
	}

	if (!player.hasPermission("residence.compass")) {
	    Residence.msg(player, lm.General_NoPermission);
	    return true;
	}

	if (Residence.getResidenceManager().getByName(args[1]) != null) {
	    if (Residence.getResidenceManager().getByName(args[1]).getWorld().equalsIgnoreCase(player.getWorld().getName())) {
		Location low = Residence.getResidenceManager().getByName(args[1]).getArea("main").getLowLoc();
		Location high = Residence.getResidenceManager().getByName(args[1]).getArea("main").getHighLoc();
		Location mid = new Location(low.getWorld(), (low.getBlockX() + high.getBlockX()) / 2, (low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high
		    .getBlockZ()) / 2);
		player.setCompassTarget(mid);
		Residence.msg(player, lm.General_CompassTargetSet, args[1]);
	    }
	} else {
	    Residence.msg(player, lm.Invalid_Residence);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Set compass ponter to residence location");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res compass <residence>"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

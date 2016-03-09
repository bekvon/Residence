package com.bekvon.bukkit.residence.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class compass implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 2) {
	    player.setCompassTarget(player.getWorld().getSpawnLocation());
	    player.sendMessage(Residence.getLM().getMessage("General.CompassTargetReset"));
	    return true;
	}

	if (!player.hasPermission("residence.compass")) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}

	if (Residence.getResidenceManager().getByName(args[1]) != null) {
	    if (Residence.getResidenceManager().getByName(args[1]).getWorld().equalsIgnoreCase(player.getWorld().getName())) {
		Location low = Residence.getResidenceManager().getByName(args[1]).getArea("main").getLowLoc();
		Location high = Residence.getResidenceManager().getByName(args[1]).getArea("main").getHighLoc();
		Location mid = new Location(low.getWorld(), (low.getBlockX() + high.getBlockX()) / 2, (low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high
		    .getBlockZ()) / 2);
		player.setCompassTarget(mid);
		player.sendMessage(Residence.getLM().getMessage("General.CompassTargetSet", args[1]));
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
	return true;
    }

}

package com.bekvon.bukkit.residence.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.utils.RandomTp;

public class rt implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1) {
	    return false;
	}

	int sec = Residence.getConfigManager().getrtCooldown();
	if (Residence.getRandomTeleportMap().containsKey(player.getName()) && !resadmin) {
	    if (Residence.getRandomTeleportMap().get(player.getName()) + (sec * 1000) > System.currentTimeMillis()) {
		int left = (int) (sec - ((System.currentTimeMillis() - Residence.getRandomTeleportMap().get(player.getName())) / 1000));
		player.sendMessage(Residence.getLM().getMessage("RandomTeleport.TpLimit", left));
		return true;
	    }
	}

	if (!player.hasPermission("residence.randomtp") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}

	Location loc = RandomTp.getRandomlocation(player.getLocation().getWorld().getName());
	Residence.getRandomTeleportMap().put(player.getName(), System.currentTimeMillis());

	if (loc == null) {
	    player.sendMessage(Residence.getLM().getMessage("RandomTeleport.IncorrectLocation", sec));
	    return true;
	}

	if (Residence.getConfigManager().getTeleportDelay() > 0 && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("RandomTeleport.TeleportStarted", loc.getX(), loc.getY(), loc
		.getZ(), Residence.getConfigManager().getTeleportDelay()));
	    Residence.getTeleportDelayMap().add(player.getName());
	    Residence.getRandomTpManager().performDelaydTp(loc, player);
	} else
	    RandomTp.performInstantTp(loc, player);

	return true;
    }
}

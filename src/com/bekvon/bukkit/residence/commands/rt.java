package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class rt implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2500)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1 && args.length != 2) {
	    return false;
	}

	int sec = Residence.getConfigManager().getrtCooldown();
	if (Residence.getRandomTeleportMap().containsKey(player.getName()) && !resadmin) {
	    if (Residence.getRandomTeleportMap().get(player.getName()) + (sec * 1000) > System.currentTimeMillis()) {
		int left = (int) (sec - ((System.currentTimeMillis() - Residence.getRandomTeleportMap().get(player.getName())) / 1000));
		Residence.msg(player, lm.RandomTeleport_TpLimit, left);
		return true;
	    }
	}

	if (!player.hasPermission("residence.randomtp") && !resadmin) {
	    Residence.msg(player, lm.General_NoPermission);
	    return true;
	}

	String wname = null;

	if (args.length == 2) {
	    for (RandomTeleport one : Residence.getConfigManager().getRandomTeleport()) {
		if (!one.getWorld().equalsIgnoreCase(args[1]))
		    continue;
		wname = one.getWorld();
		break;
	    }

	    if (wname == null) {
		Residence.msg(sender, lm.Invalid_World);

		String worlds = "";

		for (RandomTeleport one : Residence.getConfigManager().getRandomTeleport()) {
		    worlds += one.getWorld() + " ";
		    break;
		}

		Residence.msg(sender, lm.RandomTeleport_WorldList, worlds);
		return true;
	    }
	}
	if (wname == null)
	    wname = player.getLocation().getWorld().getName();

	Location loc = Residence.getRandomTpManager().getRandomlocation(wname);
	Residence.getRandomTeleportMap().put(player.getName(), System.currentTimeMillis());

	if (loc == null) {
	    Residence.msg(player, lm.RandomTeleport_IncorrectLocation, sec);
	    return true;
	}

	if (Residence.getConfigManager().getTeleportDelay() > 0 && !resadmin) {
	    Residence.msg(player, lm.RandomTeleport_TeleportStarted, loc.getX(), loc.getY(), loc
		.getZ(), Residence.getConfigManager().getTeleportDelay());
	    Residence.getTeleportDelayMap().add(player.getName());
	    Residence.getRandomTpManager().performDelaydTp(loc, player);
	} else
	    Residence.getRandomTpManager().performInstantTp(loc, player);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Teleports to random location in world");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res rt", "Teleports you to random location in defined world."));
    }
}

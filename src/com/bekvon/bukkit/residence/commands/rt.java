package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
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
	if (args.length != 1 && args.length != 2 && args.length != 3) {
	    return false;
	}

	if (!sender.hasPermission("residence.randomtp") && !resadmin) {
	    Residence.msg(sender, lm.General_NoPermission);
	    return true;
	}

	String wname = null;

	Player tPlayer = null;

	if (args.length > 1) {
	    c: for (int i = 1; i < args.length; i++) {
		for (RandomTeleport one : Residence.getConfigManager().getRandomTeleport()) {
		    if (!one.getWorld().equalsIgnoreCase(args[i]))
			continue;
		    wname = one.getWorld();
		    continue c;
		}
		Player p = Bukkit.getPlayer(args[i]);
		if (p != null)
		    tPlayer = p;
	    }
	}

	if (args.length > 1 && wname == null && tPlayer == null) {
	    Residence.msg(sender, lm.Invalid_World);
	    String worlds = "";
	    for (RandomTeleport one : Residence.getConfigManager().getRandomTeleport()) {
		worlds += one.getWorld() + " ";
		break;
	    }
	    Residence.msg(sender, lm.RandomTeleport_WorldList, worlds);
	    return true;
	}
	
	if (tPlayer == null && sender instanceof Player)
	    tPlayer = (Player) sender;

	if (wname == null && tPlayer != null)
	    wname = tPlayer.getLocation().getWorld().getName();

	if (wname == null && tPlayer == null) {
	    Residence.msg(sender, lm.Invalid_World);
	    String worlds = "";
	    for (RandomTeleport one : Residence.getConfigManager().getRandomTeleport()) {
		worlds += one.getWorld() + " ";
		break;
	    }
	    Residence.msg(sender, lm.RandomTeleport_WorldList, worlds);
	    return true;
	}

	if (tPlayer == null)
	    return false;

	if (!sender.getName().equalsIgnoreCase(tPlayer.getName()) && !sender.hasPermission("residence.randomtp.admin"))
	    return false;

	int sec = Residence.getConfigManager().getrtCooldown();
	if (Residence.getRandomTeleportMap().containsKey(tPlayer.getName()) && !resadmin) {
	    if (Residence.getRandomTeleportMap().get(tPlayer.getName()) + (sec * 1000) > System.currentTimeMillis()) {
		int left = (int) (sec - ((System.currentTimeMillis() - Residence.getRandomTeleportMap().get(tPlayer.getName())) / 1000));
		Residence.msg(tPlayer, lm.RandomTeleport_TpLimit, left);
		return true;
	    }
	}

	Location loc = Residence.getRandomTpManager().getRandomlocation(wname);
	Residence.getRandomTeleportMap().put(tPlayer.getName(), System.currentTimeMillis());

	if (loc == null) {
	    Residence.msg(sender, lm.RandomTeleport_IncorrectLocation, sec);
	    return true;
	}

	if (Residence.getConfigManager().getTeleportDelay() > 0 && !resadmin) {
	    Residence.msg(tPlayer, lm.RandomTeleport_TeleportStarted, loc.getX(), loc.getY(), loc
		.getZ(), Residence.getConfigManager().getTeleportDelay());
	    Residence.getTeleportDelayMap().add(tPlayer.getName());
	    Residence.getRandomTpManager().performDelaydTp(loc, tPlayer);
	} else
	    Residence.getRandomTpManager().performInstantTp(loc, tPlayer);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Teleports to random location in world");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res rt (worldname) (playerName)", "Teleports you to random location in defined world."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[worldname]", "[playername]"));
    }
}

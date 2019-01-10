package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.utils.Debug;

public class rt implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2500)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length != 1 && args.length != 2 && args.length != 3) {
	    return false;
	}

	if (!resadmin && !plugin.hasPermission(sender, "residence.randomtp"))
	    return true;

	World wname = null;

	Player tPlayer = null;

	if (args.length > 1) {
	    c: for (int i = 1; i < args.length; i++) {
		for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
		    if (!one.getCenter().getWorld().getName().equalsIgnoreCase(args[i]))
			continue;
		    wname = one.getCenter().getWorld();
		    continue c;
		}
		Player p = Bukkit.getPlayer(args[i]);
		if (p != null)
		    tPlayer = p;
	    }
	}

	if (args.length > 1 && wname == null && tPlayer == null) {
	    plugin.msg(sender, lm.Invalid_World);
	    String worlds = "";
	    for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
		if (!worlds.isEmpty())
		    worlds += ", ";
		worlds += one.getCenter().getWorld().getName();
		break;
	    }
	    plugin.msg(sender, lm.RandomTeleport_WorldList, worlds);
	    return true;
	}

	if (tPlayer == null && sender instanceof Player)
	    tPlayer = (Player) sender;

	if (wname == null && tPlayer != null)
	    wname = tPlayer.getLocation().getWorld();

	if (wname == null && tPlayer == null) {
	    plugin.msg(sender, lm.Invalid_World);
	    String worlds = "";
	    for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
		if (!worlds.isEmpty())
		    worlds += ", ";
		worlds += one.getCenter().getWorld().getName();
		break;
	    }
	    plugin.msg(sender, lm.RandomTeleport_WorldList, worlds);
	    return true;
	}

	if (tPlayer == null)
	    return false;

	if (!sender.getName().equalsIgnoreCase(tPlayer.getName()) && !plugin.hasPermission(sender, "residence.randomtp.admin"))
	    return false;

	int sec = plugin.getConfigManager().getrtCooldown();
	if (plugin.getRandomTeleportMap().containsKey(tPlayer.getName()) && !resadmin && !plugin.hasPermission(sender, "residence.randomtp.cooldownbypass", false)) {
	    if (plugin.getRandomTeleportMap().get(tPlayer.getName()) + (sec * 1000) > System.currentTimeMillis()) {
		int left = (int) (sec - ((System.currentTimeMillis() - plugin.getRandomTeleportMap().get(tPlayer.getName())) / 1000));
		plugin.msg(tPlayer, lm.RandomTeleport_TpLimit, left);
		return true;
	    }
	}
	if (!plugin.getRandomTpManager().isDefinedRnadomTp(wname)) {
	    plugin.msg(sender, lm.RandomTeleport_Disabled);
	    return true;
	}
	Long time = System.currentTimeMillis();
	Location loc = plugin.getRandomTpManager().getRandomlocation(wname);
	plugin.getRandomTeleportMap().put(tPlayer.getName(), System.currentTimeMillis());

	if (loc == null) {
	    plugin.msg(sender, lm.RandomTeleport_IncorrectLocation, sec);
	    return true;
	}

	if (plugin.getConfigManager().getTeleportDelay() > 0 && !resadmin && !plugin.hasPermission(sender, "residence.randomtp.delaybypass", false)) {
	    plugin.msg(tPlayer, lm.RandomTeleport_TeleportStarted, loc.getX(), loc.getY(), loc
		.getZ(), plugin.getConfigManager().getTeleportDelay());
	    plugin.getTeleportDelayMap().add(tPlayer.getName());
	    plugin.getRandomTpManager().performDelaydTp(loc, tPlayer);
	} else
	    plugin.getRandomTpManager().performInstantTp(loc, tPlayer);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Teleports to random location in world");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res rt (worldname) (playerName)", "Teleports you to random location in defined world."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[worldname]", "[playername]"));
    }
}

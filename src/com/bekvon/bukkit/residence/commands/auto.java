package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

public class auto implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 150)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2 && args.length != 1 && args.length != 3) {
	    return false;
	}

	String resName = null;

	if (args.length > 1)
	    resName = args[1];
	else
	    resName = player.getName();

	int lenght = -1;
	if (args.length == 3) {
	    try {
		lenght = Integer.parseInt(args[2]);
	    } catch (Exception ex) {
	    }
	}

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);

	PermissionGroup group = rPlayer.getGroup();

	Location loc = player.getLocation();

	int X = group.getMaxX();
	int Y = group.getMaxY();
	int Z = group.getMaxZ();

	if (lenght > 0) {
	    if (lenght < X)
		X = lenght;
	    if (lenght < Z)
		Z = lenght;
	}

	int rX = (X - 1) / 2;
	int rY = (Y - 1) / 2;
	int rZ = (Z - 1) / 2;

	int minX = loc.getBlockX() - rX;
	int maxX = loc.getBlockX() + rX;

	if (maxX - minX + 1 < X)
	    maxX++;

	int minY = loc.getBlockY() - rY;
	int maxY = loc.getBlockY() + rY;

	if (maxY - minY + 1 < Y)
	    maxY++;

	if (minY < 0) {
	    maxY += -minY;
	    minY = 0;
	}

	if (maxY > loc.getWorld().getMaxHeight()) {
	    int dif = maxY - loc.getWorld().getMaxHeight();
	    if (minY > 0)
		minY -= dif;
	    if (minY < 0)
		minY = 0;
	    maxY = loc.getWorld().getMaxHeight() - 1;
	}

	int minZ = loc.getBlockZ() - rZ;
	int maxZ = loc.getBlockZ() + rZ;
	if (maxZ - minZ + 1 < Z)
	    maxZ++;

	Residence.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), minX, minY, minZ), false);
	Residence.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), maxX, maxY, maxZ), false);
	
	player.performCommand("res create " + resName);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Create maximum allowed residence around you");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res auto (residence name) (radius)"));
    }
}

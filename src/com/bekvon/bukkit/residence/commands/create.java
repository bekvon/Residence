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
import com.bekvon.bukkit.residence.selection.WorldGuardUtil;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class create implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 100)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}

	if (Residence.getWEplugin() != null) {
	    if (Residence.getWEplugin().getConfig().getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
		Residence.getSelectionManager().worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wg != null) {
		if (WorldGuardUtil.isSelectionInRegion(player) == null) {
		    Residence.getResidenceManager().addResidence(player, args[1], Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence
			.getSelectionManager().getPlayerLoc2(player.getName()), resadmin);
		    return true;
		}
		ProtectedRegion Region = WorldGuardUtil.isSelectionInRegion(player);
		Residence.msg(player, lm.Select_WorldGuardOverlap, Region.getId());

		Location lowLoc = new Location(Residence.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMinimumPoint().getBlockX(),
		    Region.getMinimumPoint().getBlockY(), Region.getMinimumPoint().getBlockZ());

		Location highLoc = new Location(Residence.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMaximumPoint().getBlockX(),
		    Region.getMaximumPoint().getBlockY(), Region.getMaximumPoint().getBlockZ());

		Residence.getSelectionManager().NewMakeBorders(player, lowLoc, highLoc, true);
		Residence.getSelectionManager().NewMakeBorders(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence
		    .getSelectionManager().getPlayerLoc2(player.getName()), false);
		return true;
	    }
	    Residence.getResidenceManager().addResidence(player, args[1], Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence
		.getSelectionManager().getPlayerLoc2(player.getName()), resadmin);
	    return true;
	}
	Residence.msg(player, lm.Select_Points);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Create Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res create <residence name>"));
    }
}

package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class subzone implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2100)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2 && args.length != 3) {
	    return false;
	}
	String zname;
	String parent;
	if (args.length == 2) {
	    parent = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    zname = args[1];
	} else {
	    parent = args[1];
	    zname = args[2];
	}

	if (Residence.getWEplugin() != null) {
	    if (Residence.getWEplugin().getConfig().getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
		Residence.getSelectionManager().worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(parent);
	    if (res == null) {
		Residence.msg(player, lm.Invalid_Residence);
		return true;
	    }

	    if (!player.hasPermission("residence.create.subzone") && !resadmin) {
		Residence.msg(player, lm.Subzone_CantCreate);
		return false;
	    }

	    res.addSubzone(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(player.getName()),
		zname, resadmin);
	} else {
	    Residence.msg(player, lm.Select_Points);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Create subzones in residences.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res subzone <residence> [subzone name]",
	    "If residence name is left off, will attempt to use residence your standing in."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }

}

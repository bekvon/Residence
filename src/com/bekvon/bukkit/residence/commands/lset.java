package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class lset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 5000)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	ClaimedResidence res = null;
	Material mat = null;
	String listtype = null;
	boolean showinfo = false;
	if (args.length == 2 && args[1].equals("info")) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    showinfo = true;
	} else if (args.length == 3 && args[2].equals("info")) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    showinfo = true;
	}
	if (showinfo) {
	    if (res == null) {
		Residence.msg(player, lm.Invalid_Residence);
		return true;
	    }
	    Residence.msg(player, lm.General_Blacklist);
	    res.getItemBlacklist().printList(player);
	    Residence.msg(player, lm.General_Ignorelist);
	    res.getItemIgnoreList().printList(player);
	    return true;
	} else if (args.length == 4) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    listtype = args[2];
	    try {
		mat = Material.valueOf(args[3].toUpperCase());
	    } catch (Exception ex) {
		Residence.msg(player, lm.Invalid_Material);
		return true;
	    }
	} else if (args.length == 3) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    listtype = args[1];
	    try {
		mat = Material.valueOf(args[2].toUpperCase());
	    } catch (Exception ex) {
		Residence.msg(player, lm.Invalid_Material);
		return true;
	    }
	}
	if (res != null) {
	    if (listtype != null && listtype.equalsIgnoreCase("blacklist")) {
		res.getItemBlacklist().playerListChange(player, mat, resadmin);
	    } else if (listtype != null && listtype.equalsIgnoreCase("ignorelist")) {
		res.getItemIgnoreList().playerListChange(player, mat, resadmin);
	    } else {
		Residence.msg(player, lm.Invalid_List);
	    }
	    return true;
	}
	Residence.msg(player, lm.Invalid_Residence);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Change blacklist and ignorelist options");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res lset <residence> [blacklist/ignorelist] [material]",
	    "&eUsage: &6/res lset <residence> Info",
	    "Blacklisting a material prevents it from being placed in the residence.",
	    "Ignorelist causes a specific material to not be protected by Residence."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()),
	    Arrays.asList("[residence]%%blacklist%%ignorelist", "blacklist%%ignorelist%%[material]", "[material]"));
    }
}

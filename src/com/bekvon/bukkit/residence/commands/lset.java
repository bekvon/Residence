package com.bekvon.bukkit.residence.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class lset implements cmd {

    @Override
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
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }
	    player.sendMessage(Residence.getLM().getMessage("General.Blacklist"));
	    res.getItemBlacklist().printList(player);
	    player.sendMessage(Residence.getLM().getMessage("General.Ignorelist"));
	    res.getItemIgnoreList().printList(player);
	    return true;
	} else if (args.length == 4) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    listtype = args[2];
	    try {
		mat = Material.valueOf(args[3].toUpperCase());
	    } catch (Exception ex) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Material"));
		return true;
	    }
	} else if (args.length == 3) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    listtype = args[1];
	    try {
		mat = Material.valueOf(args[2].toUpperCase());
	    } catch (Exception ex) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Material"));
		return true;
	    }
	}
	if (res != null) {
	    if (listtype.equalsIgnoreCase("blacklist")) {
		res.getItemBlacklist().playerListChange(player, mat, resadmin);
	    } else if (listtype.equalsIgnoreCase("ignorelist")) {
		res.getItemIgnoreList().playerListChange(player, mat, resadmin);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.List"));
	    }
	    return true;
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}
    }
}

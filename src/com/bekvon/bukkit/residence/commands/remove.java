package com.bekvon.bukkit.residence.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class remove implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	Player player = null;
	if (sender instanceof Player) {
	    player = (Player) sender;

	    if (Residence.deleteConfirm.containsKey(player.getName()))
		Residence.deleteConfirm.remove(player.getName());

	    if (args.length == 1) {

		ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());

		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		    return false;
		}

		if (res.isSubzone() && !player.hasPermission("residence.delete.subzone") && !resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("Subzone.CantDelete"));
		    return false;
		}

		if (res.isSubzone() && player.hasPermission("residence.delete.subzone") && !resadmin && Residence.getConfigManager().isPreventSubZoneRemoval() && !res
		    .getParent().isOwner(player)) {
		    player.sendMessage(Residence.getLM().getMessage("Subzone.CantDeleteNotOwnerOfParent"));
		    return false;
		}

		if (!res.isSubzone() && !player.hasPermission("residence.delete") && !resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.CantDeleteResidence"));
		    return false;
		}

		if (res.isSubzone()) {
		    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		    String[] split = area.split("\\.");
		    String words = split[split.length - 1];
		    if (!Residence.deleteConfirm.containsKey(player.getName()) || !area.equalsIgnoreCase(Residence.deleteConfirm.get(player.getName()))) {
			player.sendMessage(Residence.getLM().getMessage("Subzone.DeleteConfirm", words));
			Residence.deleteConfirm.put(player.getName(), area);
		    } else {
			Residence.getResidenceManager().removeResidence(player, area, resadmin);
		    }
		    return true;
		} else {
		    if (!Residence.deleteConfirm.containsKey(player.getName()) || !res.getName().equalsIgnoreCase(Residence.deleteConfirm.get(player.getName()))) {
			player.sendMessage(Residence.getLM().getMessage("Residence.DeleteConfirm", res.getName()));
			Residence.deleteConfirm.put(player.getName(), res.getName());
		    } else {
			Residence.getResidenceManager().removeResidence(player, res.getName(), resadmin);
		    }
		    return true;
		}

	    }
	}
	if (args.length != 2) {
	    return false;
	}
	if (player != null) {
	    if (!Residence.deleteConfirm.containsKey(player.getName()) || !args[1].equalsIgnoreCase(Residence.deleteConfirm.get(player.getName()))) {
		String words = "";
		if (Residence.getResidenceManager().getByName(args[1]) != null) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		    if (res.getParent() != null) {
			String[] split = args[1].split("\\.");
			words = split[split.length - 1];
		    }
		}
		if (words == "") {
		    player.sendMessage(Residence.getLM().getMessage("Subzone.DeleteConfirm", args[1]));
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Subzone.DeleteConfirm", words));
		}
		Residence.deleteConfirm.put(player.getName(), args[1]);
	    } else {
		Residence.getResidenceManager().removeResidence(player, args[1], resadmin);
	    }
	} else {
	    if (!Residence.deleteConfirm.containsKey("Console") || !args[1].equalsIgnoreCase(Residence.deleteConfirm.get("Console"))) {
		String words = "";
		if (Residence.getResidenceManager().getByName(args[1]) != null) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		    if (res.getParent() != null) {
			String[] split = args[1].split("\\.");
			words = split[split.length - 1];
		    }
		}
		if (words == "") {
		    Bukkit.getConsoleSender().sendMessage(Residence.getLM().getMessage("Subzone.DeleteConfirm", args[1]));
		} else {
		    Bukkit.getConsoleSender().sendMessage(Residence.getLM().getMessage("Subzone.DeleteConfirm", words));
		}
		Residence.deleteConfirm.put("Console", args[1]);
	    } else {
		Residence.getResidenceManager().removeResidence(args[1]);
	    }
	}
	return true;
    }

}

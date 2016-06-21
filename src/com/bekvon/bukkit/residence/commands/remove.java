package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class remove implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	ClaimedResidence res = null;
	String senderName = sender.getName();
	if (args.length == 2) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	} else if (sender instanceof Player && args.length == 1) {
	    res = Residence.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}

	if (res == null) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}

	if (res.isSubzone() && !sender.hasPermission("residence.delete.subzone") && !resadmin) {
	    sender.sendMessage(Residence.getLM().getMessage("Subzone.CantDelete"));
	    return true;
	}

	if (res.isSubzone() &&
	    sender.hasPermission("residence.delete.subzone") &&
	    !resadmin &&
	    Residence.getConfigManager().isPreventSubZoneRemoval() &&
	    !res.getParent().isOwner(senderName)) {
	    sender.sendMessage(Residence.getLM().getMessage("Subzone.CantDeleteNotOwnerOfParent"));
	    return true;
	}

	if (!res.isSubzone() && !sender.hasPermission("residence.delete") && !resadmin) {
	    sender.sendMessage(Residence.getLM().getMessage("Residence.CantDeleteResidence"));
	    return true;
	}

	if (Residence.deleteConfirm.containsKey(senderName))
	    Residence.deleteConfirm.remove(senderName);

	String resname = res.getName();

	if (!Residence.deleteConfirm.containsKey(senderName) || !resname.equalsIgnoreCase(Residence.deleteConfirm.get(senderName))) {
	    if (res.isSubzone())
		sender.sendMessage(Residence.getLM().getMessage("Subzone.DeleteConfirm", res.getResidenceName()));
	    else
		sender.sendMessage(Residence.getLM().getMessage("Residence.DeleteConfirm", res.getResidenceName()));
	    Residence.deleteConfirm.put(senderName, resname);
	} else {
	    Residence.getResidenceManager().removeResidence(sender, resname, resadmin);
	}
	return true;
    }
}

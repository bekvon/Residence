package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class remove implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2300)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	ClaimedResidence res = null;
	String senderName = sender.getName();
	if (args.length == 2) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	} else if (sender instanceof Player && args.length == 1) {
	    res = Residence.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}

	if (res == null) {
	    Residence.msg(sender, lm.Invalid_Residence);
	    return true;
	}

	if (res.isSubzone() && !sender.hasPermission("residence.delete.subzone") && !resadmin) {
	    Residence.msg(sender, lm.Subzone_CantDelete);
	    return true;
	}

	if (res.isSubzone() &&
	    sender.hasPermission("residence.delete.subzone") &&
	    !resadmin &&
	    Residence.getConfigManager().isPreventSubZoneRemoval() &&
	    !res.getParent().isOwner(senderName)) {
	    Residence.msg(sender, lm.Subzone_CantDeleteNotOwnerOfParent);
	    return true;
	}

	if (!res.isSubzone() && !sender.hasPermission("residence.delete") && !resadmin) {
	    Residence.msg(sender, lm.Residence_CantDeleteResidence);
	    return true;
	}

	if (Residence.deleteConfirm.containsKey(senderName))
	    Residence.deleteConfirm.remove(senderName);

	String resname = res.getName();

	if (!Residence.deleteConfirm.containsKey(senderName) || !resname.equalsIgnoreCase(Residence.deleteConfirm.get(senderName))) {
	    String cmd = "res";
	    if (resadmin)
		cmd = "resadmin";
	    if (sender instanceof Player) {
		String raw = "";
		if (res.isSubzone()) {
		    raw = Residence.getResidenceManager().convertToRaw(null, Residence.msg(lm.Subzone_DeleteConfirm, res.getResidenceName()),
			"Click to confirm", cmd + " confirm");
		} else {
		    raw = Residence.getResidenceManager().convertToRaw(null, Residence.msg(lm.Residence_DeleteConfirm, res.getResidenceName()),
			"Click to confirm", cmd + " confirm");
		}
		if (Residence.msg(lm.Subzone_DeleteConfirm, res.getResidenceName()).length() > 0)
		    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + raw);
	    } else {
		if (res.isSubzone())
		    Residence.msg(sender, lm.Subzone_DeleteConfirm, res.getResidenceName());
		else
		    Residence.msg(sender, lm.Residence_DeleteConfirm, res.getResidenceName());
	    }
	    Residence.deleteConfirm.put(senderName, resname);
	} else {
	    Residence.getResidenceManager().removeResidence(sender, resname, resadmin);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Remove residences.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res remove <residence name>"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

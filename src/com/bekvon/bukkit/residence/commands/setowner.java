package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class setowner implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5500)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length < 3)
	    return false;

	if (!resadmin) {
	    plugin.msg(sender, lm.General_NoPermission);
	    return true;
	}

	ClaimedResidence area = plugin.getResidenceManager().getByName(args[1]);
	if (area != null) {
	    area.getPermissions().setOwner(args[2], true);
	    if (plugin.getRentManager().isForRent(area.getName()))
		plugin.getRentManager().removeRentable(area.getName());
	    if (plugin.getTransactionManager().isForSale(area.getName()))
		plugin.getTransactionManager().removeFromSale(area.getName());
	    area.getPermissions().applyDefaultFlags();

	    plugin.getSignUtil().updateSignResName(area);
	    
	    if (area.getParent() == null) {
		plugin.msg(sender, lm.Residence_OwnerChange, args[1], args[2]);
	    } else {
		plugin.msg(sender, lm.Subzone_OwnerChange, args[1].split("\\.")[args[1].split("\\.").length - 1], args[2]);
	    }
	} else {
	    plugin.msg(sender, lm.Invalid_Residence);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Change owner of a residence.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/resadmin setowner [residence] [player]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]"));
    }

}

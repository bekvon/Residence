package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class reset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4400)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length != 2 && args.length != 1)
	    return false;

	String residenceName = null;
	if (args.length == 2)
	    residenceName = args[1];

	ClaimedResidence res = null;
	if (residenceName != null && !residenceName.equalsIgnoreCase("all"))
	    res = plugin.getResidenceManager().getByName(residenceName);
	if (args.length == 1 && sender instanceof Player)
	    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

	if (residenceName != null && !residenceName.equalsIgnoreCase("all") && res == null || args.length == 1  && res == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return true;
	}

	if (res != null) {
	    if (!resadmin && !res.isOwner(sender)) {
		plugin.msg(sender, lm.Residence_NotOwner);
		return true;
	    }
	    res.getPermissions().applyDefaultFlags();
	    plugin.msg(sender, lm.Flag_reset, res.getName());
	    return true;
	}

	if (!resadmin) {
	    plugin.msg(sender, lm.General_AdminOnly);
	    return true;
	}

	int count = 0;
	for (World oneW : Bukkit.getWorlds()) {
	    for (ClaimedResidence one : plugin.getResidenceManager().getFromAllResidences(true, false, oneW)) {
		one.getPermissions().applyDefaultFlags();
		count++;
	    }
	}
	plugin.msg(sender, lm.Flag_resetAll, count);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Reset residence to default flags.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res reset <residence/all>",
	    "Resets the flags on a residence to their default.  You must be the owner or an admin to do this."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

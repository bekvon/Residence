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

public class info implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 600)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length == 1 && sender instanceof Player) {
	    Player player = (Player) sender;
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		Residence.getResidenceManager().printAreaInfo(res.getName(), sender, resadmin);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if (args.length == 2) {
	    Residence.getResidenceManager().printAreaInfo(args[1], sender, resadmin);
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Show info on a residence.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res info <residence>", "Leave off <residence> to display info for the residence your currently in."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

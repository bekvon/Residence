package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.Debug;

public class info implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 600)
    public boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	Debug.D("check"); 
	if (args.length == 0 && sender instanceof Player) {
	    Player player = (Player) sender;
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		plugin.getResidenceManager().printAreaInfo(res.getName(), sender, resadmin);
	    } else {
		plugin.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if (args.length == 1) {
	    plugin.getResidenceManager().printAreaInfo(args[0], sender, resadmin);
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Show info on a residence.");
	c.get("Info", Arrays.asList("&eUsage: &6/res info <residence>", "Leave off <residence> to display info for the residence your currently in."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

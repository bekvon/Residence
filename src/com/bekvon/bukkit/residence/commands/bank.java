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

public class bank implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3400)
    public boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	
	if (args.length != 2 && args.length != 3) {
	    return false;
	}	
	ClaimedResidence res = null;
	
	if (args.length == 3) {
	    res = plugin.getResidenceManager().getByName(args[1]);
	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return true;
	    }
	} else if ((sender instanceof Player)) {
	    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}
	if (res == null) {
	    plugin.msg(sender, lm.Residence_NotIn);
	    return true;
	}
	double amount = 0D;
	try {
	    if (args.length == 2)
		amount = Double.parseDouble(args[1]);
	    else
		amount = Double.parseDouble(args[2]);
	} catch (Exception ex) {
	    plugin.msg(sender, lm.Invalid_Amount);
	    return true;
	}
	if (args[0].equals("deposit"))
	    res.getBank().deposit(sender, amount, resadmin);
	else if (args[0].equals("withdraw"))
	    res.getBank().withdraw(sender, amount, resadmin);
	else
	    return false;

	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Manage money in a Residence");
	c.get("Info", Arrays.asList("&eUsage: &6/res bank [deposit/withdraw] <residence> [amount]",
	    "You must be standing in a Residence or provide residence name",
	    "You must have the +bank flag."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("deposit%%withdraw", "[residence]"));
    }
}

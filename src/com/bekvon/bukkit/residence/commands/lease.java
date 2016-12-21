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

public class lease implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3900)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 2 || args.length == 3) {
	    if (args[1].equals("expires")) {
		ClaimedResidence res = null;
		if (args.length == 2) {
		    res = plugin.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			plugin.msg(player, lm.Residence_NotIn);
			return true;
		    }
		} else {
		    res = plugin.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			plugin.msg(player, lm.Invalid_Residence);
			return true;
		    }
		}

		String until = plugin.getLeaseManager().getExpireTime(res.getName());
		if (until != null)
		    plugin.msg(player, lm.Economy_LeaseRenew, until);
		return true;
	    }
	    if (args[1].equals("renew")) {
		if (args.length == 3) {
		    plugin.getLeaseManager().renewArea(args[2], player);
		} else {
		    plugin.getLeaseManager().renewArea(plugin.getResidenceManager().getNameByLoc(player.getLocation()), player);
		}
		return true;
	    } else if (args[1].equals("cost")) {
		if (args.length == 3) {
		    ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
		    if (res == null || plugin.getLeaseManager().leaseExpires(args[2])) {
			int cost = plugin.getLeaseManager().getRenewCost(res);
			plugin.msg(player, lm.Economy_LeaseRenewalCost, args[2], cost);
		    } else {
			plugin.msg(player, lm.Economy_LeaseNotExpire);
		    }
		    return true;
		}
		String area = plugin.getResidenceManager().getNameByLoc(player.getLocation());
		ClaimedResidence res = plugin.getResidenceManager().getByName(area);
		if (area == null || res == null) {
		    plugin.msg(player, lm.Invalid_Area);
		    return true;
		}
		if (plugin.getLeaseManager().leaseExpires(area)) {
		    int cost = plugin.getLeaseManager().getRenewCost(res);
		    plugin.msg(player, lm.Economy_LeaseRenewalCost, area, cost);
		} else {
		    plugin.msg(player, lm.Economy_LeaseNotExpire);
		}
		return true;

	    }
	} else if (args.length == 4) {
	    if (args[1].equals("set")) {
		if (!resadmin) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}
		if (args[3].equals("infinite")) {
		    if (plugin.getLeaseManager().leaseExpires(args[2])) {
			plugin.getLeaseManager().removeExpireTime(args[2]);
			plugin.msg(player, lm.Economy_LeaseInfinite);
		    } else {
			plugin.msg(player, lm.Economy_LeaseNotExpire);
		    }
		    return true;
		}
		int days;
		try {
		    days = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.Invalid_Days);
		    return true;
		}
		plugin.getLeaseManager().setExpireTime(player, args[2], days);
		return true;
	    }
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage residence leases");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res lease [renew/cost] [residence]",
	    "/res lease cost will show the cost of renewing a residence lease.", "/res lease renew will renew the residence provided you have enough money."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("renew%%cost", "[residence]"));

	// Sub commands
	path += "SubCommands.";
	c.get(path + "set.Description", "Set the lease time");
	c.get(path + "set.Info", Arrays.asList("&eUsage: &6/resadmin lease set [residence] [#days/infinite]",
	    "Sets the lease time to a specified number of days, or infinite."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "set"), Arrays.asList("[residence]"));

	c.get(path + "renew.Description", "Renew the lease time");
	c.get(path + "renew.Info", Arrays.asList("&eUsage: &6/resadmin lease renew <residence>", "Renews the lease time for current or specified residence."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "renew"), Arrays.asList("[residence]"));

	c.get(path + "expires.Description", "Lease end date");
	c.get(path + "expires.Info", Arrays.asList("&eUsage: &6/resadmin lease expires <residence>", "Shows when expires residence lease time."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "expires"), Arrays.asList("[residence]"));

	c.get(path + "cost.Description", "Shows renew cost");
	c.get(path + "cost.Info", Arrays.asList("&eUsage: &6/resadmin lease cost <residence>", "Shows how much money you need to renew residence lease."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "cost"), Arrays.asList("[residence]"));
    }
}

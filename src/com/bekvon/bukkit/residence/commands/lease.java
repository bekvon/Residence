package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class lease implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 2 || args.length == 3) {
	    if (args[1].equals("expires")) {
		ClaimedResidence res = null;
		if (args.length == 2) {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
			return true;
		    }
		} else {
		    res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
			return true;
		    }
		}

		String until = Residence.getLeaseManager().getExpireTime(res.getName());
		if (until != null)
		    player.sendMessage(Residence.getLM().getMessage("Economy.LeaseRenew", until));
		return true;
	    }
	    if (args[1].equals("renew")) {
		if (args.length == 3) {
		    Residence.getLeaseManager().renewArea(args[2], player);
		} else {
		    Residence.getLeaseManager().renewArea(Residence.getResidenceManager().getNameByLoc(player.getLocation()), player);
		}
		return true;
	    } else if (args[1].equals("cost")) {
		if (args.length == 3) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null || Residence.getLeaseManager().leaseExpires(args[2])) {
			int cost = Residence.getLeaseManager().getRenewCost(res);
			player.sendMessage(Residence.getLM().getMessage("Economy.LeaseRenewalCost", args[2], cost));
		    } else {
			player.sendMessage(Residence.getLM().getMessage("Economy.LeaseNotExpire"));
		    }
		    return true;
		}
		String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		ClaimedResidence res = Residence.getResidenceManager().getByName(area);
		if (area == null || res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Area"));
		    return true;
		}
		if (Residence.getLeaseManager().leaseExpires(area)) {
		    int cost = Residence.getLeaseManager().getRenewCost(res);
		    player.sendMessage(Residence.getLM().getMessage("Economy.LeaseRenewalCost", area, cost));
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Economy.LeaseNotExpire"));
		}
		return true;

	    }
	} else if (args.length == 4) {
	    if (args[1].equals("set")) {
		if (!resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}
		if (args[3].equals("infinite")) {
		    if (Residence.getLeaseManager().leaseExpires(args[2])) {
			Residence.getLeaseManager().removeExpireTime(args[2]);
			player.sendMessage(Residence.getLM().getMessage("Economy.LeaseInfinite"));
		    } else {
			player.sendMessage(Residence.getLM().getMessage("Economy.LeaseNotExpire"));
		    }
		    return true;
		}
		int days;
		try {
		    days = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Days"));
		    return true;
		}
		Residence.getLeaseManager().setExpireTime(player, args[2], days);
		return true;
	    }
	}
	return false;
    }
}

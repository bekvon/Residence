package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class bank implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if ((args.length != 3) && (args.length != 4)) {
	    return false;
	}
	ClaimedResidence res = null;
	if (args.length == 4) {
	    res = Residence.getResidenceManager().getByName(args[2]);
	    if (res == null) {
		sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }
	} else if ((sender instanceof Player)) {
	    res = Residence.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}
	if (res == null) {
	    sender.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
	    return true;
	}
	int amount = 0;
	try {
	    if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	    else
		amount = Integer.parseInt(args[3]);
	} catch (Exception ex) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
	    return true;
	}
	if (args[1].equals("deposit"))
	    res.getBank().deposit(sender, amount, resadmin);
	else if (args[1].equals("withdraw"))
	    res.getBank().withdraw(sender, amount, resadmin);
	else
	    return false;

	return true;
    }
}

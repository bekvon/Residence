package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class signupdate implements cmd {
    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length == 1) {
	    if (!resadmin) {
		sender.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return true;
	    }
	    int number = Residence.getSignUtil().updateAllSigns();
	    sender.sendMessage(Residence.getLM().getMessage("Sign.Updated", number));
	    return true;
	}
	return false;
    }
}

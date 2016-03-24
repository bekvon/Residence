package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class signconvert implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length != 0)
	    return false;

	if (sender instanceof Player) {
	    Player player = (Player) sender;
	    if (Residence.getPermissionManager().isResidenceAdmin(player)) {
		Residence.getSignUtil().convertSigns(sender);
	    } else
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	} else {
	    Residence.getSignUtil().convertSigns(sender);
	}
	return true;
    }
}

package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class server implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (!resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}
	if (args.length == 2) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }
	    res.getPermissions().setOwner(Residence.getServerLandname(), false);
	    player.sendMessage(Residence.getLM().getMessage("Residence.OwnerChange", args[1], Residence.getServerLandname()));
	    return true;
	}
	player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	return true;
    }
}

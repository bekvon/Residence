package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class removeall implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}
	if (resadmin || args[1].endsWith(player.getName())) {
	    Residence.getResidenceManager().removeAllByOwner(player, args[1]);
	    player.sendMessage(Residence.getLM().getMessage("Residence.RemovePlayersResidences", args[1]));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	}
	return true;
    }

}

package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class current implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 1)
	    return false;

	String res = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.In", res));
	}
	return true;
    }

}

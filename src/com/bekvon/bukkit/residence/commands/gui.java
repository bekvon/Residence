package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class gui implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (Residence.getSpoutListener() == null)
	    return true;

	if (args.length == 1) {
	    Residence.getSpout().showResidenceFlagGUI(SpoutManager.getPlayer(player), Residence.getResidenceManager().getNameByLoc(player.getLocation()), resadmin);
	} else if (args.length == 2) {
	    Residence.getSpout().showResidenceFlagGUI(SpoutManager.getPlayer(player), args[1], resadmin);
	}

	return true;
    }
}

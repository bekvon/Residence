package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class resadmin implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;
	if (args.length != 2)
	    return true;

	Player player = (Player) sender;
	if (args[1].equals("on")) {
	    Residence.resadminToggle.add(player.getName());
	    player.sendMessage(Residence.getLM().getMessage("General.AdminToggleTurnOn"));
	} else if (args[1].equals("off")) {
	    Residence.resadminToggle.remove(player.getName());
	    player.sendMessage(Residence.getLM().getMessage("General.AdminToggleTurnOff"));
	}
	return true;
    }

}

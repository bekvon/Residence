package com.bekvon.bukkit.residence.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class material implements cmd {

    @SuppressWarnings("deprecation")
    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}
	try {
	    player.sendMessage(Residence.getLM().getMessage("General.MaterialGet", args[1], Material.getMaterial(Integer.parseInt(args[1])).name()));
	} catch (Exception ex) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Material"));
	}
	return true;
    }
}

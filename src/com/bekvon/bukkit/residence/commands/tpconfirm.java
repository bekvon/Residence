package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class tpconfirm implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1) {
	    return false;
	}
	if (Residence.getTeleportMap().containsKey(player.getName())) {
	    Residence.getTeleportMap().get(player.getName()).tpToResidence(player, player, resadmin);
	    Residence.getTeleportMap().remove(player.getName());
	} else
	    player.sendMessage(Residence.getLM().getMessage("General.NoTeleportConfirm"));
	return true;
    }
}

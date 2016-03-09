package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class renamearea implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 4)
	    return false;

	ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	if (res == null)
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	else
	    res.renameArea(player, args[2], args[3], resadmin);
	return true;
    }
}

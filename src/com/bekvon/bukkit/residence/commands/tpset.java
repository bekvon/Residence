package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class tpset implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	if (res != null) {
	    res.setTpLoc(player, resadmin);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
	return true;
    }
}

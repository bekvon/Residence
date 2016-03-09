package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class clearflags implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (!resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}
	ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	if (area != null) {
	    area.getPermissions().clearFlags();
	    player.sendMessage(Residence.getLM().getMessage("Flag.Cleared"));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
	return true;
    }
}

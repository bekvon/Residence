package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class setmain implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1 && args.length != 2) {
	    return false;
	}

	ClaimedResidence res = null;

	if (args.length == 1)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else
	    res = Residence.getResidenceManager().getByName(args[1]);

	if (res == null) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return false;
	}

	if (res.isOwner(player)) {
	    res.setMainResidence(res.isMainResidence() ? false : true);
	} else if (Residence.getRentManager().isRented(res.getName()) && !Residence.getRentManager().getRentingPlayer(res.getName()).equalsIgnoreCase(player.getName())) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return false;
	}

	player.sendMessage(Residence.getLM().getMessage("Residence.ChangedMain",  res.getTopParentName()));

	ResidencePlayer rplayer = Residence.getPlayerManager().getResidencePlayer(player);
	if (rplayer != null)
	    rplayer.setMainResidence(res);

	return true;
    }
}

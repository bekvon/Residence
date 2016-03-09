package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class subzone implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2 && args.length != 3) {
	    return false;
	}
	String zname;
	String parent;
	if (args.length == 2) {
	    parent = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    zname = args[1];
	} else {
	    parent = args[1];
	    zname = args[2];
	}

	if (Residence.getWEplugin() != null) {
	    if (Residence.getWEplugin().getConfig().getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
		Residence.getSelectionManager().worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(parent);
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    if (!player.hasPermission("residence.create.subzone") && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Subzone.CantCreate"));
		return false;
	    }

	    res.addSubzone(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(player.getName()),
		zname, resadmin);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Select.Points"));
	}
	return true;
    }

}

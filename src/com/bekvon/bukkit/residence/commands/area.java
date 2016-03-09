package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class area implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if (args.length == 4) {
	    if (args[1].equals("remove")) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		if (res != null) {
		    res.removeArea(player, args[3], resadmin);
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		}
		return true;
	    } else if (args[1].equals("add")) {
		if (Residence.getWEplugin() != null) {
		    if (Residence.getWEplugin().getConfig().getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
			Residence.getSelectionManager().worldEdit(player);
		    }
		}
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res != null) {
			res.addArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(
			    player.getName())), args[3], resadmin);
		    } else {
			player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		    }
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Select.Points"));
		}
		return true;
	    } else if (args[1].equals("replace")) {
		if (Residence.getWEplugin() != null) {
		    if (Residence.getWEplugin().getConfig().getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
			Residence.getSelectionManager().worldEdit(player);
		    }
		}
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res != null) {
			res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager()
			    .getPlayerLoc2(player.getName())), args[3], resadmin);
		    } else {
			player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		    }
		} else {
		    player.sendMessage(Residence.getLM().getMessage("Select.Points"));
		}
		return true;
	    }
	}
	if ((args.length == 3 || args.length == 4) && args[1].equals("list")) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
	    if (res != null) {
		res.printAreaList(player, page);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if ((args.length == 3 || args.length == 4) && args[1].equals("listall")) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
	    if (res != null) {
		res.printAdvancedAreaList(player, page);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	}
	return false;
    }
}

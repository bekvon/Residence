package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.gui.SetFlag;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class pset implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(player, args[1], resadmin);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if (args.length == 4 && args[3].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(player, args[2], resadmin);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());

	    if (!Residence.isPlayerExist(player, args[1], true))
		return false;

	    if (area != null) {
		area.getPermissions().setPlayerFlag(player, args[1], args[2], args[3], resadmin, true);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if (args.length == 5) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (!Residence.isPlayerExist(player, args[2], true))
		return false;
	    if (area != null) {
		area.getPermissions().setPlayerFlag(player, args[2], args[3], args[4], resadmin, true);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if (args.length == 2) {
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		if (!Residence.isPlayerExist(player, args[1], true))
		    return false;
		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}
		SetFlag flag = new SetFlag(res.getName(), player, resadmin);
		flag.setTargePlayer(args[1]);
		flag.recalculatePlayer(res);
		ResidencePlayerListener.GUI.put(player.getName(), flag);
		player.openInventory(flag.getInventory());
	    } else
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	} else if (args.length == 3) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	    if (res != null) {
		if (!Residence.isPlayerExist(player, args[2], true))
		    return false;
		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		SetFlag flag = new SetFlag(res.getName(), player, resadmin);
		flag.setTargePlayer(args[2]);
		flag.recalculatePlayer(res);
		ResidencePlayerListener.GUI.put(player.getName(), flag);
		player.openInventory(flag.getInventory());
	    } else
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}
	return false;
    }
}

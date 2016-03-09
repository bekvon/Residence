package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class lists implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 2) {
	    if (args[1].equals("list")) {
		Residence.getPermissionListManager().printLists(player);
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("view")) {
		Residence.getPermissionListManager().printList(player, args[2]);
		return true;
	    } else if (args[1].equals("remove")) {
		Residence.getPermissionListManager().removeList(player, args[2]);
		return true;
	    } else if (args[1].equals("add")) {
		Residence.getPermissionListManager().makeList(player, args[2]);
		return true;
	    }
	} else if (args.length == 4) {
	    if (args[1].equals("apply")) {
		Residence.getPermissionListManager().applyListToResidence(player, args[2], args[3], resadmin);
		return true;
	    }
	} else if (args.length == 5) {
	    if (args[1].equals("set")) {
		Residence.getPermissionListManager().getList(player.getName(), args[2]).setFlag(args[3], FlagPermissions.stringToFlagState(args[4]));
		player.sendMessage(Residence.getLM().getMessage("Flag.Set"));
		return true;
	    }
	} else if (args.length == 6) {
	    if (args[1].equals("gset")) {
		Residence.getPermissionListManager().getList(player.getName(), args[2]).setGroupFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		player.sendMessage(Residence.getLM().getMessage("Flag.Set"));
		return true;
	    } else if (args[1].equals("pset")) {
		Residence.getPermissionListManager().getList(player.getName(), args[2]).setPlayerFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		player.sendMessage(Residence.getLM().getMessage("Flag.Set"));
		return true;
	    }
	}
	return false;
    }

}

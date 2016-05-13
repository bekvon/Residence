package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.gui.SetFlag;
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
	} else if ((args.length == 2 || args.length == 3) && Residence.getConfigManager().useFlagGUI()) {
	    ClaimedResidence res = null;
	    String targetPlayer = null;
	    if (args.length == 2) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		targetPlayer = args[1];
	    } else {
		res = Residence.getResidenceManager().getByName(args[1]);
		targetPlayer = args[2];
	    }

	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    if (!Residence.isPlayerExist(player, targetPlayer, true))
		return false;
	    if (!res.isOwner(player) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return true;
	    }
	    SetFlag flag = new SetFlag(res.getName(), player, resadmin);
	    flag.setTargetPlayer(targetPlayer);
	    flag.recalculatePlayer(res);
	    player.closeInventory();
	    Residence.getPlayerListener().getGUImap().put(player.getName(), flag);
	    player.openInventory(flag.getInventory());

	    return true;
	}
	return false;
    }
}

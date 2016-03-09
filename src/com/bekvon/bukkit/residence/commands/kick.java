package com.bekvon.bukkit.residence.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class kick implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 2)
	    return false;

	Player targetplayer = Bukkit.getPlayer(args[1]);
	if (targetplayer == null) {
	    player.sendMessage(Residence.getLM().getMessage("General.NotOnline"));
	    return true;
	}
	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!group.hasKickAccess() && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(targetplayer.getLocation());

	if (res == null || res != null && !res.isOwner(player) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.PlayerNotIn"));
	    return true;
	}

	if (!res.isOwner(player))
	    return false;

	if (res.getPlayersInResidence().contains(targetplayer)) {
	    Location loc = Residence.getConfigManager().getKickLocation();
	    if (loc != null)
		targetplayer.teleport(loc);
	    else
		targetplayer.teleport(res.getOutsideFreeLoc(player.getLocation(), player));
	    targetplayer.sendMessage(Residence.getLM().getMessage("Residence.Kicked"));
	}
	return true;
    }
}

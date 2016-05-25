package com.bekvon.bukkit.residence.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class limits implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player) && args.length < 2)
	    return false;

	if (args.length != 1 && args.length != 2)
	    return false;
	final String[] tempArgs = args;
	OfflinePlayer target;
	boolean rsadm = false;
	if (tempArgs.length == 1) {
	    target = (Player) sender;
	    rsadm = true;
	} else
	    target = Residence.getOfflinePlayer(tempArgs[1]);
	if (target == null)
	    return false;
	Residence.getPermissionManager().updateGroupNameForPlayer(target.getName(), target.isOnline() ? target.getPlayer().getLocation().getWorld().getName() : Residence
	    .getConfigManager().getDefaultWorld(), true);

	Residence.getPermissionManager().getGroup(target.getName(), Residence.getConfigManager().getDefaultWorld()).printLimits(sender, target, rsadm);
	return true;
    }
}

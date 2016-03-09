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
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 1 && args.length != 2)
	    return false;
	final String[] tempArgs = args;
	final Player p = player;
	OfflinePlayer target;
	boolean rsadm = false;
	if (tempArgs.length == 1) {
	    target = p;
	    rsadm = true;
	} else
	    target = Residence.getOfflinePlayer(tempArgs[1]);
	if (target == null)
	    return false;
	Residence.getPermissionManager().getGroup(target.getName(), Residence.getConfigManager().getDefaultWorld()).printLimits(p, target, rsadm);
	return true;
    }
}

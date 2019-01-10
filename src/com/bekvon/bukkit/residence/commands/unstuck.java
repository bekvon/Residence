package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class unstuck implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4000)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 1)
	    return false;

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (!group.hasUnstuckAccess()) {
	    plugin.msg(player, lm.General_NoPermission);
	    return true;
	}
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	if (res == null) {
	    plugin.msg(player, lm.Residence_NotIn);
	} else {
	    plugin.msg(player, lm.General_Moved);
	    player.teleport(res.getOutsideFreeLoc(player.getLocation(), player));
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Teleports outside of residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res unstuck"));
    }
}

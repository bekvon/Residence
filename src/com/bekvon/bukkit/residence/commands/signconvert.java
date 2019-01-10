package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class signconvert implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5600)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length != 0)
	    return false;

	if (sender instanceof Player) {
	    Player player = (Player) sender;
	    if (plugin.getPermissionManager().isResidenceAdmin(player)) {
		plugin.getSignUtil().convertSigns(sender);
	    } else
		plugin.msg(player, lm.General_NoPermission);
	} else {
	    plugin.getSignUtil().convertSigns(sender);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Converts signs from ResidenceSign plugin");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res signconvert", "Will try to convert saved sign data from 3rd party plugin"));
    }
}

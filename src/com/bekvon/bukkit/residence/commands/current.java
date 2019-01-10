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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class current implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 1)
	    return false;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	if (res == null) {
	    plugin.msg(player, lm.Residence_NotIn);
	} else {
	    plugin.msg(player, lm.Residence_In, res.getName());
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Show residence your currently in.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res current"));
    }

}

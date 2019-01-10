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

public class confirm implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2400)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	Player player = null;
	String name = "Console";
	if (sender instanceof Player) {
	    player = (Player) sender;
	    name = player.getName();
	}
	if (args.length != 1)
	    return true;

	String area = plugin.deleteConfirm.get(name);
	if (area == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	} else {
	    plugin.getResidenceManager().removeResidence(player, area, resadmin);
	    plugin.deleteConfirm.remove(name);
	}

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Confirms removal of a residence.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res confirm", "Confirms removal of a residence."));
    }

}

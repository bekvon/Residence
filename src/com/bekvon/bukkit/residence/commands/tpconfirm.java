package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class tpconfirm implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1500)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1) {
	    return false;
	}
	if (Residence.getTeleportMap().containsKey(player.getName())) {
	    Residence.getTeleportMap().get(player.getName()).tpToResidence(player, player, resadmin);
	    Residence.getTeleportMap().remove(player.getName());
	} else
	    Residence.msg(player, lm.General_NoTeleportConfirm);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Ignore unsafe teleportation warning");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res tpconfirm", "Teleports you to a residence, when teleportation is unsafe."));
    }
}

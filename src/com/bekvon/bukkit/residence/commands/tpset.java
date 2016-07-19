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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class tpset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 200)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	if (res != null) {
	    res.setTpLoc(player, resadmin);
	} else {
	    Residence.msg(player, lm.Invalid_Residence);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Set the teleport location of a Residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res tpset", "This will set the teleport location for a residence to where your standing.",
	    "You must be standing in the residence to use this command.", "You must also be the owner or have the +admin flag for the residence."));
    }
}

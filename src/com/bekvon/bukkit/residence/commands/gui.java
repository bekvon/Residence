package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class gui implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4600)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (plugin.getSpoutListener() == null)
	    return true;

	if (args.length == 1) {
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null)
	    plugin.getSpout().showResidenceFlagGUI(SpoutManager.getPlayer(player), res.getName(), resadmin);
	} else if (args.length == 2) {
	    plugin.getSpout().showResidenceFlagGUI(SpoutManager.getPlayer(player), args[1], resadmin);
	}

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Opens gui (Spout only)");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res gui <residence>"));
    }
}

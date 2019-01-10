package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class show implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3300)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	ClaimedResidence res = null;

	if (args.length == 2) {
	    res = plugin.getResidenceManager().getByName(args[1]);
	} else {
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	}

	if (res == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return true;
	}

	Visualizer v = new Visualizer(player);
	v.setAreas(res.getAreaArray());
	plugin.getSelectionManager().showBounds(player, v);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Show residence boundaries");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res show <residence>"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

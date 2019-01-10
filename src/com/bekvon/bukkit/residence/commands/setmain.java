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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class setmain implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2900)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1 && args.length != 2) {
	    return false;
	}

	ClaimedResidence res = null;

	if (args.length == 1)
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	else
	    res = plugin.getResidenceManager().getByName(args[1]);

	if (res == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return false;
	}

	if (res.isOwner(player)) {
	    res.setMainResidence(res.isMainResidence() ? false : true);
	} else if (plugin.getRentManager().isRented(res.getName()) && !plugin.getRentManager().getRentingPlayer(res.getName()).equalsIgnoreCase(player.getName())) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return false;
	}

	plugin.msg(player, lm.Residence_ChangedMain, res.getTopParentName());

	ResidencePlayer rplayer = plugin.getPlayerManager().getResidencePlayer(player);
	if (rplayer != null)
	    rplayer.setMainResidence(res);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Sets defined residence as main to show up in chat as prefix");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res setmain (residence)", "Set defined residence as main."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

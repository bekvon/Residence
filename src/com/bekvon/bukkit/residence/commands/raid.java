package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Zrips.CMI.utils.RawMessage;
import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.RaidDefender;

public class raid implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	final Player player = (Player) sender;

	if (args.length != 1 && args.length != 2)
	    return false;

	if (!ConfigManager.RaidEnabled) {
	    plugin.msg(player, lm.Raid_NotEnabled);
	    return true;
	}
	
	if (!resadmin) {
	    plugin.msg(sender, lm.General_NoPermission);
	}
	
	// raid start [resname/playerName/currentres]
	// raid stop [resname/playerName/currentres]
	// raid immunity [add/take/set/clear] [resname/playerName/currentres]


	return false;
    }


    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage raid in residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res defend [resName] (playerName)"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]%%[playername]"));
    }

}

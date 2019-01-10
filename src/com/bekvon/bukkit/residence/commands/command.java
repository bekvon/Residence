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

public class command implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3000)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	ClaimedResidence res = null;
	String action = null;
	String cmd = null;
	if (args.length == 2 && sender instanceof Player) {
	    Player player = (Player) sender;
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    action = args[1];
	} else if (args.length == 3 && sender instanceof Player) {
	    Player player = (Player) sender;
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    action = args[1];
	    cmd = args[2];
	} else if (args.length == 4) {
	    res = plugin.getResidenceManager().getByName(args[1]);
	    action = args[2];
	    cmd = args[3];
	} else if (args.length == 3 && !(sender instanceof Player)) {
	    res = plugin.getResidenceManager().getByName(args[1]);
	    action = args[2];
	}

	if (res == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return true;
	}

	if (!res.isOwner(sender) && !resadmin) {
	    plugin.msg(sender, lm.Residence_NotOwner);
	    return true;
	}

	if (action != null && action.equalsIgnoreCase("allow")) {
	    if (res.addCmdWhiteList(cmd)) {
		plugin.msg(sender, lm.command_addedAllow, res.getName());
	    } else
		plugin.msg(sender, lm.command_removedAllow, res.getName());
	} else if (action != null && action.equalsIgnoreCase("block")) {
	    if (res.addCmdBlackList(cmd)) {
		plugin.msg(sender, lm.command_addedBlock, res.getName());
	    } else
		plugin.msg(sender, lm.command_removedBlock, res.getName());
	} else if (action != null && action.equalsIgnoreCase("list")) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < res.getCmdBlackList().size(); i++) {
		sb.append("/" + res.getCmdBlackList().get(i).replace("_", " "));
		if (i + 1 < res.getCmdBlackList().size())
		    sb.append(", ");
	    }
	    plugin.msg(sender, lm.command_Blocked, sb.toString());

	    sb = new StringBuilder();
	    for (int i = 0; i < res.getCmdWhiteList().size(); i++) {
		sb.append("/" + res.getCmdWhiteList().get(i).replace("_", " "));
		if (i + 1 < res.getCmdWhiteList().size())
		    sb.append(", ");
	    }
	    plugin.msg(sender, lm.command_Allowed, sb.toString());
	} else
	    return false;

	return true;

    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manages allowed or blocked commands in residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res command <residence> <allow/block/list> <command>",
	    "Shows list, adds or removes allowed or disabled commands in residence",
	    "Use _ to include command with multiple variables"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%allow%%block%%list",
	    "allow%%block%%list"));
    }
}

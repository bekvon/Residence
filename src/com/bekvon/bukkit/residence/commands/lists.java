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
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class lists implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4900)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 2) {
	    if (args[1].equals("list")) {
		Residence.getPermissionListManager().printLists(player);
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("view")) {
		Residence.getPermissionListManager().printList(player, args[2]);
		return true;
	    } else if (args[1].equals("remove")) {
		Residence.getPermissionListManager().removeList(player, args[2]);
		return true;
	    } else if (args[1].equals("add")) {
		Residence.getPermissionListManager().makeList(player, args[2]);
		return true;
	    }
	} else if (args.length == 4) {
	    if (args[1].equals("apply")) {
		Residence.getPermissionListManager().applyListToResidence(player, args[2], args[3], resadmin);
		return true;
	    }
	} else if (args.length == 5) {
	    if (args[1].equals("set")) {
		Residence.getPermissionListManager().getList(player.getName(), args[2]).setFlag(args[3], FlagPermissions.stringToFlagState(args[4]));
		Residence.msg(player, lm.Flag_Set);
		return true;
	    }
	} else if (args.length == 6) {
	    if (args[1].equals("gset")) {
		Residence.getPermissionListManager().getList(player.getName(), args[2]).setGroupFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		Residence.msg(player, lm.Flag_Set);
		return true;
	    } else if (args[1].equals("pset")) {
		Residence.getPermissionListManager().getList(player.getName(), args[2]).setPlayerFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		Residence.msg(player, lm.Flag_Set);
		return true;
	    }
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {

	c.get(path + "Description", "Predefined permission lists");
	c.get(path + "Info", Arrays.asList("Predefined permissions that can be applied to a residence."));

	path += "SubCommands.";
	c.get(path + "add.Description", "Add a list");
	c.get(path + "add.Info", Arrays.asList("&eUsage: &6/res lists add <listname>"));

	c.get(path + "remove.Description", "Remove a list");
	c.get(path + "remove.Info", Arrays.asList("&eUsage: &6/res lists remove <listname>"));

	c.get(path + "apply.Description", "Apply a list to a residence");
	c.get(path + "apply.Info", Arrays.asList("&eUsage: &6/res lists apply <listname> <residence>"));

	c.get(path + "set.Description", "Set a flag");
	c.get(path + "set.Info", Arrays.asList("&eUsage: &6/res lists set <listname> <flag> <value>"));

	c.get(path + "pset.Description", "Set a player flag");
	c.get(path + "pset.Info", Arrays.asList("&eUsage: &6/res lists pset <listname> <player> <flag> <value>"));

	c.get(path + "gset.Description", "Set a group flag");
	c.get(path + "gset.Info", Arrays.asList("&eUsage: &6/res lists gset <listname> <group> <flag> <value>"));

	c.get(path + "view.Description", "View a list.");
	c.get(path + "view.Info", Arrays.asList("&eUsage: &6/res lists view <listname>"));
    }

}

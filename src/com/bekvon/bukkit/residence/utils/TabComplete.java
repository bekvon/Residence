package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.bekvon.bukkit.residence.text.help.HelpEntry;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
	List<String> completionList = new ArrayList<>();
	Set<String> Commands = HelpEntry.getSubCommands(args);

	if (Commands.contains("?")) {
	    String com = "";
	    for (String one : args) {
		com += " " + one;
	    }
	    Bukkit.dispatchCommand(sender, "res" + com + "?");
	    Bukkit.getConsoleSender().sendMessage(sender.getName() + " issued server command: /res" + com + "?");
	    Commands.clear();
	    Commands.add("");
	}

	String PartOfCommand = args[args.length - 1];
	StringUtil.copyPartialMatches(PartOfCommand, Commands, completionList);
	Collections.sort(completionList);
	return completionList;
    }
}

package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

public class removeworld implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5200)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length != 2)
	    return false;
	if (sender instanceof Player || sender instanceof BlockCommandSender) {
	    sender.sendMessage(ChatColor.RED + "MUST be run from console.");
	    return false;
	}
	plugin.getResidenceManager().removeAllFromWorld(sender, args[1]);

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Removes all residences from particular world");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res removeworld [worldName]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[worldname]"));
    }
}

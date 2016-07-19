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

public class resadmin implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5300)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;
	if (args.length != 2)
	    return true;

	Player player = (Player) sender;
	if (args[1].equals("on")) {
	    Residence.resadminToggle.add(player.getName());
	    Residence.msg(player, lm.General_AdminToggleTurnOn);
	} else if (args[1].equals("off")) {
	    Residence.resadminToggle.remove(player.getName());
	    Residence.msg(player, lm.General_AdminToggleTurnOff);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Enabled or disable residence admin");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res resadmin [on/off]"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("on%%off"));
    }
}

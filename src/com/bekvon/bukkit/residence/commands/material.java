package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class material implements cmd {

    @SuppressWarnings("deprecation")
    @Override
    @CommandAnnotation(simple = true, priority = 4300)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}
	try {
	    Residence.msg(player, lm.General_MaterialGet, args[1], Material.getMaterial(Integer.parseInt(args[1])).name());
	} catch (Exception ex) {
	    Residence.msg(player, lm.Invalid_Material);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Check if material exists by its id");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res material [material]"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[materialId]"));
    }
}

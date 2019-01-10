package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.ItemManager.CMIMaterial;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class material implements cmd {

    @SuppressWarnings("deprecation")
    @Override
    @CommandAnnotation(simple = true, priority = 4300)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}
	try {
	    plugin.msg(player, lm.General_MaterialGet, args[1], CMIMaterial.get(Integer.parseInt(args[1])).getName());
	} catch (Exception ex) {
	    plugin.msg(player, lm.Invalid_Material);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Check if material exists by its id");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res material [material]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[materialId]"));
    }
}

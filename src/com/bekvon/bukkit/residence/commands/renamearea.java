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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class renamearea implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2800)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 4)
	    return false;

	ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	if (res == null)
	    Residence.msg(player, lm.Invalid_Residence);
	else
	    res.renameArea(player, args[2], args[3], resadmin);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Rename area name for residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res removeworld [residence] [oldAreaName] [newAreaName]"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class check implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3500)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;
	Player player = (Player) sender;
	String pname = player.getName();

	if (args.length != 3 && args.length != 4)
	    return false;

	if (args.length == 4)
	    pname = args[3];

	ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return true;
	}

	Flags flag = Flags.getFlag(args[2]);

	if (flag == null) {
	    plugin.msg(player, lm.Invalid_Flag);
	    return true;
	}

	if (!res.getPermissions().hasApplicableFlag(pname, args[2])) {
	    plugin.msg(player, lm.Flag_CheckFalse, flag, pname, args[1]);
	} else {
	    plugin.msg(player, lm.Flag_CheckTrue, flag, pname, args[1], (res.getPermissions().playerHas(player, res.getPermissions().getWorld(), flag, false) ? plugin.msg(lm.General_True)
		: plugin.msg(lm.General_False)));
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Check flag state for you");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res check [residence] [flag] (playername)"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]", "[flag]", "[playername]"));
    }
}

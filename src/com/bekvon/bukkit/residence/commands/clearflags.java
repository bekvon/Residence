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

public class clearflags implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 3600)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (!resadmin) {
	    plugin.msg(player, lm.General_NoPermission);
	    return true;
	}
	ClaimedResidence area = plugin.getResidenceManager().getByName(args[1]);
	if (area != null) {
	    area.getPermissions().clearFlags();
	    plugin.msg(player, lm.Flag_Cleared);
	} else {
	    plugin.msg(player, lm.Invalid_Residence);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Remove all flags from residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res clearflags <residence>"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}

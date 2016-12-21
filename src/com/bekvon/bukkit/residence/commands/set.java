package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.gui.SetFlag;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class set implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player) && args.length != 4)
	    return false;

	if (args.length == 3) {
	    Player player = (Player) sender;
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return true;
	    }
	    if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
		plugin.msg(sender, lm.General_NoPermission);
		return true;
	    }
	    res.getPermissions().setFlag(sender, args[1], args[2], resadmin);
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return true;
	    }
	    if (!res.isOwner(sender) && !resadmin && !res.getPermissions().playerHas(sender, Flags.admin, false)) {
		plugin.msg(sender, lm.General_NoPermission);
		return true;
	    }
	    res.getPermissions().setFlag(sender, args[2], args[3], resadmin);
	    return true;
	} else if ((args.length == 1 || args.length == 2) && plugin.getConfigManager().useFlagGUI()) {
	    Player player = (Player) sender;
	    ClaimedResidence res = null;
	    if (args.length == 1)
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    else
		res = plugin.getResidenceManager().getByName(args[1]);
	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return true;
	    }
	    if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
		plugin.msg(sender, lm.General_NoPermission);
		return true;
	    }
	    SetFlag flag = new SetFlag(res.getName(), player, resadmin);
	    flag.recalculateResidence(res);
	    player.closeInventory();
	    plugin.getPlayerListener().getGUImap().put(player.getName(), flag);
	    player.openInventory(flag.getInventory());
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Set general flags on a Residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res set <residence> [flag] [true/false/remove]",
	    "To see a list of flags, use /res flags ?", "These flags apply to any players who do not have the flag applied specifically to them. (see /res pset ?)"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[flag]", "[flag]%%true%%false%%remove",
	    "true%%false%%remove"));
    }
}

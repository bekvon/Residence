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
import com.bekvon.bukkit.residence.gui.SetFlag;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class set implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player) && args.length != 4)
	    return false;

	if (args.length == 3) {
	    Player player = (Player) sender;
	    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    if (area != null) {
		Residence.getResidenceManager().getByName(area).getPermissions().setFlag(sender, args[1], args[2], resadmin);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setFlag(sender, args[2], args[3], resadmin);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if ((args.length == 1 || args.length == 2) && Residence.getConfigManager().useFlagGUI()) {
	    Player player = (Player) sender;
	    ClaimedResidence res = null;
	    if (args.length == 1)
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    else
		res = Residence.getResidenceManager().getByName(args[1]);
	    if (res == null) {
		Residence.msg(sender, lm.Invalid_Residence);
		return true;
	    }
	    if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, "admin", false)) {
		Residence.msg(sender, lm.General_NoPermission);
		return true;
	    }
	    SetFlag flag = new SetFlag(res.getName(), player, resadmin);
	    flag.recalculateResidence(res);
	    player.closeInventory();
	    Residence.getPlayerListener().getGUImap().put(player.getName(), flag);
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
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[flag]", "[flag]%%true%%false%%remove", "true%%false%%remove"));
    }
}

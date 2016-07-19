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

public class pset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 800)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player) && args.length != 5 && args.length == 4 && !args[3].equalsIgnoreCase("removeall"))
	    return false;

	if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
	    Player player = (Player) sender;
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(sender, args[1], resadmin);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if (args.length == 4 && args[3].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(sender, args[2], resadmin);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if (args.length == 4) {
	    Player player = (Player) sender;
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());

	    if (!Residence.isPlayerExist(sender, args[1], true))
		return false;

	    if (area != null) {
		area.getPermissions().setPlayerFlag(sender, args[1], args[2], args[3], resadmin, true);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if (args.length == 5) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (!Residence.isPlayerExist(sender, args[2], true))
		return false;
	    if (area != null) {
		area.getPermissions().setPlayerFlag(sender, args[2], args[3], args[4], resadmin, true);
	    } else {
		Residence.msg(sender, lm.Invalid_Residence);
	    }
	    return true;
	} else if ((args.length == 2 || args.length == 3) && Residence.getConfigManager().useFlagGUI()) {
	    Player player = (Player) sender;
	    ClaimedResidence res = null;
	    String targetPlayer = null;
	    if (args.length == 2) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		targetPlayer = args[1];
	    } else {
		res = Residence.getResidenceManager().getByName(args[1]);
		targetPlayer = args[2];
	    }

	    if (res == null) {
		Residence.msg(sender, lm.Invalid_Residence);
		return true;
	    }

	    if (!Residence.isPlayerExist(player, targetPlayer, true))
		return false;
	    if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, "admin", false)) {
		Residence.msg(sender, lm.General_NoPermission);
		return true;
	    }
	    SetFlag flag = new SetFlag(res.getName(), player, resadmin);
	    flag.setTargetPlayer(targetPlayer);
	    flag.recalculatePlayer(res);
	    player.closeInventory();
	    Residence.getPlayerListener().getGUImap().put(player.getName(), flag);
	    player.openInventory(flag.getInventory());

	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Set flags on a specific player for a Residence.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res pset <residence> [player] [flag] [true/false/remove]",
	    "&eUsage: &6/res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[playername]", "[playername]%%[flag]", "[flag]%%true%%false%%remove",
	    "true%%false%%remove"));
    }
}

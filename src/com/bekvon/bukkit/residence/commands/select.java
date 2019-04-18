package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class select implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1300)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

	PermissionGroup group = rPlayer.getGroup();
	if (!group.selectCommandAccess() && !resadmin) {
	    plugin.msg(player, lm.Select_Disabled);
	    return true;
	}
	if (!group.canCreateResidences() && rPlayer.getMaxSubzones() <= 0 && !resadmin) {
	    plugin.msg(player, lm.Select_Disabled);
	    return true;
	}
	if ((!player.hasPermission("residence.create") && player.isPermissionSet("residence.create") && !player.hasPermission("residence.select") && player
	    .isPermissionSet("residence.select")) && !resadmin) {
	    plugin.msg(player, lm.Select_Disabled);
	    return true;
	}

	if (args.length == 2) {
	    if (args[1].equals("size") || args[1].equals("cost")) {
		if (plugin.getSelectionManager().hasPlacedBoth(player)) {
		    try {
			plugin.getSelectionManager().showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		} else if (plugin.getSelectionManager().worldEdit(player)) {
		    try {
			plugin.getSelectionManager().showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		}
	    } else if (args[1].equals("vert")) {
		plugin.getSelectionManager().vert(player, resadmin);
		plugin.getSelectionManager().afterSelectionUpdate(player, true);
		return true;
	    } else if (args[1].equals("sky")) {
		plugin.getSelectionManager().sky(player, resadmin);
		plugin.getSelectionManager().afterSelectionUpdate(player, true);
		return true;
	    } else if (args[1].equals("bedrock")) {
		plugin.getSelectionManager().bedrock(player, resadmin);
		plugin.getSelectionManager().afterSelectionUpdate(player, true);
		return true;
	    } else if (args[1].equals("coords")) {
		plugin.msg(player, lm.General_Separator);

		if (!plugin.getSelectionManager().hasPlacedBoth(player)) {
		    plugin.msg(player, lm.Select_Points);
		    return true;
		}

		Location playerLoc1 = plugin.getSelectionManager().getPlayerLoc1(player);
		if (playerLoc1 != null) {
		    plugin.msg(player, lm.Select_Primary, plugin.msg(lm.General_CoordsTop, playerLoc1.getBlockX(), playerLoc1
			.getBlockY(), playerLoc1.getBlockZ()));
		}
		Location playerLoc2 = plugin.getSelectionManager().getPlayerLoc2(player);
		if (playerLoc2 != null) {
		    plugin.msg(player, lm.Select_Secondary, plugin.msg(lm.General_CoordsBottom, playerLoc2.getBlockX(),
			playerLoc2.getBlockY(), playerLoc2.getBlockZ()));
		}
		plugin.msg(player, lm.General_Separator);
		plugin.getSelectionManager().afterSelectionUpdate(player, false);
		return true;
	    } else if (args[1].equals("chunk")) {
		plugin.getSelectionManager().selectChunk(player);
		plugin.getSelectionManager().afterSelectionUpdate(player, true);
		return true;
	    } else if (args[1].equals("worldedit")) {
		if (plugin.getSelectionManager().worldEdit(player)) {
		    plugin.msg(player, lm.Select_Success);
		    plugin.getSelectionManager().afterSelectionUpdate(player, false);
		}
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("expand")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.Invalid_Amount);
		    return true;
		}
		plugin.getSelectionManager().modify(player, false, amount);
		return true;
	    }
	    if (args[1].equals("contract")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.Invalid_Amount);
		    return true;
		}
		plugin.getSelectionManager().contract(player, amount);
		return true;
	    } else if (args[1].equals("shift")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.Invalid_Amount);
		    return true;
		}
		if (amount > 100)
		    amount = 100;
		if (amount < -100)
		    amount = -100;
		plugin.getSelectionManager().modify(player, true, amount);
		return true;
	    }
	}
	if ((args.length == 2 || args.length == 3) && args[1].equals("auto")) {
	    Player target = player;
	    if (args.length == 3) {
		if (!plugin.hasPermission(player, "residence.select.auto.others")) {
		    return true;
		}
		target = Bukkit.getPlayer(args[2]);
		if (target == null) {
		    plugin.msg(player, lm.General_NotOnline);
		    return true;
		}
	    }
	    plugin.getSelectionManager().clearSelection(target);
	    plugin.getAutoSelectionManager().switchAutoSelection(target);
	    return true;
	}
	if (args.length > 1 && args[1].equals("residence")) {
	    String resName;
	    String areaName;
	    ClaimedResidence res = null;
	    if (args.length > 2) {
		res = plugin.getResidenceManager().getByName(args[2]);
	    } else {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    }
	    if (res == null) {
		plugin.msg(player, lm.Invalid_Residence);
		return true;
	    }
	    resName = res.getName();
	    CuboidArea area = null;
	    if (args.length > 3) {
		area = res.getArea(args[3]);
		areaName = args[3];
	    } else {
		areaName = res.getAreaIDbyLoc(player.getLocation());
		area = res.getArea(areaName);
	    }
	    if (area != null) {
		plugin.getSelectionManager().placeLoc1(player, area.getHighLoc(), false);
		plugin.getSelectionManager().placeLoc2(player, area.getLowLoc(), true);
		plugin.msg(player, lm.Select_Area, areaName, resName);
	    } else {
		plugin.msg(player, lm.Area_NonExist);
	    }
	    return true;
	}
	try {
	    plugin.getSelectionManager().selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
	    return true;
	} catch (Exception ex) {
//	    plugin.msg(player, lm.Select_Fail);
	    return false;
	}
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Selection Commands");
	c.get(path + "Info", Arrays.asList("This command selects areas for usage with residence.",
	    "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."));

	// Sub commands
	path += "SubCommands.";

	c.get(path + "coords.Description", "Display selected coordinates");
	c.get(path + "coords.Info", Arrays.asList("&eUsage: &6/res select coords"));

	c.get(path + "size.Description", "Display selected size");
	c.get(path + "size.Info", Arrays.asList("&eUsage: &6/res select size"));

	c.get(path + "auto.Description", "Turns on auto selection tool");
	c.get(path + "auto.Info", Arrays.asList("&eUsage: &6/res select auto [playername]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "auto"), Arrays.asList("[playername]"));

	c.get(path + "cost.Description", "Display selection cost");
	c.get(path + "cost.Info", Arrays.asList("&eUsage: &6/res select cost"));

	c.get(path + "vert.Description", "Expand Selection Vertically");
	c.get(path + "vert.Info", Arrays.asList("&eUsage: &6/res select vert", "Will expand selection as high and as low as allowed."));

	c.get(path + "sky.Description", "Expand Selection to Sky");
	c.get(path + "sky.Info", Arrays.asList("&eUsage: &6/res select sky", "Expands as high as your allowed to go."));

	c.get(path + "bedrock.Description", "Expand Selection to Bedrock");
	c.get(path + "bedrock.Info", Arrays.asList("&eUsage: &6/res select bedrock", "Expands as low as your allowed to go."));

	c.get(path + "expand.Description", "Expand selection in a direction.");
	c.get(path + "expand.Info", Arrays.asList("&eUsage: &6/res select expand <amount>", "Expands <amount> in the direction your looking."));

	c.get(path + "shift.Description", "Shift selection in a direction");
	c.get(path + "shift.Info", Arrays.asList("&eUsage: &6/res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."));

	c.get(path + "chunk.Description", "Select the chunk your currently in.");
	c.get(path + "chunk.Info", Arrays.asList("&eUsage: &6/res select chunk", "Selects the chunk your currently standing in."));

	c.get(path + "residence.Description", "Select a existing area in a residence.");
	c.get(path + "residence.Info", Arrays.asList("&eUsage: &6/res select residence <residence>", "Selects a existing area in a residence."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "residence"), Arrays.asList("[residence]"));

	c.get(path + "worldedit.Description", "Set selection using the current WorldEdit selection.");
	c.get(path + "worldedit.Info", Arrays.asList("&eUsage: &6/res select worldedit", "Sets selection area using the current WorldEdit selection."));
    }
}

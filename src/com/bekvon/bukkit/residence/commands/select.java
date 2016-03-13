package com.bekvon.bukkit.residence.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selection.AutoSelection;

public class select implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!group.selectCommandAccess() && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Select.Disabled"));
	    return true;
	}
	if (!group.canCreateResidences() && group.getMaxSubzoneDepth(player.getName()) <= 0 && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Select.Disabled"));
	    return true;
	}
	if ((!player.hasPermission("residence.create") && player.isPermissionSet("residence.create") && !player.hasPermission("residence.select") && player
	    .isPermissionSet("residence.select")) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Select.Disabled"));
	    return true;
	}
	if (args.length == 2) {
	    if (args[1].equals("size") || args[1].equals("cost")) {
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    try {
			Residence.getSelectionManager().showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		} else if (Residence.getSelectionManager().worldEdit(player)) {
		    try {
			Residence.getSelectionManager().showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		}
	    } else if (args[1].equals("vert")) {
		Residence.getSelectionManager().vert(player, resadmin);
		return true;
	    } else if (args[1].equals("sky")) {
		Residence.getSelectionManager().sky(player, resadmin);
		return true;
	    } else if (args[1].equals("bedrock")) {
		Residence.getSelectionManager().bedrock(player, resadmin);
		return true;
	    } else if (args[1].equals("coords")) {
		player.sendMessage(Residence.getLM().getMessage("General.Separator"));
		Location playerLoc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
		if (playerLoc1 != null) {
		    player.sendMessage(Residence.getLM().getMessage("Select.Primary", Residence.getLM().getMessage("General.CoordsTop", playerLoc1.getBlockX(), playerLoc1
			.getBlockY(), playerLoc1.getBlockZ())));
		}
		Location playerLoc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());
		if (playerLoc2 != null) {
		    player.sendMessage(Residence.getLM().getMessage("Select.Secondary", Residence.getLM().getMessage("General.CoordsBottom", playerLoc2.getBlockX(), playerLoc2
			.getBlockY(), playerLoc2.getBlockZ())));
		}
		player.sendMessage(Residence.getLM().getMessage("General.Separator"));
		return true;
	    } else if (args[1].equals("chunk")) {
		Residence.getSelectionManager().selectChunk(player);
		return true;
	    } else if (args[1].equals("worldedit")) {
		if (Residence.getSelectionManager().worldEdit(player)) {
		    player.sendMessage(Residence.getLM().getMessage("Select.Success"));
		}
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("expand")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
		    return true;
		}
		Residence.getSelectionManager().modify(player, false, amount);
		return true;
	    } else if (args[1].equals("shift")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
		    return true;
		}
		Residence.getSelectionManager().modify(player, true, amount);
		return true;
	    }
	}
	if ((args.length == 2 || args.length == 3) && args[1].equals("auto")) {
	    Player target = player;
	    if (args.length == 3) {
		if (!player.hasPermission("residence.select.auto.others")) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}
		target = Bukkit.getPlayer(args[2]);
		if (target == null) {
		    player.sendMessage(Residence.getLM().getMessage("General.NotOnline"));
		    return true;
		}
	    }
	    AutoSelection.switchAutoSelection(target);
	    return true;
	}
	if (args.length > 1 && args[1].equals("residence")) {
	    String resName;
	    String areaName;
	    ClaimedResidence res = null;
	    if (args.length > 2) {
		res = Residence.getResidenceManager().getByName(args[2]);
	    } else {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    }
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
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
		Residence.getSelectionManager().placeLoc1(player, area.getHighLoc(), true);
		Residence.getSelectionManager().placeLoc2(player, area.getLowLoc(), true);
		player.sendMessage(Residence.getLM().getMessage("Select.Area", areaName, resName));
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Area.NonExist"));
	    }
	    return true;
	} else {
	    try {
		Residence.getSelectionManager().selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		return true;
	    } catch (Exception ex) {
		player.sendMessage(Residence.getLM().getMessage("Select.Fail"));
		return true;
	    }
	}
    }

}

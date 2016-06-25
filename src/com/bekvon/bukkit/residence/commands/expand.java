package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class expand implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	ClaimedResidence res = null;
	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length == 3) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	} else
	    return false;

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}

	if (res.isSubzone() && !player.hasPermission("residence.expand.subzone") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Subzone.CantExpand"));
	    return false;
	}

	if (!res.isSubzone() && !player.hasPermission("residence.expand") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.CantExpandResidence"));
	    return false;
	}

	String resName = res.getName();
	CuboidArea area = null;
	String areaName = null;

	if (args.length == 2) {
	    areaName = res.getAreaIDbyLoc(player.getLocation());
	    area = res.getArea(areaName);
	} else if (args.length == 3) {
	    areaName = res.isSubzone() ? Residence.getResidenceManager().getSubzoneNameByRes(res) : "main";
	    area = res.getCuboidAreabyName(areaName);
	}

	if (area != null) {
	    Residence.getSelectionManager().placeLoc1(player, area.getHighLoc(), false);
	    Residence.getSelectionManager().placeLoc2(player, area.getLowLoc(), false);
	    player.sendMessage(Residence.getLM().getMessage("Select.Area", areaName, resName));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Area.NonExist"));
	    return true;
	}
	int amount = -1;
	try {
	    if (args.length == 2)
		amount = Integer.parseInt(args[1]);
	    else if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	} catch (Exception ex) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
	    return true;
	}

	if (amount > 1000) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
	    return true;
	}

	if (amount < 0)
	    amount = 1;

	Residence.getSelectionManager().modify(player, false, amount);

	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wep != null) {
		if (Residence.wepid == Residence.getConfigManager().getSelectionTooldID()) {
		    Residence.getSelectionManager().worldEdit(player);
		}
	    }

	    res.replaceArea(player, Residence.getSelectionManager().getSelectionCuboid(player), areaName, resadmin);
	    return true;
	}
	player.sendMessage(Residence.getLM().getMessage("Select.Points"));

	return false;
    }

}

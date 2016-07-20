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
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class contract implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1900)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	ClaimedResidence res = null;
	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length == 3)
	    res = Residence.getResidenceManager().getByName(args[1]);
	else
	    return false;
	if (res == null) {
	    Residence.msg(player, lm.Invalid_Residence);
	    return true;
	}

	if (res.isSubzone() && !player.hasPermission("residence.contract.subzone") && !resadmin) {
	    Residence.msg(player, lm.Subzone_CantContract);
	    return false;
	}

	if (!res.isSubzone() && !player.hasPermission("residence.contract") && !resadmin) {
	    Residence.msg(player, lm.Residence_CantContractResidence);
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
	    Residence.msg(player, lm.Select_Area, areaName, resName);
	} else {
	    Residence.msg(player, lm.Area_NonExist);
	    return true;
	}
	int amount = -1;
	try {
	    if (args.length == 2)
		amount = Integer.parseInt(args[1]);
	    else if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	} catch (Exception ex) {
	    Residence.msg(player, lm.Invalid_Amount);
	    return true;
	}

	if (amount > 1000) {
	    Residence.msg(player, lm.Invalid_Amount);
	    return true;
	}

	if (amount < 0)
	    amount = 1;

	if (!Residence.getSelectionManager().contract(player, amount))
	    return true;

	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.getWEplugin() != null) {
		if (Residence.wepid == Residence.getConfigManager().getSelectionTooldID()) {
		    Residence.getSelectionManager().worldEdit(player);
		}
	    }
	    res.replaceArea(player, Residence.getSelectionManager().getSelectionCuboid(player), areaName, resadmin);
	    return true;
	}
	Residence.msg(player, lm.Select_Points);

	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Contracts residence in direction you looking");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res contract (residence) [amount]", "Contracts residence in direction you looking.",
	    "Residence name is optional"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%1", "1"));
    }

}

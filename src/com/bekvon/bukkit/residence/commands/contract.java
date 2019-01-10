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
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class contract implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1900)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	ClaimedResidence res = null;
	if (args.length == 2)
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length == 3)
	    res = plugin.getResidenceManager().getByName(args[1]);
	else
	    return false;
	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return true;
	}

	if (res.isSubzone() && !resadmin && !plugin.hasPermission(player, "residence.contract.subzone", lm.Subzone_CantContract))
	    return true;

	if (!res.isSubzone() && !resadmin && !plugin.hasPermission(player, "residence.contract", lm.Residence_CantContractResidence))
	    return true;

	String resName = res.getName();
	CuboidArea area = null;
	String areaName = null;

	if (args.length == 2) {
	    areaName = res.getAreaIDbyLoc(player.getLocation());
	    area = res.getArea(areaName);
	} else if (args.length == 3) {
	    areaName = res.isSubzone() ? plugin.getResidenceManager().getSubzoneNameByRes(res) : "main";
	    area = res.getCuboidAreabyName(areaName);
	}

	if (area != null) {
	    plugin.getSelectionManager().placeLoc1(player, area.getHighLoc(), false);
	    plugin.getSelectionManager().placeLoc2(player, area.getLowLoc(), false);
	    plugin.msg(player, lm.Select_Area, areaName, resName);
	} else {
	    plugin.msg(player, lm.Area_NonExist);
	    return true;
	}
	int amount = -1;
	try {
	    if (args.length == 2)
		amount = Integer.parseInt(args[1]);
	    else if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	} catch (Exception ex) {
	    plugin.msg(player, lm.Invalid_Amount);
	    return true;
	}

	if (amount > 1000) {
	    plugin.msg(player, lm.Invalid_Amount);
	    return true;
	}

	if (amount < 0)
	    amount = 1;

	if (!plugin.getSelectionManager().contract(player, amount))
	    return true;

	if (plugin.getSelectionManager().hasPlacedBoth(player.getName())) {
	    res.replaceArea(player, plugin.getSelectionManager().getSelectionCuboid(player), areaName, resadmin);
	    return true;
	}
	plugin.msg(player, lm.Select_Points);

	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Contracts residence in direction you looking");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res contract (residence) [amount]", "Contracts residence in direction you looking.",
	    "Residence name is optional"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%1", "1"));
    }

}

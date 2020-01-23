package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class area implements cmd {

    @Override
    @CommandAnnotation(info = "Manage physical areas for a residence.",priority = 3300)
    public boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if (args.length == 3) {
	    if (args[0].equals("remove")) {
		ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
		if (res != null) {
		    res.removeArea(player, args[2], resadmin);
		} else {
		    plugin.msg(player, lm.Invalid_Residence);
		}
		return true;
	    } else if (args[0].equals("add")) {
		if (plugin.getWorldEdit() != null) {
		    if (plugin.getWorldEditTool().equals(plugin.getConfigManager().getSelectionTool())) {
			plugin.getSelectionManager().worldEdit(player);
		    }
		}
		if (plugin.getSelectionManager().hasPlacedBoth(player)) {
		    ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
		    if (res != null) {
			if (res.addArea(player, plugin.getSelectionManager().getSelectionCuboid(player), args[2], resadmin))
			    plugin.msg(player, lm.Area_Create, args[2]);
		    } else {
			plugin.msg(player, lm.Invalid_Residence);
		    }
		} else {
		    plugin.msg(player, lm.Select_Points);
		}
		return true;
	    } else if (args[0].equals("replace")) {
		if (plugin.getWorldEdit() != null) {
		    if (plugin.getWorldEditTool().equals(plugin.getConfigManager().getSelectionTool())) {
			plugin.getSelectionManager().worldEdit(player);
		    }
		}
		if (plugin.getSelectionManager().hasPlacedBoth(player)) {
		    ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
		    if (res != null) {
			res.replaceArea(player, plugin.getSelectionManager().getSelectionCuboid(player), args[2], resadmin);
		    } else {
			plugin.msg(player, lm.Invalid_Residence);
		    }
		} else {
		    plugin.msg(player, lm.Select_Points);
		}
		return true;
	    }
	}
	if ((args.length == 2 || args.length == 3) && args[0].equals("list")) {
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
	    if (res != null) {
		res.printAreaList(player, page);
	    } else {
		plugin.msg(player, lm.Invalid_Residence);
	    }
	    return true;
	} else if ((args.length == 2 || args.length == 3) && args[0].equals("listall")) {
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);
	    if (res != null) {
		res.printAdvancedAreaList(player, page);
	    } else {
		plugin.msg(player, lm.Invalid_Residence);
	    }
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	// Sub commands
	c.setP(c.getPath() + "SubCommands.");
	c.get("list.Description", "List physical areas in a residence");
	c.get("list.Info", Arrays.asList("&eUsage: &6/res area list [residence] <page>"));	
	LocaleManager.addTabComplete(this, "list", "[residence]");

	c.get("listall.Description", "List coordinates and other Info for areas");
	c.get("listall.Info", Arrays.asList("&eUsage: &6/res area listall [residence] <page>"));	
	
	LocaleManager.addTabComplete(this, "listall", "[residence]");
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "listall"), Arrays.asList("[residence]"));

	c.get("add.Description", "Add physical areas to a residence");
	c.get("add.Info", Arrays.asList("&eUsage: &6/res area add [residence] [areaID]", "You must first select two points first."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "add"), Arrays.asList("[residence]"));

	c.get("remove.Description", "Remove physical areas from a residence");
	c.get("remove.Info", Arrays.asList("&eUsage: &6/res area remove [residence] [areaID]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "remove"), Arrays.asList("[residence]"));

	c.get("replace.Description", "Replace physical areas in a residence");
	c.get("replace.Info", Arrays.asList("&eUsage: &6/res area replace [residence] [areaID]",
	    "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "replace"), Arrays.asList("[residence]"));

    }
}

package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;

public class reload implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5800)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!resadmin && !sender.isOp()) {
	    plugin.msg(sender, lm.General_NoPermission);
	    return true;
	}

	if (args.length != 2) {
	    return false;
	}

	if (args[1].equalsIgnoreCase("lang")) {
	    plugin.getLM().LanguageReload();	    
	    plugin.getLocaleManager().LoadLang(plugin.getConfigManager().getLanguage());
	    sender.sendMessage(plugin.getPrefix() + " Reloaded language file.");
	    return true;
	} else if (args[1].equalsIgnoreCase("config")) {
	    plugin.getConfigManager().UpdateConfigFile();
	    sender.sendMessage(plugin.getPrefix() + " Reloaded config file.");
	    return true;
	} else if (args[1].equalsIgnoreCase("groups")) {
	    plugin.getConfigManager().loadGroups();
	    plugin.gmanager = new PermissionManager(plugin);
	    plugin.wmanager = new WorldFlagManager(plugin);
	    sender.sendMessage(plugin.getPrefix() + " Reloaded groups file.");
	    return true;
	} else if (args[1].equalsIgnoreCase("flags")) {
	    plugin.getConfigManager().loadFlags();
	    plugin.gmanager = new PermissionManager(plugin);
	    plugin.imanager = new WorldItemManager(plugin);
	    plugin.wmanager = new WorldFlagManager(plugin);
	    sender.sendMessage(plugin.getPrefix() + " Reloaded flags file.");
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "reload lanf or config files");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res reload [config/lang/groups/flags]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("config%%lang%%groups%%flags"));
    }
}

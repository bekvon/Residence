package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class raid implements cmd {

    enum States {
	start, stop, immunity, kick;
	public static States getState(String name) {
	    for (States one : States.values()) {
		if (one.toString().equalsIgnoreCase(name))
		    return one;
	    }
	    return  null;
	}

    }

    @Override
    @CommandAnnotation(simple = true, priority = 3100, regVar = {  1, 2 }, consoleVar = { 1, 2 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	final Player player = (Player) sender;

	if (args.length != 0 && args.length != 1)
	    return false;

	if (!ConfigManager.RaidEnabled) {
	    plugin.msg(sender, lm.Raid_NotEnabled);
	    return true;
	}

	if (!resadmin) {
	    plugin.msg(sender, lm.General_NoPermission);
	}

	States state = States.getState(args[1]);
	
	if (state == null) {	    
	    return false;
	}
	
	switch(state) {
	case immunity:
	    break;
	case kick:
	    
	    
	    
	    break;
	case start:
	    break;
	case stop:
	    break;
	default:
	    break;	
	}
	
	// raid start [resname/playerName/currentres]
	// raid stop [resname/playerName/currentres]
	// raid kick [resname/playerName]
	// raid immunity [add/take/set/clear] [resname/playerName/currentres]

	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Manage raid in residence");
	c.get("Info", Arrays.asList("&eUsage: &6/res defend [resName] (playerName)"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]%%[playername]"));
    }

}

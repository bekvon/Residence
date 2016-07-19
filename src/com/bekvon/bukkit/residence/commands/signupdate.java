package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class signupdate implements cmd {
    
    @Override
    @CommandAnnotation(simple = false, priority = 5700)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length == 1) {
	    if (!resadmin) {
		Residence.msg(sender, lm.General_NoPermission);
		return true;
	    }
	    int number = Residence.getSignUtil().updateAllSigns();
	    Residence.msg(sender, lm.Sign_Updated, number);
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Updated residence signs");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res signupdate"));
    }
}

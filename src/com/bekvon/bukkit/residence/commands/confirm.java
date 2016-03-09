package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class confirm implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	Player player = null;
	String name = "Console";
	if (sender instanceof Player) {
	    player = (Player) sender;
	    name = player.getName();
	}
	if (args.length != 1)
	    return true;

	String area = Residence.deleteConfirm.get(name);
	if (area == null) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	} else {
	    Residence.getResidenceManager().removeResidence(player, area, resadmin);
	    Residence.deleteConfirm.remove(name);
	    sender.sendMessage(Residence.getLM().getMessage("Residence.Remove", name));
	}

	return true;
    }

}

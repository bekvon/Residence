package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.utils.Debug;

public class give implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	Debug.D("ss");
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	Debug.D(args.toString());

	Debug.D("ss2");
	if (args.length != 3)
	    return false;

	Debug.D("ss1");
	Residence.getResidenceManager().giveResidence(player, args[2], args[1], resadmin);
	return true;
    }
}

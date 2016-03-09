package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class check implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;
	Player player = (Player) sender;
	String pname = player.getName();

	if (args.length != 3 && args.length != 4)
	    return false;

	if (args.length == 4)
	    pname = args[3];

	ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}
	if (!res.getPermissions().hasApplicableFlag(pname, args[2])) {
	    player.sendMessage(Residence.getLM().getMessage("Flag.CheckFalse", args[2], pname, args[1]));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Flag.CheckTrue", args[2], pname, args[1], (res.getPermissions().playerHas(pname, res.getPermissions()
		.getWorld(), args[2], false) ? Residence.getLM().getMessage("General.True") : Residence.getLM().getMessage("General.False"))));
	}
	return true;
    }

}

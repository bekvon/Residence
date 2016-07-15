package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class setowner implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {

	if (args.length < 3)
	    return false;

	if (!resadmin) {
	    sender.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	}

	ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	if (area != null) {
	    area.getPermissions().setOwner(args[2], true);
	    if (Residence.getRentManager().isForRent(area.getName()))
		Residence.getRentManager().removeRentable(area.getName());
	    if (Residence.getTransactionManager().isForSale(area.getName()))
		Residence.getTransactionManager().removeFromSale(area.getName());
	    area.getPermissions().applyDefaultFlags();

	    if (area.getParent() == null) {
		sender.sendMessage(Residence.getLM().getMessage("Residence.OwnerChange", args[1], args[2]));
	    } else {
		sender.sendMessage(Residence.getLM().getMessage("Subzone.OwnerChange", args[1].split("\\.")[args[1].split("\\.").length - 1], args[2]));
	    }
	} else {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
	return true;
    }

}

package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.gui.SetFlag;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class set implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 3) {
	    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    if (area != null) {
		Residence.getResidenceManager().getByName(area).getPermissions().setFlag(player, args[1], args[2], resadmin);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setFlag(player, args[2], args[3], resadmin);
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return true;
	} else if ((args.length == 1 || args.length == 2) && Residence.getConfigManager().useFlagGUI()) {
	    ClaimedResidence res = null;
	    if (args.length == 1)
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    else
		res = Residence.getResidenceManager().getByName(args[1]);
	    if (res == null) {

		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }
	    if (!res.isOwner(player) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return true;
	    }
	    SetFlag flag = new SetFlag(res.getName(), player, resadmin);
	    flag.recalculateResidence(res);
	    player.closeInventory();	   
	    Residence.getPlayerListener().getGUImap().put(player.getName(), flag);
	    player.openInventory(flag.getInventory());
	    return true;
	}
	return false;
    }
}

package com.bekvon.bukkit.residence.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;

public class tool implements cmd {

    @SuppressWarnings("deprecation")
    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	player.sendMessage(Residence.getLM().getMessage("Select.Tool", Material.getMaterial(Residence.getConfigManager().getSelectionTooldID())));
	player.sendMessage(Residence.getLM().getMessage("General.InfoTool", Material.getMaterial(Residence.getConfigManager().getInfoToolID())));
	player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	return true;
    }
}

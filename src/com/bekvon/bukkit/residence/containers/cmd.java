package com.bekvon.bukkit.residence.containers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;

public interface cmd {

    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender);

    public void getLocale(ConfigReader c, String path);

}

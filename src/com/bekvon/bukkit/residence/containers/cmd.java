package com.bekvon.bukkit.residence.containers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface cmd {
    
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender);

    public void getLocale(ConfigReader c, String path);

}

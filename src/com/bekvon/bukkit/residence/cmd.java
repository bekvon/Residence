package com.bekvon.bukkit.residence;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface cmd {
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender);
}

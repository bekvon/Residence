/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;
import org.bukkit.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceCommandEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected boolean cancelled;
    protected String cmd;
    protected String arglist[];
    CommandSender commandsender;

    public ResidenceCommandEvent(String command, String args[], CommandSender sender)
    {
        super();
        cancelled = false;
        arglist = args;
        cmd = command;
        commandsender = sender;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean bln) {
        cancelled = bln;
    }

    public String getCommand()
    {
        return cmd;
    }

    public String[] getArgs()
    {
        return arglist;
    }

    public CommandSender getSender()
    {
        return commandsender;
    }

}

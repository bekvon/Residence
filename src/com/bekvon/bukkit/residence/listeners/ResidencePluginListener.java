package com.bekvon.bukkit.residence.listeners;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;




public class ResidencePluginListener implements Listener 
{
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPluginEnabled(PluginEvent event) 
    {
        // TODO: Move plugin events here!
    }
}
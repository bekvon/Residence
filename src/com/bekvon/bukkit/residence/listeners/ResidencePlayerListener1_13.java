package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;

public class ResidencePlayerListener1_13 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_13(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.PHYSICAL) || !event.getClickedBlock().getType().equals(Material.TURTLE_EGG))
            return;
        if (!ResidenceBlockListener.canBreakBlock(event.getPlayer(), event.getClickedBlock(), true))
            event.setCancelled(true);        
    }
}

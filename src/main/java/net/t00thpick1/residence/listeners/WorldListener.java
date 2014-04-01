package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {
    @EventHandler
    public void onCreate(WorldLoadEvent event) {
        ResidenceAPI.getResidenceWorld(event.getWorld());
    }
}

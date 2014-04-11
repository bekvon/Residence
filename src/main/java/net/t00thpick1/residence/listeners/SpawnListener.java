package net.t00thpick1.residence.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.utils.Utilities;

public class SpawnListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.CUSTOM && ConfigManager.getInstance().ignorePluginSpawns()) {
            return;
        }
        EntityType type = event.getEntity().getType();
        Flag flag = null;
        if (Utilities.isAnimal(type)) {
            flag = FlagManager.ANIMALSPAWN;
        } else {
            flag = FlagManager.MONSTERSPAWN;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getLocation()).allowAction(flag)) {
            event.setCancelled(true);
        }
    }
}

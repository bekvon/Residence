package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class FlowListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Material mat = event.getBlock().getType();
        Flag flag = null;
        if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
            flag = FlagManager.LAVAFLOW;
        }
        if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
            flag = FlagManager.WATERFLOW;
        }
        if (flag == null) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(flag)) {
            event.setCancelled(true);
        }
    }
}

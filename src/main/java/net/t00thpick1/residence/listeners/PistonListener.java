package net.t00thpick1.residence.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.FlagManager;

public class PistonListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(FlagManager.PISTON)) {
            event.setCancelled(true);
            return;
        }
        if (!event.isSticky()) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getRetractLocation()).allowAction(FlagManager.PISTON)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(FlagManager.PISTON)) {
            event.setCancelled(true);
            return;
        }
        for (Block block : event.getBlocks()) {
            if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(FlagManager.PISTON)) {
                event.setCancelled(true);
                return;
            }
            Location blockto = block.getLocation();
            blockto.setX(blockto.getX() + event.getDirection().getModX());
            blockto.setY(blockto.getY() + event.getDirection().getModY());
            blockto.setZ(blockto.getZ() + event.getDirection().getModZ());
            if (!ResidenceAPI.getPermissionsAreaByLocation(blockto).allowAction(FlagManager.PISTON)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}

package net.t00thpick1.residence.flags.enviromental;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.Plugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public class PistonFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("PistonFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
            return;
        }
        if (!event.isSticky()) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getRetractLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
            return;
        }
        for (Block block : event.getBlocks()) {
            if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()))) {
                event.setCancelled(true);
                return;
            }
            Location blockto = block.getLocation();
            blockto.setX(blockto.getX() + event.getDirection().getModX());
            blockto.setY(blockto.getY() + event.getDirection().getModY());
            blockto.setZ(blockto.getZ() + event.getDirection().getModZ());
            if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(blockto))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new PistonFlag(), plugin);
    }
}

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
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class PistonFlag extends Flag implements Listener {
    private PistonFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final PistonFlag FLAG = new PistonFlag(LocaleLoader.getString("Flags.Flags.Piston"), FlagType.AREA_ONLY, null);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(this)) {
            event.setCancelled(true);
            return;
        }
        if (!event.isSticky()) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getRetractLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(this)) {
            event.setCancelled(true);
            return;
        }
        for (Block block : event.getBlocks()) {
            if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(this)) {
                event.setCancelled(true);
                return;
            }
            Location blockto = block.getLocation();
            blockto.setX(blockto.getX() + event.getDirection().getModX());
            blockto.setY(blockto.getY() + event.getDirection().getModY());
            blockto.setZ(blockto.getZ() + event.getDirection().getModZ());
            if (!ResidenceAPI.getPermissionsAreaByLocation(blockto).allowAction(this)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

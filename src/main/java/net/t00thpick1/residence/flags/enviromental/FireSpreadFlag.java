package net.t00thpick1.residence.flags.enviromental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.plugin.Plugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public class FireSpreadFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("FireSpreadFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() != IgniteCause.SPREAD) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new FireSpreadFlag(), plugin);
    }
}

package net.t00thpick1.residence.flags.enviromental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.plugin.Plugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class FireSpreadFlag extends Flag implements Listener {
    private FireSpreadFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final FireSpreadFlag FLAG = new FireSpreadFlag(LocaleLoader.getString("Flags.Flags.FireSpread"), FlagType.AREA_ONLY, null);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() != IgniteCause.SPREAD) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

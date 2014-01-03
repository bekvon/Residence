package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

public class EndermanPickupFlag extends BuildFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.EndermanPickup");
    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.ENDERMAN) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new EndermanPickupFlag(), plugin);
    }
}

package net.t00thpick1.residence.flags.enviromental.explosion;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

public class WitherExplosionFlag extends ExplosionFlag {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.WitherExplosion");
    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    protected boolean shouldCheck(EntityType type) {
        return type == EntityType.WITHER || type == EntityType.WITHER_SKULL;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWitherDoSomethingToBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.WITHER) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
                event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new WitherExplosionFlag(), plugin);
    }
}

package net.t00thpick1.residence.flags.enviromental;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public class DamageFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("DamageFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        Entity ent = event.getEntity();
        if (ent.getType() == EntityType.ITEM_FRAME) {
            return;
        }
        if (!(ent instanceof Player || (ent instanceof Wolf && ((Wolf) ent).isTamed()))) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()))) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                ent.setFireTicks(0);
            }
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new DamageFlag(), plugin);
    }
}

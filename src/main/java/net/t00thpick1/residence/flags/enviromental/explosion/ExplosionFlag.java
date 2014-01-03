package net.t00thpick1.residence.flags.enviromental.explosion;

import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public abstract class ExplosionFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("ExplosionFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    protected abstract boolean shouldCheck(EntityType type);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        EntityType entity = event.getEntityType();
        if (!shouldCheck(entity)) {
            return;
        }
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Location loc = it.next().getLocation();
            if (allowAction(ResidenceAPI.getPermissionsAreaByLocation(loc))) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplosion(EntityDamageByEntityEvent event) {
        if (event.getCause() != DamageCause.ENTITY_EXPLOSION && event.getCause() != DamageCause.BLOCK_EXPLOSION) {
            return;
        }
        EntityType entity = event.getDamager().getType();
        if (!shouldCheck(entity)) {
            return;
        }
        if (allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()))) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        CreeperFlag.initialize();
        BedExplosionFlag.initialize();
        TNTFlag.initialize();
        WitherExplosionFlag.initialize();
        FireballFlag.initialize();
    }
}

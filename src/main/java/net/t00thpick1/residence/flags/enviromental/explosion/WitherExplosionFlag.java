package net.t00thpick1.residence.flags.enviromental.explosion;

import java.util.Iterator;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class WitherExplosionFlag extends Flag implements Listener {
    private WitherExplosionFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final WitherExplosionFlag FLAG = new WitherExplosionFlag(LocaleLoader.getString("Flags.Flags.WitherExplosion"), FlagType.AREA_ONLY, ExplosionFlag.FLAG);

    protected boolean shouldCheck(EntityType type) {
        return type == EntityType.WITHER || type == EntityType.WITHER_SKULL;
    }

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
            if (ResidenceAPI.getPermissionsAreaByLocation(loc).allowAction(this)) {
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
        if (ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWitherDoSomethingToBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.WITHER) {
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

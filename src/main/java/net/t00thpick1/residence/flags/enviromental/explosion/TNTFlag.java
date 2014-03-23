package net.t00thpick1.residence.flags.enviromental.explosion;

import java.util.Iterator;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class TNTFlag extends Flag implements Listener {
    private TNTFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final TNTFlag FLAG = new TNTFlag(LocaleLoader.getString("Flags.Flags.TNT"), FlagType.AREA_ONLY, ExplosionFlag.FLAG);

    private boolean shouldCheck(EntityType type) {
        return type == EntityType.PRIMED_TNT || type == EntityType.MINECART_TNT;
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

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

package net.t00thpick1.residence.listeners;

import java.util.Iterator;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        Flag flag = null;
        if (event.getEntity() != null) {
            EntityType entity = event.getEntityType();
            flag = getFlag(entity);
            if (flag == null) {
                return;
            }
        } else {
            flag = FlagManager.BEDEXPLOSION;
        }
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Location loc = it.next().getLocation();
            if (ResidenceAPI.getPermissionsAreaByLocation(loc).allowAction(flag)) {
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
        Flag flag = getFlag(entity);
        if (flag == null) {
            return;
        }
        if (ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()).allowAction(flag)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplosion(EntityDamageByBlockEvent event) {
        if (event.getCause() != DamageCause.BLOCK_EXPLOSION) {
            return;
        }
        Block block = event.getDamager();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.BED_BLOCK && block.getType() != Material.BED) {
            return;
        }
        if (ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()).allowAction(FlagManager.BEDEXPLOSION)) {
            event.setCancelled(true);
        }
    }

    private Flag getFlag(EntityType entity) {
        switch (entity) {
            case WITHER:
            case WITHER_SKULL:
                return FlagManager.WITHEREXPLOSION;
            case PRIMED_TNT:
            case MINECART_TNT:
                return FlagManager.TNTEXPLOSION;
            case SMALL_FIREBALL:
            case FIREBALL:
                return FlagManager.FIREBALLEXPLOSION;
            case CREEPER:
                return FlagManager.CREEPEREXPLOSION;
            default:
                return null;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWitherDoSomethingToBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.WITHER) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(FlagManager.WITHEREXPLOSION)) {
            event.setCancelled(true);
        }
    }
}

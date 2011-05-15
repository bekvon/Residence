/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;

/**
 *
 * @author Administrator
 */
public class ResidenceEntityListener extends EntityListener {

    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.isCancelled())
            return;
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getLocation());
        if (res != null) {
            if (!res.getPermissions().has("monsters", true)) {
                event.setCancelled(true);
            }
        }
        super.onCreatureSpawn(event);
    }

    @Override
    public void onEntityCombust(EntityCombustEvent event) {
        if(event.isCancelled())
            return;
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getEntity().getLocation());
        if (res != null) {
            if (!res.getPermissions().has("ignite", true)) {
                event.setCancelled(true);
            }
        }
        super.onEntityCombust(event);
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.isCancelled())
            return;
        Entity ent = event.getEntity();
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getLocation());
        if(!explosionProximityCheck(event.getLocation(), ent instanceof LivingEntity))
            event.setCancelled(true);
        else if(res != null) {
            if (ent instanceof LivingEntity) {
                if (!res.getPermissions().has("creeper", true)) {
                    event.setCancelled(true);
                }
            }
            else {
                if (!res.getPermissions().has("tnt", true)) {
                    event.setCancelled(true);
                }
            }
        }
        else
        {
            if(ent instanceof LivingEntity)
            {
                if(!Residence.getWorldFlags().getPerms(ent.getWorld().getName()).has("creeper", true))
                    event.setCancelled(true);
            }
            else
            {
                if(!Residence.getWorldFlags().getPerms(ent.getWorld().getName()).has("tnt", true))
                    event.setCancelled(true);
            }
        }
        super.onEntityExplode(event);
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.isCancelled())
            return;
        Entity ent = event.getEntity();
        ClaimedResidence area = Residence.getResidenceManger().getByLoc(ent.getLocation());
        /* Living Entities */
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
            Entity damager = attackevent.getDamager();
            ent = attackevent.getEntity();

            if ((ent instanceof Player) && (damager instanceof Player)) {
                /* Check for Player vs Player */
                if (area == null) {
                    /* World PvP */
                    if (!Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has("pvp", true)) {
                        ((Player) damager).sendMessage("§cWorld PVP is disabled.");
                        event.setCancelled(true);
                    }
                } else {
                    /* Normal PvP */
                    if (!area.getPermissions().has("pvp", true)) {
                        ((Player) damager).sendMessage("§cPlayer is in a No-PVP zone.");
                        event.setCancelled(true);
                    }
                }
                return;
            }
            else if ((ent instanceof Player) && (damager instanceof Creeper)) {
                if (area == null) {
                    if (!Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has("creeper", true)) {
                        event.setCancelled(true);
                    }
                } else {
                    if (!area.getPermissions().has("creeper", true)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        if (area == null) {
            if (!Residence.getWorldFlags().getPerms(ent.getWorld().getName()).has("damage", true) && ent instanceof Player) {
                event.setCancelled(true);
            }
        } else {
            if (!area.getPermissions().has("damage", true) && ent instanceof Player) {
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) {
            /* Put out a fire on a player */
            if ((ent instanceof Player)
                    && (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
                ent.setFireTicks(0);
            }
        }
    }
    
    private boolean explosionProximityCheck(Location loc, boolean creeper) {
        ResidenceManager manager = Residence.getResidenceManger();
        ClaimedResidence res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }

        loc.setX(loc.getX() + 4);
        res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }
        loc.setX(loc.getX() - 4);

        loc.setY(loc.getY() + 4);
        res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }
        loc.setY(loc.getY() - 4);

        loc.setZ(loc.getZ() + 4);
        res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }
        loc.setZ(loc.getZ() - 4);

        loc.setX(loc.getX() - 4);
        res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }
        loc.setX(loc.getX() + 4);

        loc.setY(loc.getY() - 4);
        res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }
        loc.setY(loc.getY() + 4);

        loc.setZ(loc.getZ() - 4);
        res = manager.getByLoc(loc);
        if (res != null) {
            if (creeper) {
                if (!res.getPermissions().has("creeper", true)) {
                    return false;
                }
            } else {
                if (!res.getPermissions().has("tnt", true)) {
                    return false;
                }
            }
        }
        return true;
    }

}

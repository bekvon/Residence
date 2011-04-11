/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author Administrator
 */
public class ResidenceEntityListener extends EntityListener {

    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
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
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getEntity().getLocation());
        if (res != null) {
            if (!res.getPermissions().has("fire", true)) {
                event.setCancelled(true);
            }
        }
        else if(!Residence.getConfig().worldFireEnabled())
            event.setCancelled(true);
        super.onEntityCombust(event);
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getLocation());
        if (res != null) {
            if (!res.getPermissions().has("explosions", true)) {
                event.setCancelled(true);
            }
        }
        else if(!Residence.getConfig().worldExplosionsEnabled())
        {
            event.setCancelled(true);
        }
        super.onEntityExplode(event);
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
            Entity ent = attackevent.getEntity();
            if (ent instanceof Player) {
                ClaimedResidence area = Residence.getResidenceManger().getByLoc(ent.getLocation());
                if (area != null) {
                    Entity damager = attackevent.getDamager();
                    if (!area.getPermissions().has("pvp",true) && damager instanceof Player) {
                        event.setCancelled(true);
                        Player attacker = (Player) damager;
                        attacker.sendMessage("§cPlayer is in a No-PVP zone.");
                    }
                    else if(!(damager instanceof Player) && !area.getPermissions().has("damage",true)) {
                        event.setCancelled(true);
                    }
                }
                else
                {
                    Entity damager = attackevent.getDamager();
                    if(damager instanceof Player)
                    {
                        if(!Residence.getConfig().worldPvpEnabled())
                        {
                            ((Player)damager).sendMessage("§cWorld PVP is disabled.");
                            event.setCancelled(true);
                        }
                    }
                    else
                    {
                        if(!Residence.getConfig().worldDamageEnabled())
                            event.setCancelled(true);
                    }
                }
            }
        }
        super.onEntityDamage(event);
    }

}

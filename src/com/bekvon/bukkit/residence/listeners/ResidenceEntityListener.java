/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;
import org.bukkit.ChatColor;

import java.util.Iterator;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;

/**
 *
 * @author Administrator
 */
public class ResidenceEntityListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEndermanChangeBlock(EntityChangeBlockEvent  event) {
    	if(event.getEntityType() != EntityType.ENDERMAN)
    	{
    		return;
    	}
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (res != null) {
            ResidencePermissions perms = res.getPermissions();
            if (!perms.has("build", true)) {
                event.setCancelled(true);
            }
        } else {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(event.getBlock().getLocation().getWorld().getName());
            if (!perms.has("build", true)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityInteract(EntityInteractEvent event){
    	Block block = event.getBlock();
    	Material mat = block.getType();
    	Entity entity = event.getEntity();    	
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
	boolean hastrample = true;
	boolean hasbuild = true;
        if (res != null) {
            hasbuild = res.getPermissions().has("hasbuild", true);
            hastrample = res.getPermissions().has("trample", hasbuild);
        } else {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(entity.getWorld().getName());
            hasbuild =  perms.has("build", true);
            hastrample =  perms.has("trample", hasbuild);
        }        			
	if(!hastrample && !(entity.getType() == EntityType.FALLING_BLOCK) && (mat == Material.SOIL || mat == Material.SOUL_SAND)){
	    event.setCancelled(true);
	}
    }
/*
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEndermanPlace(EndermanPlaceEvent event) {
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getLocation());
        if (res != null) {
            ResidencePermissions perms = res.getPermissions();
            if (!perms.has("build", true)) {
                event.setCancelled(true);
            }
        } else {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(event.getLocation().getWorld().getName());
            if (!perms.has("build", true)) {
                event.setCancelled(true);
            }
        }
    }
*/
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.isCancelled())
            return;
        FlagPermissions perms = Residence.getPermsByLoc(event.getLocation());
        Entity ent = event.getEntity();
        if(perms!=null)
        {
            if(ent instanceof Snowman || ent instanceof IronGolem || ent instanceof Ocelot || ent instanceof Pig || ent instanceof Sheep || ent instanceof Chicken || ent instanceof Wolf || ent instanceof Cow || ent instanceof Squid || ent instanceof Villager || ent instanceof Slime)
            {
                if(!perms.has("animals", true))
                {
                    event.setCancelled(true);
                }
            }
            else
            {
                if (!perms.has("monsters", true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPaintingPlace(PaintingPlaceEvent event) {
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        Player player = event.getPlayer();
        if(Residence.isResAdminOn(player)){
            return;
        }
        if(res!=null)
        {
            ResidencePermissions perms = res.getPermissions();
            String pname = player.getName();
            boolean hasbuild = perms.playerHas(pname, "build", true);
            boolean hasplace = perms.playerHas(pname, "place", hasbuild);
            if ((!hasbuild && !hasplace) || !hasplace) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            }
        } else {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(player);
            boolean hasbuild = perms.has("build", true);
            boolean hasplace = perms.has("destroy", hasbuild);
            if ((!hasbuild && !hasplace) || !hasplace) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPaintingBreak(PaintingBreakEvent event) {
		if(event instanceof PaintingBreakByEntityEvent)
		{
			PaintingBreakByEntityEvent evt = (PaintingBreakByEntityEvent) event;
			if(evt.getRemover() instanceof Player)
			{
				Player player = (Player) evt.getRemover();
				if(Residence.isResAdminOn(player)){
                   		    return;
               			}
				String pname = player.getName();
				ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getPainting().getLocation());
				if (res != null) {
					ResidencePermissions perms = res.getPermissions();
					boolean hasbuild = perms.playerHas(pname, "build", true);
					boolean hasplace = perms.playerHas(pname, "place", hasbuild);
					if ((!hasbuild && !hasplace) || !hasplace) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
					}
				} else {
					FlagPermissions perms = Residence.getWorldFlags().getPerms(player);
					boolean hasbuild = perms.has("build", true);
					boolean hasplace = perms.has("place", hasbuild);
					if ((!hasbuild && !hasplace) || !hasplace) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
					}
				}
			}
		}
	}

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombust(EntityCombustEvent event) {
        if(event.isCancelled())
            return;
        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        if (perms != null) {
            if (!perms.has("ignite", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if(event.isCancelled())
            return;
        if(this.checkExplosionCancel(event.getEntity(), event.getEntity().getLocation()))
        {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.isCancelled())
            return;
        if(this.checkExplosionCancel(event.getEntity(), event.getLocation()))
        {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    public boolean checkExplosionCancel(Entity ent, Location loc)
    {
        if(ent == null || loc == null)
            return false;
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        if(!explosionProximityCheck(loc, ent instanceof LivingEntity))
            return true;
        else if(res != null) {
            if (ent instanceof LivingEntity) {
                if (!res.getPermissions().has("creeper", true)) {
                    return true;
                }
            }
            else {
                if (!res.getPermissions().has("tnt", true)) {
                    return true;
                }
            }
        }
        else
        {
            World world = ent.getWorld();
            if(world == null)
                return false;
            if(ent instanceof LivingEntity)
            {
                if(!Residence.getWorldFlags().getPerms(world.getName()).has("creeper", true))
                    return true;
            }
            else
            {
                if(!Residence.getWorldFlags().getPerms(world.getName()).has("tnt", true))
                    return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSplashPotion(PotionSplashEvent event) {
    	if(event.isCancelled())
    		return;
    	Entity ent = event.getEntity();
    	ClaimedResidence srcarea = null;
    	boolean srcpvp = true;
    	srcarea = Residence.getResidenceManager().getByLoc(ent.getLocation());
    	if(srcarea != null)
    		srcpvp = srcarea.getPermissions().has("pvp", true);
    	Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
    	while(it.hasNext()){
    		LivingEntity target = it.next();
    		if(target instanceof HumanEntity){
    			if(!srcpvp){
    				event.setIntensity(target, 0);
    			}
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.isCancelled())
            return;
        Entity ent = event.getEntity();
        boolean tamedWolf = ent instanceof Wolf ? ((Wolf)ent).isTamed() : false;
        ClaimedResidence area = Residence.getResidenceManager().getByLoc(ent.getLocation());
        /* Living Entities */
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
            Entity damager = attackevent.getDamager();
            ClaimedResidence srcarea = null;
            if(damager!=null)
                srcarea = Residence.getResidenceManager().getByLoc(damager.getLocation());
            boolean srcpvp = true;
            if(srcarea !=null)
                srcpvp = srcarea.getPermissions().has("pvp", true);
            ent = attackevent.getEntity();
            if ((ent instanceof Player || tamedWolf) && (damager instanceof Player || (damager instanceof Arrow && (((Arrow)damager).getShooter() instanceof Player)))) {
                Player attacker = null;
                if(damager instanceof Player)
                    attacker = (Player) damager;
                else if(damager instanceof Arrow)
                    attacker = (Player)((Arrow)damager).getShooter();
                if(!srcpvp)
                {
                    attacker.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPVPZone"));
                    event.setCancelled(true);
                    return;
                }
                /* Check for Player vs Player */
                if (area == null) {
                    /* World PvP */
                    if (!Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has("pvp", true)) {
                        attacker.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("WorldPVPDisabled"));
                        event.setCancelled(true);
                    }
                } else {
                    /* Normal PvP */
                    if (!area.getPermissions().has("pvp", true)) {
                        attacker.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPVPZone"));
                        event.setCancelled(true);
                    }
                }
                return;
            }
            else if ((ent instanceof Player || tamedWolf) && (damager instanceof Creeper)) {
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
            if (!Residence.getWorldFlags().getPerms(ent.getWorld().getName()).has("damage", true) && (ent instanceof Player || tamedWolf)) {
                event.setCancelled(true);
            }
        } else {
            if (!area.getPermissions().has("damage", true) && (ent instanceof Player || tamedWolf)) {
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) {
            /* Put out a fire on a player */
            if ((ent instanceof Player || tamedWolf)
                    && (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
                ent.setFireTicks(0);
            }
        }
    }
    
    private boolean explosionProximityCheck(Location loc, boolean creeper) {
        ResidenceManager manager = Residence.getResidenceManager();
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

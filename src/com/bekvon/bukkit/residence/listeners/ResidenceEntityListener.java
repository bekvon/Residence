/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bekvon.bukkit.residence.protection.FlagPermissions;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
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

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Administrator
 */
public class ResidenceEntityListener implements Listener {

    protected Map<BlockState, ItemStack[]> ExplodeRestore = new HashMap<BlockState,ItemStack[]>();
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEndermanChangeBlock(EntityChangeBlockEvent  event) {
    	if(event.getEntityType() != EntityType.ENDERMAN){
    		return;
    	}
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("build", true)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityInteract(EntityInteractEvent event){
    	Block block = event.getBlock();
    	Material mat = block.getType();
    	Entity entity = event.getEntity();    	
    	FlagPermissions perms = Residence.getPermsByLoc(block.getLocation());
    	boolean hastrample = perms.has("trample", perms.has("hasbuild", true));     			
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
        if(ent instanceof Snowman || ent instanceof IronGolem || ent instanceof Ocelot || ent instanceof Pig || ent instanceof Sheep || ent instanceof Chicken || ent instanceof Wolf || ent instanceof Cow || ent instanceof Squid || ent instanceof Villager){
        	if(!perms.has("animals", true)){
        		event.setCancelled(true);
        	}
        } else {
        	if (!perms.has("monsters", true)) {
        		event.setCancelled(true);
        	}
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPaintingPlace(PaintingPlaceEvent event) {
        Player player = event.getPlayer();
        if(Residence.isResAdminOn(player)){
            return;
        }
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        String pname = player.getName();
        String world = event.getBlock().getWorld().getName();
        boolean hasplace = perms.playerHas(pname, world, "place", perms.playerHas(pname, world, "build", true));
        if (!hasplace) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPaintingBreak(PaintingBreakEvent event) {
	if(event instanceof PaintingBreakByEntityEvent){
		PaintingBreakByEntityEvent evt = (PaintingBreakByEntityEvent) event;
		if(evt.getRemover() instanceof Player){
			Player player = (Player) evt.getRemover();
			if(Residence.isResAdminOn(player)){
                    		return;
               		}
			String pname = player.getName();
		        FlagPermissions perms = Residence.getPermsByLoc(event.getPainting().getLocation());
		        String world = event.getPainting().getWorld().getName();
			boolean hasplace = perms.playerHas(pname, world, "place", perms.playerHas(pname, world, "build", true));
			if (!hasplace){
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			}
		}
	}
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombust(EntityCombustEvent event) {
        if(event.isCancelled())
            return;
        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        if (!perms.has("burn", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if(event.isCancelled())
            return;
        EntityType entity = event.getEntityType();
        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        if (entity == EntityType.CREEPER) {
            if (!perms.has("creeper", perms.has("explode", true))) {
            	event.setCancelled(true);
            	event.getEntity().remove();
            }
        }
        if (entity == EntityType.PRIMED_TNT) {
        	if (!perms.has("tnt", perms.has("explode", true))) {
        		event.setCancelled(true);
        		event.getEntity().remove();
        	}
        }
        if (entity == EntityType.FIREBALL) {
        	if(!perms.has("fireball", perms.has("explode", true))){
        		event.setCancelled(true);
        		event.getEntity().remove();
        	}
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.isCancelled()||event.getEntity()==null)
            return;
        Boolean cancel = false;
        EntityType entity = event.getEntityType();
        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        if (entity == EntityType.CREEPER) {
            if (!perms.has("creeper", perms.has("explode", true))) {
            	cancel = true;
            }
        }
        if (entity == EntityType.PRIMED_TNT) {
        	if (!perms.has("tnt", perms.has("explode", true))) {
        		cancel = true;
        	}
        }
        if (entity == EntityType.FIREBALL) {
        	if(!perms.has("fireball", perms.has("explode", true))){
        		event.setCancelled(true);
        		event.getEntity().remove();
        	}
        }
        if(cancel){
        	event.setCancelled(true);
        	event.getEntity().remove();
        } else {
	        for(Block block: event.blockList()){
	        	FlagPermissions blockperms = Residence.getPermsByLoc(block.getLocation());
	        	if((!blockperms.has("fireball", perms.has("explode", true))&&entity==EntityType.FIREBALL)||(!blockperms.has("tnt", perms.has("explode", true))&&entity==EntityType.PRIMED_TNT)||(!blockperms.has("creeper", perms.has("explode", true))&&entity==EntityType.CREEPER)){
	        		if(block!=null){
	        			ItemStack[] inventory = null;
	        			BlockState save = block.getState();
	        			if(block.getType()==Material.CHEST){
	        				Chest chest = (Chest)save;
	        				inventory = chest.getInventory().getContents();
	        				chest.getInventory().clear();
	        			}
	        			if(block.getType()==Material.FURNACE||block.getType()==Material.BURNING_FURNACE){
	        				Furnace furnace = (Furnace)save;
	        				inventory = furnace.getInventory().getContents();
	        				furnace.getInventory().clear();
	        			}
	        			if(block.getType()==Material.BREWING_STAND){
	        				BrewingStand brew = (BrewingStand)save;
	        				inventory = brew.getInventory().getContents();
	        				brew.getInventory().clear();
	        			}
	        			if(block.getType()==Material.DISPENSER){
	        				Dispenser dispenser = (Dispenser)save;
	        				inventory = dispenser.getInventory().getContents();
	        				dispenser.getInventory().clear();
	        			}
	        			if(block.getType()==Material.JUKEBOX){
	        				Jukebox jukebox = (Jukebox)save;
	        				if(jukebox.isPlaying()){
	        					inventory = new ItemStack[1];
	        					inventory[0] = new ItemStack(jukebox.getPlaying());
	        					jukebox.setPlaying(null);
	        				}
	        			}
	        			ExplodeRestore.put(save, inventory);
	        			block.setType(Material.AIR);
	        		}
	        	}
	        }
        	Residence.getServ().getScheduler().scheduleSyncDelayedTask(Residence.getServ().getPluginManager().getPlugin("Residence"), new Runnable() {
        		   public void run() {
        		       for(BlockState block: ExplodeRestore.keySet().toArray(new BlockState[0])){
        		    	   ItemStack[] inventory = ExplodeRestore.get(block);
        		    	   block.update(true);
        		    	   if(inventory!=null){
        		    		   if(block.getType()==Material.CHEST)
        		    			   ((Chest)block.getLocation().getBlock().getState()).getInventory().setContents(inventory);
        		    		   if(block.getType()==Material.FURNACE||block.getType()==Material.BURNING_FURNACE)
        		    			   ((Furnace)block.getLocation().getBlock().getState()).getInventory().setContents(inventory);
        		    		   if(block.getType()==Material.BREWING_STAND)
        		    			   ((BrewingStand)block.getLocation().getBlock().getState()).getInventory().setContents(inventory);
        		    		   if(block.getType()==Material.DISPENSER)
        		    			   ((Dispenser)block.getLocation().getBlock().getState()).getInventory().setContents(inventory);
        		    		   if(block.getType()==Material.JUKEBOX)
        		    			   ((Jukebox)block.getLocation().getBlock().getState()).setPlaying(inventory[0].getType());
        		    	   }
        		       }
        		       ExplodeRestore.clear();
        		   }
        	}, 1L);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSplashPotion(PotionSplashEvent event) {
    	if(event.isCancelled())
    		return;
    	Entity ent = event.getEntity();
    	boolean srcpvp = Residence.getPermsByLoc(ent.getLocation()).has("pvp", true);
    	Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
    	while(it.hasNext()){
    		LivingEntity target = it.next();
    		if(target.getType()==EntityType.PLAYER){
    			Boolean tgtpvp = Residence.getPermsByLoc(target.getLocation()).has("pvp", true);
    			if(!srcpvp||!tgtpvp){
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
}

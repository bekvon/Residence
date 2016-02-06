/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Administrator
 */
public class ResidenceEntityListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanChangeBlock(EntityChangeBlockEvent event) {
	if (event.getEntityType() != EntityType.ENDERMAN)
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has("build", true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWitherChangeBlock(EntityChangeBlockEvent event) {
	if (event.getEntityType() != EntityType.WITHER)
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	FlagPermissions world = Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName());
	if (!perms.has("wither", perms.has("explode", world.has("wither", world.has("explode", true))))) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
	Block block = event.getBlock();
	Material mat = block.getType();
	Entity entity = event.getEntity();
	FlagPermissions perms = Residence.getPermsByLoc(block.getLocation());
	boolean hastrample = perms.has("trample", perms.has("hasbuild", true));
	if (!hastrample && !(entity.getType() == EntityType.FALLING_BLOCK) && (mat == Material.SOIL || mat == Material.SOUL_SAND)) {
	    event.setCancelled(true);
	}
    }

    public static boolean isMonster(Entity ent) {
	return (ent instanceof Monster || ent instanceof Slime || ent instanceof Ghast);
    }

    private boolean isTamed(Entity ent) {
	return (ent instanceof Tameable ? ((Tameable) ent).isTamed() : false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(EntityDamageByEntityEvent event) {

	Entity entity = event.getEntity();
	if (!Residence.getNms().isAnimal(entity))
	    return;

	Entity damager = event.getDamager();

	if (!(damager instanceof Arrow) && !(damager instanceof Player))
	    return;

	if (damager instanceof Arrow && !(((Arrow) damager).getShooter() instanceof Player))
	    return;

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Arrow) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (Residence.isResAdminOn(cause))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (!res.getPermissions().playerHas(cause.getName(), "animalkilling", true)) {
	    cause.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "AnimalKilling|" + res.getName()));
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnEntityDeath(EntityDeathEvent event) {
	if (event.getEntity() instanceof Player)
	    return;
	Location loc = event.getEntity().getLocation();
	FlagPermissions perms = Residence.getPermsByLoc(loc);
	if (!perms.has("mobitemdrop", true)) {
	    event.getDrops().clear();
	}
	if (!perms.has("mobexpdrop", true)) {
	    event.setDroppedExp(0);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(VehicleDestroyEvent event) {

	Vehicle vehicle = event.getVehicle();

	Entity damager = event.getAttacker();

	if (damager instanceof Projectile && !(((Projectile) damager).getShooter() instanceof Player) || !(damager instanceof Player)) {
	    FlagPermissions perms = Residence.getPermsByLoc(vehicle.getLocation());
	    if (!perms.has("vehicledestroy", true)) {
		event.setCancelled(true);
		return;
	    }
	}

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else if (damager instanceof Projectile) {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (Residence.isResAdminOn(cause))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(vehicle.getLocation());

	if (res == null)
	    return;

	if (!res.getPermissions().playerHas(cause.getName(), "vehicledestroy", true)) {
	    cause.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "vehicledestroy|" + res.getName()));
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void MonsterKilling(EntityDamageByEntityEvent event) {

	Entity entity = event.getEntity();
	if (!isMonster(entity))
	    return;

	Entity damager = event.getDamager();

	if (!(damager instanceof Arrow) && !(damager instanceof Player))
	    return;

	if (damager instanceof Arrow && !(((Arrow) damager).getShooter() instanceof Player))
	    return;

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Arrow) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (Residence.isResAdminOn(cause))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (!res.getPermissions().playerHas(cause.getName(), "mobkilling", true)) {
	    cause.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "MobKilling|" + res.getName()));
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalLeash(PlayerLeashEntityEvent event) {
	Player player = event.getPlayer();

	Entity entity = event.getEntity();

	if (!Residence.getNms().isAnimal(entity) && !(player instanceof Player))
	    return;

	if (Residence.isResAdminOn(player))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (!res.getPermissions().playerHas(player.getName(), "leash", true)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "Leash|" + res.getName()));
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
	FlagPermissions perms = Residence.getPermsByLoc(event.getLocation());
	Entity ent = event.getEntity();
	if (Residence.getNms().isAnimal(ent)) {
	    if (!perms.has("animals", true)) {
		event.setCancelled(true);
		return;
	    } else
		switch (event.getSpawnReason()) {
		case BUILD_WITHER:
		    break;
		case BUILD_IRONGOLEM:
		case BUILD_SNOWMAN:
		case CUSTOM:
		case DEFAULT:
		    if (!perms.has("canimals", true)) {
			event.setCancelled(true);
			return;
		    }
		    break;
		case BREEDING:
		case CHUNK_GEN:
		case CURED:
		case DISPENSE_EGG:
		case EGG:
		case JOCKEY:
		case MOUNT:
		case VILLAGE_INVASION:
		case VILLAGE_DEFENSE:
		case NETHER_PORTAL:
		case OCELOT_BABY:
		case NATURAL:
		    if (!perms.has("nanimals", true)) {
			event.setCancelled(true);
			return;
		    }
		    break;
		case SPAWNER_EGG:
		case SPAWNER:
		    if (!perms.has("sanimals", true)) {
			event.setCancelled(true);
			return;
		    }
		    break;
		default:
		    break;
		}
	} else if (isMonster(ent))
	    if (!perms.has("monsters", true)) {
		event.setCancelled(true);
		return;
	    } else
		switch (event.getSpawnReason()) {
		case BUILD_WITHER:
		case CUSTOM:
		case DEFAULT:
		    if (!perms.has("cmonsters", true)) {
			event.setCancelled(true);
			return;
		    }
		    break;
		case CHUNK_GEN:
		case CURED:
		case DISPENSE_EGG:
		case INFECTION:
		case JOCKEY:
		case MOUNT:
		case NETHER_PORTAL:
		case SILVERFISH_BLOCK:
		case SLIME_SPLIT:
		case LIGHTNING:
		case NATURAL:
		    if (!perms.has("nmonsters", true)) {
			event.setCancelled(true);
			return;
		    }
		    break;
		case SPAWNER_EGG:
		case SPAWNER:
		    if (!perms.has("smonsters", true)) {
			event.setCancelled(true);
			return;
		    }
		    break;
		default:
		    break;
		}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player))
	    return;

	FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getEntity().getLocation(), player);
	String pname = player.getName();
	String world = player.getWorld().getName();
	if (!perms.playerHas(pname, world, "place", perms.playerHas(pname, world, "build", true))) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "build"));
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {

	if (!(event instanceof HangingBreakByEntityEvent))
	    return;

	HangingBreakByEntityEvent evt = (HangingBreakByEntityEvent) event;
	if (!(evt.getRemover() instanceof Player))
	    return;

	Player player = (Player) evt.getRemover();
	if (Residence.isResAdminOn(player))
	    return;

	String pname = player.getName();
	FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getEntity().getLocation(), player);
	String world = event.getEntity().getWorld().getName();
	if (!perms.playerHas(pname, world, "destroy", perms.playerHas(pname, world, "build", true))) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "build"));
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
	FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
	if (!perms.has("burn", true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
	EntityType entity = event.getEntityType();
	FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());

	switch (entity) {
	case CREEPER:
	    if (!perms.has("creeper", perms.has("explode", true))) {
		event.setCancelled(true);
		event.getEntity().remove();
	    }
	    break;
	case PRIMED_TNT:
	case MINECART_TNT:
	    if (!perms.has("tnt", perms.has("explode", true))) {
		event.setCancelled(true);
		event.getEntity().remove();
	    }
	    break;
	case SMALL_FIREBALL:
	case FIREBALL:
	    if (!perms.has("fireball", perms.has("explode", true))) {
		event.setCancelled(true);
		event.getEntity().remove();
	    }
	    break;
	case WITHER_SKULL:
	    if (!perms.has("witherdamage", perms.has("damage", true))) {
		event.setCancelled(true);
		event.getEntity().remove();
	    }
	    break;
	default:
	    break;
	}
    }

    @SuppressWarnings("incomplete-switch")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
	if (event.isCancelled() || event.getEntity() == null)
	    return;
	Boolean cancel = false;
	EntityType entity = event.getEntityType();
	FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
	FlagPermissions world = Residence.getWorldFlags().getPerms(event.getEntity().getWorld().getName());

	switch (entity) {
	case CREEPER:
	    if (!perms.has("creeper", perms.has("explode", true)))
		cancel = true;
	    break;
	case PRIMED_TNT:
	case MINECART_TNT:
	    if (!perms.has("tnt", perms.has("explode", true)))
		cancel = true;
	    break;
	case SMALL_FIREBALL:
	case FIREBALL:
	    if (!perms.has("fireball", perms.has("explode", true)))
		cancel = true;
	    break;
	case WITHER_SKULL:
	case WITHER:
	    if (!perms.has("wither", perms.has("explode", world.has("wither", world.has("explode", true)))))
		cancel = true;
	    break;
	}

	if (cancel) {
	    event.setCancelled(true);
	    event.getEntity().remove();
	    return;
	}

	List<Block> preserve = new ArrayList<Block>();
	for (Block block : event.blockList()) {
	    FlagPermissions blockperms = Residence.getPermsByLoc(block.getLocation());

	    switch (entity) {
	    case CREEPER:
		if (!blockperms.has("creeper", blockperms.has("explode", true)))
		    preserve.add(block);
		continue;
	    case PRIMED_TNT:
	    case MINECART_TNT:
		if (!blockperms.has("tnt", blockperms.has("explode", true)))
		    preserve.add(block);
		continue;
	    case ENDER_DRAGON:
		if (!blockperms.has("dragongrief", false))
		    preserve.add(block);
		break;
	    case ENDER_CRYSTAL:
		if (!blockperms.has("explode", true))
		    preserve.add(block);
		continue;
	    case SMALL_FIREBALL:
	    case FIREBALL:
		if (!blockperms.has("fireball", blockperms.has("explode", true)))
		    preserve.add(block);
		continue;
	    case WITHER_SKULL:
	    case WITHER:
		if (!blockperms.has("wither", blockperms.has("explode", world.has("wither", world.has("explode", true)))))
		    preserve.add(block);
		continue;
	    }
	}
	for (Block block : preserve) {
	    event.blockList().remove(block);
	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) {
	if (event.isCancelled())
	    return;

	boolean harmfull = false;
	mein: for (PotionEffect one : event.getPotion().getEffects()) {
	    for (String oneHarm : Residence.getConfigManager().getNegativePotionEffects()) {
		if (oneHarm.equalsIgnoreCase(one.getType().getName())) {
		    harmfull = true;
		    break mein;
		}
	    }
	}
	if (!harmfull)
	    return;

	Entity ent = event.getEntity();
	boolean srcpvp = Residence.getPermsByLoc(ent.getLocation()).has("pvp", true);
	Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
	while (it.hasNext()) {
	    LivingEntity target = it.next();
	    if (target.getType() != EntityType.PLAYER)
		continue;
	    Boolean tgtpvp = Residence.getPermsByLoc(target.getLocation()).has("pvp", true);
	    if (!srcpvp || !tgtpvp)
		event.setIntensity(target, 0);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

	if (event.getEntityType() != EntityType.ITEM_FRAME && !Residence.getNms().isArmorStandEntity(event.getEntityType()))
	    return;

	Entity dmgr = event.getDamager();

	Player player = null;
	if (dmgr instanceof Player) {
	    player = (Player) event.getDamager();
	} else if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
	    player = (Player) ((Projectile) dmgr).getShooter();
	} else if ((dmgr instanceof Projectile) && (!(((Projectile) dmgr).getShooter() instanceof Player))) {
	    Location loc = event.getEntity().getLocation();
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
	    if (res != null && !res.getPermissions().has("container", true)) {
		event.setCancelled(true);
		return;
	    } else
		return;
	}

	Location loc = event.getEntity().getLocation();
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
	if (res == null)
	    return;

	if (isMonster(dmgr) && !res.getPermissions().has("container", false)) {
	    event.setCancelled(true);
	    return;
	}

	if (player == null)
	    return;

	if (Residence.isResAdminOn(player))
	    return;

	if (!res.getPermissions().playerHas(player.getName(), "container", false)) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
	Entity ent = event.getEntity();
	if (ent.hasMetadata("NPC"))
	    return;

	boolean tamedAnimal = isTamed(ent);
	ClaimedResidence area = Residence.getResidenceManager().getByLoc(ent.getLocation());

	/* Living Entities */
	if (event instanceof EntityDamageByEntityEvent) {
	    EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
	    Entity damager = attackevent.getDamager();

	    if (area != null && ent instanceof Player && damager instanceof Player) {
		if (area.getPermissions().has("overridepvp", false) || Residence.getConfigManager().isOverridePvp() && area.getPermissions().has("pvp", false)) {
		    Player player = (Player) event.getEntity();
		    Damageable damage = player;
		    damage.damage(event.getDamage());
		    damage.setVelocity(damager.getLocation().getDirection());
		    event.setCancelled(true);
		    return;
		}
	    }

	    ClaimedResidence srcarea = null;
	    if (damager != null) {
		srcarea = Residence.getResidenceManager().getByLoc(damager.getLocation());
	    }
	    boolean srcpvp = true;
	    if (srcarea != null) {
		srcpvp = srcarea.getPermissions().has("pvp", true);
	    }
	    ent = attackevent.getEntity();
	    if ((ent instanceof Player || tamedAnimal) && (damager instanceof Player || (damager instanceof Projectile && (((Projectile) damager)
		.getShooter() instanceof Player))) && event.getCause() != DamageCause.FALL) {
		Player attacker = null;
		if (damager instanceof Player) {
		    attacker = (Player) damager;
		} else if (damager instanceof Projectile) {
		    attacker = (Player) ((Projectile) damager).getShooter();
		}
		if (!srcpvp) {
		    attacker.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPVPZone"));
		    event.setCancelled(true);
		    return;
		}
		/* Check for Player vs Player */
		if (area == null) {
		    /* World PvP */
		    if (!Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has("pvp", true)) {
			attacker.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("WorldPVPDisabled"));
			event.setCancelled(true);
		    }
		} else {
		    /* Normal PvP */
		    if (!area.getPermissions().has("pvp", true)) {
			attacker.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPVPZone"));
			event.setCancelled(true);
		    }
		}
		return;
	    } else if ((ent instanceof Player || tamedAnimal) && (damager instanceof Creeper)) {
		if (area == null && !Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has("creeper", true)) {
		    event.setCancelled(true);
		} else if (area != null && !area.getPermissions().has("creeper", true)) {
		    event.setCancelled(true);
		}
	    }
	}
	if (area == null) {
	    if (!Residence.getWorldFlags().getPerms(ent.getWorld().getName()).has("damage", true) && (ent instanceof Player || tamedAnimal)) {
		event.setCancelled(true);
	    }
	} else {
	    if (!area.getPermissions().has("damage", true) && (ent instanceof Player || tamedAnimal)) {
		event.setCancelled(true);
	    }
	}
	if (event.isCancelled()) {
	    /* Put out a fire on a player */
	    if ((ent instanceof Player || tamedAnimal) && (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event
		.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
		ent.setFireTicks(0);
	    }
	}
    }
}

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Debug;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
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
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.potion.PotionEffect;

public class ResidenceEntityListener implements Listener {

    Residence plugin;

    public ResidenceEntityListener(Residence plugin) {
	this.plugin = plugin;
    }

    @SuppressWarnings("incomplete-switch")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMinecartHopperItemMove(InventoryMoveItemEvent event) {
	if (!(event.getInitiator().getHolder() instanceof HopperMinecart))
	    return;
	HopperMinecart hopper = (HopperMinecart) event.getInitiator().getHolder();
	// disabling event on world
	if (Residence.isDisabledWorldListener(hopper.getWorld()))
	    return;
	Block block = hopper.getLocation().getBlock();
	switch (block.getType()) {
	case ACTIVATOR_RAIL:
	case DETECTOR_RAIL:
	case POWERED_RAIL:
	case RAILS:
	    return;
	}
	event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanChangeBlock(EntityChangeBlockEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (event.getEntityType() != EntityType.ENDERMAN)
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.destroy, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
	// disabling event on world
	Block block = event.getBlock();
	if (block == null)
	    return;
	if (Residence.isDisabledWorldListener(block.getWorld()))
	    return;
	Material mat = block.getType();
	Entity entity = event.getEntity();
	FlagPermissions perms = Residence.getPermsByLoc(block.getLocation());
	boolean hastrample = perms.has(Flags.trample, perms.has(Flags.build, true));
	if (!hastrample && !(entity.getType() == EntityType.FALLING_BLOCK) && (mat == Material.SOIL || mat == Material.SOUL_SAND)) {
	    event.setCancelled(true);
	}
    }

    public static boolean isMonster(Entity ent) {
	return (ent instanceof Monster || ent instanceof Slime || ent instanceof Ghast);
    }

    private static boolean isTamed(Entity ent) {
	return (ent instanceof Tameable ? ((Tameable) ent).isTamed() : false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(EntityDamageByEntityEvent event) {
	// disabling event on world
	Entity entity = event.getEntity();
	if (entity == null)
	    return;
	if (Residence.isDisabledWorldListener(entity.getWorld()))
	    return;
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

	if (!res.getPermissions().playerHas(cause.getName(), Flags.animalkilling, true)) {
	    Residence.msg(cause, lm.Residence_FlagDeny, Flags.animalkilling.getName(), res.getName());
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKillingByFlame(EntityCombustByEntityEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Entity entity = event.getEntity();
	if (entity == null)
	    return;
	if (!Residence.getNms().isAnimal(entity))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	Entity damager = event.getCombuster();

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

	if (!res.getPermissions().playerHas(cause.getName(), Flags.animalkilling, true)) {
	    Residence.msg(cause, lm.Residence_FlagDeny, Flags.animalkilling.getName(), res.getName());
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalDamageByMobs(EntityDamageByEntityEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Entity entity = event.getEntity();
	if (entity == null)
	    return;
	if (!Residence.getNms().isAnimal(entity))
	    return;

	Entity damager = event.getDamager();

	if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player || damager instanceof Player)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(entity.getLocation());
	FlagPermissions world = Residence.getWorldFlags().getPerms(entity.getWorld().getName());
	if (!perms.has(Flags.animalkilling, world.has(Flags.animalkilling, true))) {
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnEntityDeath(EntityDeathEvent event) {
	// disabling event on world
	LivingEntity ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;
	if (ent instanceof Player)
	    return;
	Location loc = ent.getLocation();
	FlagPermissions perms = Residence.getPermsByLoc(loc);
	if (!perms.has(Flags.mobitemdrop, true)) {
	    event.getDrops().clear();
	}
	if (!perms.has(Flags.mobexpdrop, true)) {
	    event.setDroppedExp(0);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void VehicleDestroy(VehicleDestroyEvent event) {
	// disabling event on world
	Entity damager = event.getAttacker();
	if (damager == null)
	    return;

	if (Residence.isDisabledWorldListener(damager.getWorld()))
	    return;

	Vehicle vehicle = event.getVehicle();

	if (vehicle == null)
	    return;

	if (damager instanceof Projectile && !(((Projectile) damager).getShooter() instanceof Player) || !(damager instanceof Player)) {
	    FlagPermissions perms = Residence.getPermsByLoc(vehicle.getLocation());
	    if (!perms.has(Flags.vehicledestroy, true)) {
		event.setCancelled(true);
		return;
	    }
	}

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (Residence.isResAdminOn(cause))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(vehicle.getLocation());

	if (res == null)
	    return;

	if (!res.getPermissions().playerHas(cause.getName(), Flags.vehicledestroy, true)) {
	    Residence.msg(cause, lm.Residence_FlagDeny, Flags.vehicledestroy.getName(), res.getName());
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void MonsterKilling(EntityDamageByEntityEvent event) {
	// disabling event on world
	Entity entity = event.getEntity();
	if (entity == null)
	    return;
	if (Residence.isDisabledWorldListener(entity.getWorld()))
	    return;
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

	if (!res.getPermissions().playerHas(cause.getName(), Flags.mobkilling, true)) {
	    Residence.msg(cause, lm.Residence_FlagDeny, Flags.mobkilling.getName(), res.getName());
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalLeash(PlayerLeashEntityEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	Player player = event.getPlayer();

	Entity entity = event.getEntity();

	if (!Residence.getNms().isAnimal(entity))
	    return;

	if (Residence.isResAdminOn(player))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (!res.getPermissions().playerHas(player.getName(), Flags.leash, true)) {
	    Residence.msg(player, lm.Residence_FlagDeny, Flags.leash, res.getName());
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWitherSpawn(CreatureSpawnEvent event) {
	// disabling event on world
	Entity ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;

	if (ent.getType() != EntityType.WITHER)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(event.getLocation());
	if (perms.has(Flags.witherspawn, FlagCombo.OnlyFalse)) {
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
	// disabling event on world
	Entity ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getLocation());

	if (Residence.getNms().isAnimal(ent)) {
	    if (!perms.has(Flags.animals, true)) {
		event.setCancelled(true);
		return;
	    }
	    switch (event.getSpawnReason()) {
	    case BUILD_WITHER:
		break;
	    case BUILD_IRONGOLEM:
	    case BUILD_SNOWMAN:
	    case CUSTOM:
	    case DEFAULT:
		if (!perms.has(Flags.canimals, true)) {
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
		if (!perms.has(Flags.nanimals, true)) {
		    event.setCancelled(true);
		    return;
		}
		break;
	    case SPAWNER_EGG:
	    case SPAWNER:
		if (!perms.has(Flags.sanimals, true)) {
		    event.setCancelled(true);
		    return;
		}
		break;
	    default:
		break;
	    }
	} else if (isMonster(ent)) {
	    if (!perms.has(Flags.monsters, true)) {
		event.setCancelled(true);
		return;
	    }
	    switch (event.getSpawnReason()) {
	    case BUILD_WITHER:
	    case CUSTOM:
	    case DEFAULT:
		if (!perms.has(Flags.cmonsters, true)) {
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
		if (!perms.has(Flags.nmonsters, true)) {
		    event.setCancelled(true);
		    return;
		}
		break;
	    case SPAWNER_EGG:
	    case SPAWNER:
		if (!perms.has(Flags.smonsters, true)) {
		    event.setCancelled(true);
		    return;
		}
		break;
	    default:
		break;
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {

	// disabling event on world
	Player player = event.getPlayer();
	if (player == null)
	    return;
	if (Residence.isDisabledWorldListener(player.getWorld()))
	    return;
	if (Residence.isResAdminOn(player))
	    return;

	FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getEntity().getLocation(), player);
	String pname = player.getName();
	String world = player.getWorld().getName();
	if (!perms.playerHas(pname, world, Flags.place, perms.playerHas(pname, world, Flags.build, true))) {
	    event.setCancelled(true);
	    Residence.msg(player, lm.Flag_Deny, Flags.place);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
	// disabling event on world
	Hanging ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;

	if (!(event.getRemover() instanceof Player))
	    return;

	Player player = (Player) event.getRemover();
	if (Residence.isResAdminOn(player))
	    return;

	if (Residence.getResidenceManager().isOwnerOfLocation(player, ent.getLocation()))
	    return;

	String pname = player.getName();
	FlagPermissions perms = Residence.getPermsByLocForPlayer(ent.getLocation(), player);
	String world = ent.getWorld().getName();
	if (!perms.playerHas(pname, world, Flags.destroy, perms.playerHas(pname, world, Flags.build, true))) {
	    event.setCancelled(true);
	    Residence.msg(player, lm.Flag_Deny, Flags.destroy);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
	// disabling event on world
	Hanging ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;

	if (event.getRemover() instanceof Player)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(ent.getLocation());
	if (!perms.has(Flags.destroy, perms.has(Flags.build, true))) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
	// disabling event on world
	Entity ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(ent.getLocation());
	if (!perms.has(Flags.burn, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
	// disabling event on world
	Entity ent = event.getEntity();
	if (ent == null)
	    return;
	if (Residence.isDisabledWorldListener(ent.getWorld()))
	    return;
	EntityType entity = event.getEntityType();
	FlagPermissions perms = Residence.getPermsByLoc(ent.getLocation());

	switch (entity) {
	case CREEPER:
	    if (!perms.has(Flags.creeper, perms.has(Flags.explode, true))) {
		if (Residence.getConfigManager().isCreeperExplodeBelow()) {
		    if (ent.getLocation().getBlockY() >= Residence.getConfigManager().getCreeperExplodeBelowLevel()) {
			event.setCancelled(true);
			ent.remove();
		    } else {
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(ent.getLocation());
			if (res != null) {
			    event.setCancelled(true);
			    ent.remove();
			}
		    }
		} else {
		    event.setCancelled(true);
		    ent.remove();
		}
	    }
	    break;
	case PRIMED_TNT:
	case MINECART_TNT:
	    if (!perms.has(Flags.tnt, perms.has(Flags.explode, true))) {
		if (Residence.getConfigManager().isTNTExplodeBelow()) {
		    if (ent.getLocation().getBlockY() >= Residence.getConfigManager().getTNTExplodeBelowLevel()) {
			event.setCancelled(true);
			ent.remove();
		    } else {
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(ent.getLocation());
			if (res != null) {
			    event.setCancelled(true);
			    ent.remove();
			}
		    }
		} else {
		    event.setCancelled(true);
		    ent.remove();
		}
	    }
	    break;
	case SMALL_FIREBALL:
	case FIREBALL:
	    if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.fireball, FlagCombo.OnlyFalse)) {
		event.setCancelled(true);
		ent.remove();
	    }
	    break;
	default:
	    if (!perms.has(Flags.destroy, FlagCombo.OnlyFalse)) {
		if (entity != EntityType.ENDER_CRYSTAL) {
		    event.setCancelled(true);
		    ent.remove();
		}
	    }
	    break;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
	// disabling event on world

	Location loc = event.getLocation();
	if (Residence.isDisabledWorldListener(loc.getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Entity ent = event.getEntity();

	Boolean cancel = false;
	FlagPermissions perms = Residence.getPermsByLoc(loc);
	FlagPermissions world = Residence.getWorldFlags().getPerms(loc.getWorld().getName());

	if (ent != null) {
	    switch (event.getEntityType()) {
	    case CREEPER:
		if (!perms.has(Flags.creeper, perms.has(Flags.explode, true)))
		    if (Residence.getConfigManager().isCreeperExplodeBelow()) {
			if (loc.getBlockY() >= Residence.getConfigManager().getCreeperExplodeBelowLevel())
			    cancel = true;
			else {
			    ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
			    if (res != null)
				cancel = true;
			}
		    } else
			cancel = true;
		break;
	    case PRIMED_TNT:
	    case MINECART_TNT:
		if (!perms.has(Flags.tnt, perms.has(Flags.explode, true))) {
		    if (Residence.getConfigManager().isTNTExplodeBelow()) {
			if (loc.getBlockY() >= Residence.getConfigManager().getTNTExplodeBelowLevel())
			    cancel = true;
			else {
			    ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
			    if (res != null)
				cancel = true;
			}
		    } else
			cancel = true;
		}
		break;
	    case SMALL_FIREBALL:
	    case FIREBALL:
		if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.fireball, FlagCombo.OnlyFalse))
		    cancel = true;
		break;
	    default:
		if (!perms.has(Flags.destroy, world.has(Flags.destroy, true)))
		    cancel = true;
		break;
	    }
	} else if (!perms.has(Flags.destroy, world.has(Flags.destroy, true))) {
	    cancel = true;
	}

	if (cancel) {
	    event.setCancelled(true);
	    if (ent != null)
		ent.remove();
	    return;
	}

	List<Block> preserve = new ArrayList<Block>();
	for (Block block : event.blockList()) {
	    FlagPermissions blockperms = Residence.getPermsByLoc(block.getLocation());

	    if (ent != null) {
		switch (event.getEntityType()) {
		case CREEPER:
		    if (!blockperms.has(Flags.creeper, blockperms.has(Flags.explode, true)))
			if (Residence.getConfigManager().isCreeperExplodeBelow()) {
			    if (block.getY() >= Residence.getConfigManager().getCreeperExplodeBelowLevel())
				preserve.add(block);
			    else {
				ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
				if (res != null)
				    preserve.add(block);
			    }
			} else
			    preserve.add(block);
		    continue;
		case PRIMED_TNT:
		case MINECART_TNT:
		    if (!blockperms.has(Flags.tnt, blockperms.has(Flags.explode, true))) {
			if (Residence.getConfigManager().isTNTExplodeBelow()) {
			    if (block.getY() >= Residence.getConfigManager().getTNTExplodeBelowLevel())
				preserve.add(block);
			    else {
				ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
				if (res != null)
				    preserve.add(block);
			    }
			} else
			    preserve.add(block);
		    }
		    continue;
		case ENDER_DRAGON:
		    if (!blockperms.has(Flags.dragongrief, false))
			preserve.add(block);
		    break;
		case ENDER_CRYSTAL:
		    if (blockperms.has(Flags.explode, FlagCombo.OnlyFalse))
			preserve.add(block);
		    continue;
		case SMALL_FIREBALL:
		case FIREBALL:
		    if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.fireball, FlagCombo.OnlyFalse))
			preserve.add(block);
		    continue;
		default:
		    if (!blockperms.has(Flags.destroy, world.has(Flags.destroy, true)))
			preserve.add(block);
		    continue;
		}
	    } else {
		if (!blockperms.has(Flags.destroy, world.has(Flags.destroy, true))) {
		    preserve.add(block);
		}
	    }
	}

	for (Block block : preserve) {
	    event.blockList().remove(block);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
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
	boolean srcpvp = Residence.getPermsByLoc(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
	Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
	while (it.hasNext()) {
	    LivingEntity target = it.next();
	    if (target.getType() != EntityType.PLAYER)
		continue;
	    Boolean tgtpvp = Residence.getPermsByLoc(target.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
	    if (!srcpvp || !tgtpvp)
		event.setIntensity(target, 0);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerKillingByFlame(EntityCombustByEntityEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;
	Entity entity = event.getEntity();
	if (entity == null)
	    return;
	if (!(entity instanceof Player))
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	Entity damager = event.getCombuster();

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

	Boolean srcpvp = Residence.getPermsByLoc(cause.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
	Boolean tgtpvp = Residence.getPermsByLoc(entity.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
	if (!srcpvp || !tgtpvp)
	    event.setCancelled(true);
    }

    @EventHandler
    public void OnFallDamage(EntityDamageEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;
	if (event.getCause() != DamageCause.FALL)
	    return;
	Entity ent = event.getEntity();
	if (!(ent instanceof Player))
	    return;

	if (!Residence.getPermsByLoc(ent.getLocation()).has(Flags.falldamage, FlagCombo.TrueOrNone)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler
    public void OnArmorStandFlameDamage(EntityDamageEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;
	if (event.getCause() != DamageCause.FIRE_TICK)
	    return;
	Entity ent = event.getEntity();
	if (!Residence.getNms().isArmorStandEntity(ent.getType()) && !(ent instanceof Arrow))
	    return;

	if (!Residence.getPermsByLoc(ent.getLocation()).has(Flags.destroy, true)) {
	    event.setCancelled(true);
	    ent.setFireTicks(0);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockCatchingFire(ProjectileHitEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (!(event.getEntity() instanceof Arrow))
	    return;
	Arrow arrow = (Arrow) event.getEntity();

	FlagPermissions perms = Residence.getPermsByLoc(arrow.getLocation());

	if (!perms.has(Flags.pvp, FlagCombo.TrueOrNone))
	    arrow.setFireTicks(0);
    }

    @EventHandler
    public void OnPlayerDamageByLightning(EntityDamageEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;
	if (event.getCause() != DamageCause.LIGHTNING)
	    return;
	Entity ent = event.getEntity();
	if (!(ent instanceof Player))
	    return;
	if (!Residence.getPermsByLoc(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone))
	    event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByFireballEvent(EntityDamageByEntityEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Entity dmgr = event.getDamager();
	if (dmgr.getType() != EntityType.SMALL_FIREBALL && dmgr.getType() != EntityType.FIREBALL)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
	if (perms.has(Flags.fireball, FlagCombo.OnlyFalse)) {
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	if (event.getEntityType() != EntityType.ENDER_CRYSTAL && event.getEntityType() != EntityType.ITEM_FRAME && !Residence.getNms().isArmorStandEntity(event
	    .getEntityType()))
	    return;

	Entity dmgr = event.getDamager();

	Player player = null;
	if (dmgr instanceof Player) {
	    player = (Player) event.getDamager();
	} else if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
	    player = (Player) ((Projectile) dmgr).getShooter();
	} else if ((dmgr instanceof Projectile) && (!(((Projectile) dmgr).getShooter() instanceof Player))) {
	    Location loc = event.getEntity().getLocation();
	    FlagPermissions perm = Residence.getPermsByLoc(loc);
	    if (perm.has(Flags.destroy, FlagCombo.OnlyFalse))
		event.setCancelled(true);
	    return;
	} else if (dmgr.getType() == EntityType.PRIMED_TNT || dmgr.getType() == EntityType.MINECART_TNT || dmgr.getType() == EntityType.WITHER_SKULL || dmgr
	    .getType() == EntityType.WITHER) {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
	    if (perms.has(Flags.explode, FlagCombo.OnlyFalse)) {
		event.setCancelled(true);
		return;
	    }
	}

	Location loc = event.getEntity().getLocation();
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
	if (res == null)
	    return;

	if (isMonster(dmgr) && !res.getPermissions().has(Flags.destroy, false)) {
	    event.setCancelled(true);
	    return;
	}

	if (player == null)
	    return;

	if (Residence.isResAdminOn(player))
	    return;

	FlagPermissions perms = Residence.getPermsByLocForPlayer(loc, player);

	if (event.getEntityType() == EntityType.ITEM_FRAME) {
	    ItemFrame it = (ItemFrame) event.getEntity();
	    if (it.getItem() != null) {
		if (!perms.playerHas(player, Flags.container, true)) {
		    event.setCancelled(true);
		    Residence.msg(player, lm.Flag_Deny, Flags.container);
		}
		return;
	    }
	}

	if (!perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true))) {
	    event.setCancelled(true);
	    Debug.D("this one");
	    Residence.msg(player, lm.Flag_Deny, Flags.destroy.getName());
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
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
		if (area.getPermissions().has(Flags.overridepvp, false) || Residence.getConfigManager().isOverridePvp() && area.getPermissions().has(Flags.pvp,
		    FlagCombo.OnlyFalse, false)) {
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
	    boolean allowSnowBall = false;
	    boolean isSnowBall = false;
	    boolean isOnFire = false;
	    if (srcarea != null) {
		srcpvp = srcarea.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone, false);
	    }
	    ent = attackevent.getEntity();
	    if ((ent instanceof Player || tamedAnimal) && (damager instanceof Player || (damager instanceof Projectile && (((Projectile) damager)
		.getShooter() instanceof Player))) && event.getCause() != DamageCause.FALL) {
		Player attacker = null;
		if (damager instanceof Player) {
		    attacker = (Player) damager;
		} else if (damager instanceof Projectile) {
		    Projectile project = (Projectile) damager;
		    if (project.getType() == EntityType.SNOWBALL && srcarea != null) {
			isSnowBall = true;
			allowSnowBall = srcarea.getPermissions().has(Flags.snowball, FlagCombo.TrueOrNone);
		    }
		    if (project.getFireTicks() > 0)
			isOnFire = true;

		    attacker = (Player) ((Projectile) damager).getShooter();
		}

		if (!(ent instanceof Player))
		    return;

		if (!srcpvp && !isSnowBall || !allowSnowBall && isSnowBall) {
		    if (attacker != null)
			Residence.msg(attacker, lm.General_NoPVPZone);
		    if (isOnFire)
			ent.setFireTicks(0);
		    event.setCancelled(true);
		    return;
		}

		/* Check for Player vs Player */
		if (area == null) {
		    /* World PvP */
		    if (damager != null)
			if (!Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.pvp, FlagCombo.TrueOrNone)) {
			    if (attacker != null)
				Residence.msg(attacker, lm.General_WorldPVPDisabled);
			    if (isOnFire)
				ent.setFireTicks(0);
			    event.setCancelled(true);
			    return;
			}

		    /* Attacking from safe zone */
		    if (attacker != null) {
			FlagPermissions aPerm = Residence.getPermsByLoc(attacker.getLocation());
			if (!aPerm.has(Flags.pvp, FlagCombo.TrueOrNone)) {
			    Residence.msg(attacker, lm.General_NoPVPZone);
			    if (isOnFire)
				ent.setFireTicks(0);
			    event.setCancelled(true);
			    return;
			}
		    }
		} else {
		    /* Normal PvP */
		    if (!isSnowBall && !area.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone, false) || isSnowBall && !allowSnowBall) {
			if (attacker != null)
			    Residence.msg(attacker, lm.General_NoPVPZone);
			if (isOnFire)
			    ent.setFireTicks(0);
			event.setCancelled(true);
			return;
		    }
		}
		return;
	    } else if ((ent instanceof Player || tamedAnimal) && (damager instanceof Creeper)) {
		if (area == null && !Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.creeper, true)) {
		    event.setCancelled(true);
		} else if (area != null && !area.getPermissions().has(Flags.creeper, true)) {
		    event.setCancelled(true);
		}
	    }
	}
	if (area == null) {
	    if (!Residence.getWorldFlags().getPerms(ent.getWorld().getName()).has(Flags.damage, true) && (ent instanceof Player || tamedAnimal)) {
		event.setCancelled(true);
	    }
	} else {
	    if (!area.getPermissions().has(Flags.damage, true) && (ent instanceof Player || tamedAnimal)) {
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

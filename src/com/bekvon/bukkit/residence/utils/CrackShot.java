package com.bekvon.bukkit.residence.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

public class CrackShot implements Listener {
    public CrackShot() {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(WeaponDamageEntityEvent event) {
	Entity damager = event.getDamager();

	if ((!(damager instanceof Arrow)) && (!(damager instanceof Player))) {
	    return;
	}

	Player cause = null;
	if ((damager instanceof Arrow) && (!(((Arrow) damager).getShooter() instanceof Player))) {
	    return;
	}

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Arrow) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (Residence.isResAdminOn(cause)) {
	    return;
	}

	if (!(event.getVictim() instanceof LivingEntity))
	    return;

	Entity entity = event.getVictim();
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

	if (Residence.getNms().isAnimal(entity)) {
	    if (res != null && !res.getPermissions().playerHas(cause.getName(), "animalkilling", true)) {
		cause.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "animalkilling"));
		event.setCancelled(true);
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(WeaponDamageEntityEvent event) {
	if (event.getVictim().getType() != EntityType.ITEM_FRAME && !Residence.getNms().isArmorStandEntity(event.getVictim().getType()))
	    return;

	Entity dmgr = event.getDamager();
	Player player;
	if (event.getDamager() instanceof Player) {
	    player = (Player) event.getDamager();
	} else {
	    if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
		player = (Player) ((Projectile) dmgr).getShooter();
	    } else
		return;
	}

	if (Residence.isResAdminOn(player))
	    return;

	// Note: Location of entity, not player; otherwise player could stand outside of res and still damage
	Location loc = event.getVictim().getLocation();
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
	if (res != null && !res.getPermissions().playerHas(player.getName(), "container", false)) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(WeaponDamageEntityEvent event) {

	if (!(event.getVictim() instanceof Player))
	    return;

	Player victim = (Player) event.getVictim();
	if (victim.hasMetadata("NPC")) {
	    return;
	}

	ClaimedResidence area = Residence.getResidenceManager().getByLoc(victim.getLocation());
	/* Living Entities */
	Player damager = (Player) event.getPlayer();
	ClaimedResidence srcarea = null;

	if (damager == null)
	    return;

	srcarea = Residence.getResidenceManager().getByLoc(damager.getLocation());

	boolean srcpvp = true;
	if (srcarea != null) {
	    srcpvp = srcarea.getPermissions().has("pvp", true);
	}

	if (!srcpvp) {
	    damager.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPVPZone"));
	    event.setCancelled(true);
	    return;
	}
	/* Check for Player vs Player */
	if (area == null) {
	    /* World PvP */
	    if (!Residence.getWorldFlags().getPerms(damager.getWorld().getName()).has("pvp", true)) {
		damager.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("WorldPVPDisabled"));
		event.setCancelled(true);
	    }
	} else {
	    /* Normal PvP */
	    if (!area.getPermissions().has("pvp", true)) {
		damager.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPVPZone"));
		event.setCancelled(true);
	    }
	}
	return;

    }
}

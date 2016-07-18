package com.bekvon.bukkit.residence.allNms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class v1_8Events implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtArmoStand(PlayerInteractAtEntityEvent event) {
	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();
	if (!Residence.getNms().isArmorStandEntity(ent.getType()))
	    return;

	FlagPermissions perms = Residence.getPermsByLocForPlayer(ent.getLocation(), player);
	String world = player.getWorld().getName();

	if (!perms.playerHas(player.getName(), world, Flags.container, perms.playerHas(player.getName(), world, Flags.use, true))) {
	    event.setCancelled(true);
	    Residence.msg(player, lm.Flag_Deny, Flags.container.getName());
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {

	Location loc = event.getBlock().getLocation();

	if (Residence.isDisabledWorldListener(loc.getWorld()))
	    return;
	if (event.isCancelled())
	    return;
	FlagPermissions world = Residence.getWorldFlags().getPerms(loc.getWorld().getName());
	List<Block> preserve = new ArrayList<Block>();
	for (Block block : event.blockList()) {
	    FlagPermissions blockperms = Residence.getPermsByLoc(block.getLocation());
	    if (!blockperms.has(Flags.explode, world.has(Flags.explode, true))) {
		preserve.add(block);
	    }
	}
	for (Block block : preserve) {
	    event.blockList().remove(block);
	}
    }
}

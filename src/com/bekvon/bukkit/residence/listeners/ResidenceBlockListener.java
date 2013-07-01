/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 *
 * @author Administrator
 */
public class ResidenceBlockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        Material mat = event.getBlock().getType();
        String world = event.getBlock().getWorld().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if (Residence.getItemManager().isIgnored(mat, group, world)) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (Residence.getConfigManager().enabledRentSystem()) {
            if (res != null) {
                String resname = res.getName();
                if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
        String pname = player.getName();
        if (res != null) {
            if (res.getItemIgnoreList().isListed(mat)) {
                return;
            }
        }
        boolean hasdestroy = perms.playerHas(pname, player.getWorld().getName(), "destroy", perms.playerHas(pname, player.getWorld().getName(), "build", true));
        boolean hasContainer = perms.playerHas(pname, player.getWorld().getName(), "container", true);
        if (!hasdestroy || (!hasContainer && mat == Material.CHEST)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        Material mat = event.getBlock().getType();
        String world = event.getBlock().getWorld().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if (Residence.getItemManager().isIgnored(mat, group, world)) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (Residence.getConfigManager().enabledRentSystem()) {
            if (res != null) {
                String resname = res.getName();
                if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        String pname = player.getName();
        if (res != null) {
            if (!res.getItemBlacklist().isAllowed(mat)) {
                player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
                event.setCancelled(true);
                return;
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
        boolean hasplace = perms.playerHas(pname, player.getWorld().getName(), "place", perms.playerHas(pname, player.getWorld().getName(), "build", true));
        if (!hasplace) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Location loc = event.getBlock().getLocation();
        FlagPermissions perms = Residence.getPermsByLoc(loc);
        if (!perms.has("spread", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("piston", true)){
            event.setCancelled(true);
        	return;
    	}
        if (event.isSticky()){
            Location location = event.getRetractLocation();
            FlagPermissions blockperms = Residence.getPermsByLoc(location);
            if (!blockperms.has("piston", true)) {
            	event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("piston", true)) {
            event.setCancelled(true);
        }
        for (Block block : event.getBlocks()) {
        	FlagPermissions blockpermsfrom = Residence.getPermsByLoc(block.getLocation());
        	Location blockto = block.getLocation();
        	blockto.setX(blockto.getX()+event.getDirection().getModX());
        	blockto.setY(blockto.getY()+event.getDirection().getModY());
        	blockto.setZ(blockto.getZ()+event.getDirection().getModZ());
        	FlagPermissions blockpermsto = Residence.getPermsByLoc(blockto);
            if (!blockpermsfrom.has("piston", true) || !blockpermsto.has("piston", true)) {
            	event.setCancelled(true);
            	return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getToBlock().getLocation());
        boolean hasflow = perms.has("flow", true);
        Material mat = event.getBlock().getType();
        if (!hasflow) {
            event.setCancelled(true);
            return;
        }
        if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
            if (!perms.has("lavaflow", hasflow)) {
        		event.setCancelled(true);
        	}
            return;
        }
        if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
        	if (!perms.has("waterflow", hasflow)) {
        		event.setCancelled(true);
        	}
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("firespread", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), event.getPlayer());
        IgniteCause cause = event.getCause();
        if (cause == IgniteCause.SPREAD) {
            if (!perms.has("firespread", true)) {
        		event.setCancelled(true);
        	}
        } else if (cause == IgniteCause.FLINT_AND_STEEL) {
        	Player player = event.getPlayer();
        	if (player != null && !perms.playerHas(player.getName(), player.getWorld().getName(), "ignite", true) && !Residence.isResAdminOn(player)) {
        		event.setCancelled(true);
        		player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
        	}
        } else {
        	if(!perms.has("ignite", true)){
        	    event.setCancelled(true);
        	}
        }
    }
}

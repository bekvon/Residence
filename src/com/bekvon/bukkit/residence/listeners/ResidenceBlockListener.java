/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.List;

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

import com.bekvon.bukkit.residence.NewLanguage;
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

    private static List<String> informed = new ArrayList<String>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player)) {
	    return;
	}

	Block block = event.getBlock();
	Material mat = block.getType();
	String world = block.getWorld().getName();
	String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
	if (Residence.getItemManager().isIgnored(mat, group, world)) {
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
	if (Residence.getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
		event.setCancelled(true);
		return;
	    }
	}
	FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
	String pname = player.getName();
	if (res != null && res.getItemIgnoreList().isListed(mat))
	    return;

	boolean hasdestroy = perms.playerHas(pname, player.getWorld().getName(), "destroy", perms.playerHas(pname, player.getWorld().getName(), "build", true));
	boolean hasContainer = perms.playerHas(pname, player.getWorld().getName(), "container", true);
	if (!hasdestroy || (!hasContainer && mat == Material.CHEST)) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return;
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {

	if (!Residence.getConfigManager().ShowNoobMessage())
	    return;

	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	ArrayList<String> list = Residence.getResidenceManager().getResidenceList(player.getName(), true, false);
	if (list.size() != 0)
	    return;

	if (informed.contains(player.getName()))
	    return;

	for (String one : NewLanguage.getMessageList("Language.NewPlayerInfo")) {
	    player.sendMessage(one);
	}
	informed.add(player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player)) {
	    return;
	}
	Block block = event.getBlock();
	Material mat = block.getType();
	String world = block.getWorld().getName();
	String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
	if (Residence.getItemManager().isIgnored(mat, group, world)) {
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
	if (Residence.getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
		event.setCancelled(true);
		return;
	    }
	}
	String pname = player.getName();
	if (res != null && !res.getItemBlacklist().isAllowed(mat)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
	    event.setCancelled(true);
	    return;
	}
	FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
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
	if (!perms.has("piston", true)) {
	    event.setCancelled(true);
	    return;
	}

	List<Block> blocks = event.getBlocks();

	if (event.isSticky()) {
	    for (Block oneBlock : blocks) {
		FlagPermissions blockperms = Residence.getPermsByLoc(oneBlock.getLocation());
		if (!blockperms.has("piston", true)) {
		    event.setCancelled(true);
		    return;
		}
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
	    if (!blockpermsfrom.has("piston", true)) {
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
	if (!perms.has("firespread", true))
	    event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
	IgniteCause cause = event.getCause();
	if (cause == IgniteCause.SPREAD) {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has("firespread", true))
		event.setCancelled(true);
	} else if (cause == IgniteCause.FLINT_AND_STEEL) {
	    Player player = event.getPlayer();
	    FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
	    if (player != null && !perms.playerHas(player.getName(), player.getWorld().getName(), "ignite", true) && !Residence.isResAdminOn(player)) {
		event.setCancelled(true);
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    }
	} else {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has("ignite", true)) {
		event.setCancelled(true);
	    }
	}
    }
}

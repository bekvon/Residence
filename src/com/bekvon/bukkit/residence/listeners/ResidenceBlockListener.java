/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 *
 * @author Administrator
 */
public class ResidenceBlockListener extends BlockListener {

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getBlock().getLocation());
        Player player = event.getPlayer();
        if (res != null) {
            if (!res.getPermissions().playerHas(player.getName(), "build", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                event.setCancelled(true);
                player.sendMessage("§cYou dont have permission to build here.");
            }
        } else {
            if (!Residence.getWorldFlags().getPerms(player).has("build", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                event.setCancelled(true);

                player.sendMessage("§cWorld build is disabled.");
            }
        }
        super.onBlockBreak(event);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getBlock().getLocation());
        Player player = event.getPlayer();
        if (res != null) {
            if (!res.getPermissions().playerHas(player.getName(), "build", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                event.setCancelled(true);
                player.sendMessage("§cYou dont have permission to build here.");
            }
        } else {
            if (!Residence.getWorldFlags().getPerms(player).has("build", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                event.setCancelled(true);
                player.sendMessage("§cWorld build is disabled.");
            }
        }
        super.onBlockPlace(event);
    }

    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getToBlock().getLocation());
        if (res != null) {
            if (!res.getPermissions().has("flow", true)) {
                event.setCancelled(true);
            }
        }
        //super.onBlockFromTo(event);
    }

    @Override
    public void onBlockIgnite(BlockIgniteEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getBlock().getLocation());
        if (res != null) {
            if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
                Player player = event.getPlayer();
                if (!res.getPermissions().playerHas(player.getName(), "ignite", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou dont have permission to ignite here.");
                }
            }
            else if(event.getCause() == IgniteCause.SPREAD)
            {
                if(!res.getPermissions().has("firespread", true))
                {
                    event.setCancelled(true);
                }
            }
            else
            {
                if(!res.getPermissions().has("ignite", true))
                {
                    event.setCancelled(true);
                }
            }
        } else {
            if(event.getCause() == IgniteCause.SPREAD)
            {
                if (!Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName()).has("firespread", true)) {
                    event.setCancelled(true);
                }
            }
            else
            {
                if(!Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName()).has("ignite", true))
                {
                    event.setCancelled(true);
                }
            }
        }
        super.onBlockIgnite(event);
    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getBlock().getLocation());
        if (res != null) {
            if (!res.getPermissions().has("flow", true)) {
                event.setCancelled(true);
            }
        }
        super.onBlockPhysics(event);
    }

}

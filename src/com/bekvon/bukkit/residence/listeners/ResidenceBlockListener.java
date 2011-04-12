/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

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
            if (!Residence.getConfig().worldBuildEnabled() && !Residence.getPermissionManager().isResidenceAdmin(player)) {
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
            if (!Residence.getConfig().worldBuildEnabled() && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                event.setCancelled(true);

                player.sendMessage("§cWorld build is disabled.");
            }
        }
        super.onBlockPlace(event);
    }

    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
        ClaimedResidence res = Residence.getResidenceManger().getByLoc(event.getBlock().getLocation());
        if (res != null) {
            if (!res.getPermissions().has("flow", true)) {
                event.setCancelled(true);
            }
        }
        super.onBlockFromTo(event);
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
            else
            {
                if(!res.getPermissions().has("firespread", true))
                {
                    event.setCancelled(true);
                }
            }
        } else {
            if(event.getCause() == IgniteCause.SPREAD)
            {
                if (!Residence.getConfig().worldFireSpreadEnabled()) {
                    event.setCancelled(true);
                }
            }
            else
            {
                if(!Residence.getConfig().worldIgniteEnabled())
                {
                    event.setCancelled(true);
                }
            }
        }
        super.onBlockIgnite(event);
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if(player.getItemInHand().getTypeId() == Residence.getSelectionManager().getSelectionId())
            Residence.getSelectionManager().placeLoc1(player.getName(), event.getBlock().getLocation());
        super.onBlockDamage(event);
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

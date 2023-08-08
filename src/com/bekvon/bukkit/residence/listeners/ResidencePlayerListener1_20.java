package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Items.CMIMaterial;

public class ResidencePlayerListener1_20 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_20(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        Block block = event.getClickedBlock();

        if (block == null || !CMIMaterial.isSign(block.getType()))
            return;

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;

        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        boolean hasuse = perms.playerHas(player, Flags.use, FlagCombo.TrueOrNone);

        if (hasuse)
            return;

        event.setCancelled(true);

//        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
//            if (player.getOpenInventory().getType().equals(InventoryType.CRAFTING)) {
//                player.closeInventory();
//            }
//        }, 0L);

        plugin.msg(player, lm.Flag_Deny, Flags.use);

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignModifyEvent(SignChangeEvent event) {

//        // disabling event on world
//        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
//            return;
//
//        Player player = event.getPlayer();
//        if (player.hasMetadata("NPC"))
//            return;
//
//        Block block = event.getBlock();
//
//        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
//
//        boolean hasuse = perms.playerHas(player, Flags.use, FlagCombo.TrueOrNone);
//
//        if (hasuse)
//            return;
//
//        event.setCancelled(true);
//        plugin.msg(player, lm.Flag_Deny, Flags.use);
    }
}

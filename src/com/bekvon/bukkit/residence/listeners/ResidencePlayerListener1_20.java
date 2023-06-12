package com.bekvon.bukkit.residence.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

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

        boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
        if (hasplace)
            return;

        event.setCancelled(true);
        plugin.msg(player, lm.Flag_Deny, Flags.build);

    }
}

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

public class ResidencePlayerListener1_19 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_19(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignInteract(PlayerInteractEvent event) {

        if (!Flags.goathorn.isGlobalyEnabled())
            return;

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;

        ItemStack horn = event.getItem();

        if (horn == null)
            return;

        if (!horn.getType().equals(Material.GOAT_HORN))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getPlayer().getLocation());
        if (res == null)
            return;

        if (event.getPlayer().hasMetadata("NPC"))
            return;

        if (res.getPermissions().playerHas(event.getPlayer(), Flags.goathorn, FlagCombo.TrueOrNone))
            return;

        event.setCancelled(true);

        plugin.msg(player, lm.Residence_FlagDeny, Flags.goathorn, res.getName());
    }
}

package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class SpigotListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
	Player player = event.getPlayer();
	Location loc = player.getLocation();
	FlagPermissions perms = Residence.getPermsByLoc(loc);
	if (perms.has("nodurability", false)) {
	    ItemStack held = player.getItemInHand();
	    if (held.getType() != Material.AIR) {
		held.setDurability(held.getDurability());
		player.setItemInHand(held);
		event.setDamage(0);
		event.setCancelled(true);
	    }
	}
    }
}

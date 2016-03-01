package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ResidenceFixesListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAnvilPlace(PlayerInteractEvent event) {
	if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
	    return;
	ItemStack iih = event.getItem();
	if (iih == null || iih.getType() == Material.AIR || iih.getType() != Material.ANVIL)
	    return;
	BlockFace face = event.getBlockFace();
	Block bclicked = event.getClickedBlock();
	if (bclicked == null)
	    return;
	Location loc = new Location(bclicked.getWorld(), bclicked.getX() + face.getModX(), bclicked.getY() + face.getModY(),
	    bclicked.getZ() + face.getModZ());
	Block block = loc.getBlock();
	if (block == null || block.getType() == Material.AIR || block.getType() != Material.SKULL)
	    return;
	event.setCancelled(true);
    }
}

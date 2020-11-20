package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

public class ResidencePlayerListener1_16 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_16(Residence plugin) {
	this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractRespawn(PlayerInteractEvent event) {

	if (event.getPlayer() == null)
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;

	if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
	    return;

	Player player = event.getPlayer();

	Block block = event.getClickedBlock();
	if (block == null)
	    return;

	Material mat = block.getType();

	if (!mat.equals(Material.RESPAWN_ANCHOR))
	    return;

	ItemStack iih = event.getItem();
	CMIMaterial heldItem = CMIMaterial.get(iih);

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
	if (res == null)
	    return;

	if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.anchor, FlagCombo.OnlyTrue)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.anchor, res.getName());
	    event.setCancelled(true);
	    return;
	}

	if (!heldItem.equals(CMIMaterial.GLOWSTONE) && !player.getWorld().getEnvironment().equals(Environment.NETHER) && !res.getPermissions().has(Flags.explode, FlagCombo.TrueOrNone)) {
	    event.setCancelled(true);
	    return;
	}

	RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
	if (anchor.getMaximumCharges() == anchor.getCharges() && !res.getPermissions().has(Flags.explode, FlagCombo.TrueOrNone) && !player.getWorld().getEnvironment().equals(Environment.NETHER)) {
	    event.setCancelled(true);
	}

    }
}

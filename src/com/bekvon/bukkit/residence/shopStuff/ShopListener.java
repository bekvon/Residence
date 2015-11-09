package com.bekvon.bukkit.residence.shopStuff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class ShopListener implements Listener {

    public static List<String> Delete = new ArrayList<String>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {

	if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
	    return;

	Block block = event.getClickedBlock();

	if (block == null)
	    return;

	if (!(block.getState() instanceof Sign))
	    return;

	Player player = (Player) event.getPlayer();

	Location loc = block.getLocation();

	Board Found = null;

	String resName = null;
	for (Board one : ShopSignUtil.GetAllBoards()) {
	    resName = one.getResNameByLoc(loc);
	    if (resName != null) {
		Found = one;
		break;
	    }
	}

	if (Delete.contains(player.getName())) {
	    if (resName != null) {
		ShopSignUtil.GetAllBoards().remove(Found);
		ShopSignUtil.saveSigns();
		event.getPlayer().sendMessage("Sign board removed");
	    } else {
		event.getPlayer().sendMessage("This is not sign board, try performing command again and clicking correct block");
	    }
	    Delete.remove(player.getName());
	    return;
	}

	if (resName != null)
	    Bukkit.dispatchCommand(event.getPlayer(), "res tp " + resName);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeShop(ResidenceFlagChangeEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getFlag().equalsIgnoreCase("shop"))
	    return;

	switch (event.getNewState()) {
	case NEITHER:
	case FALSE:
	    Residence.getResidenceManager().removeShop(event.getResidence());
	    ShopSignUtil.BoardUpdate();
	    ShopSignUtil.saveSigns();
	    break;
	case INVALID:
	    break;
	case TRUE:
	    Residence.getResidenceManager().addShop(event.getResidence());
	    event.getResidence().getPermissions().setFlag("tp", FlagState.TRUE);
	    event.getResidence().getPermissions().setFlag("move", FlagState.TRUE);
	    event.getResidence().getPermissions().setFlag("pvp", FlagState.FALSE);
	    ShopSignUtil.BoardUpdate();
	    ShopSignUtil.saveSigns();
	    break;
	default:
	    break;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFlagChange(ResidenceFlagChangeEvent event) {
	if (event.isCancelled())
	    return;

	if (event.getFlag().equalsIgnoreCase("tp") && event.getNewState() == FlagState.TRUE)
	    return;

	if (event.getFlag().equalsIgnoreCase("move") && event.getNewState() == FlagState.TRUE)
	    return;

	if (event.getFlag().equalsIgnoreCase("pvp") && event.getNewState() == FlagState.FALSE)
	    return;

	if (!event.getFlag().equalsIgnoreCase("move") && !event.getFlag().equalsIgnoreCase("tp") && !event.getFlag().equalsIgnoreCase("pvp"))
	    return;

	if (!event.getResidence().getPermissions().has("shop", false))
	    return;

	event.setCancelled(true);

	if (event.getPlayer() != null)
	    event.getPlayer().sendMessage(ChatColor.YELLOW + "Can't change while shop flag is set to true");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceCreate(ResidenceCreationEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getResidence().getPermissions().has("shop", false))
	    return;

	Residence.getResidenceManager().addShop(event.getResidence());

	ShopSignUtil.BoardUpdate();
	ShopSignUtil.saveSigns();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRemove(ResidenceDeleteEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getResidence().getPermissions().has("shop", true))
	    return;

	Residence.getResidenceManager().removeShop(event.getResidence());
	ShopSignUtil.BoardUpdate();
	ShopSignUtil.saveSigns();
    }
}

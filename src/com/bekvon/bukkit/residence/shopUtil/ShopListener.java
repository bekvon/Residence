package com.bekvon.bukkit.residence.shopUtil;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class ShopListener implements Listener {

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
	    break;
	case INVALID:
	    break;
	case TRUE:
	    Residence.getResidenceManager().addShop(event.getResidence());
	    event.getResidence().getPermissions().setFlag("tp", FlagState.TRUE);
	    event.getResidence().getPermissions().setFlag("move", FlagState.TRUE);
	    event.getResidence().getPermissions().setFlag("pvp", FlagState.FALSE);
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
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRemove(ResidenceDeleteEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getResidence().getPermissions().has("shop", true))
	    return;

	Residence.getResidenceManager().removeShop(event.getResidence());
    }
}

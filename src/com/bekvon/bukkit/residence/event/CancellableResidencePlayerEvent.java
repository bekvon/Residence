package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class CancellableResidencePlayerEvent extends ResidencePlayerEvent implements Cancellable {

    protected boolean cancelled;

    public CancellableResidencePlayerEvent(String eventName, ClaimedResidence resref, Player player) {
	super(eventName, resref, player);
	cancelled = false;
    }

    public boolean isCancelled() {
	return cancelled;
    }

    public void setCancelled(boolean bln) {
	cancelled = bln;
    }

}

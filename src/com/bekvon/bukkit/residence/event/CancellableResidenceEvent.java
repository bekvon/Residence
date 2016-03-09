package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.Cancellable;

public class CancellableResidenceEvent extends ResidenceEvent implements Cancellable {

    protected boolean cancelled;

    public CancellableResidenceEvent(String eventName, ClaimedResidence resref) {
	super(eventName, resref);
    }

    public boolean isCancelled() {
	return cancelled;
    }

    public void setCancelled(boolean bln) {
	cancelled = bln;
    }

}

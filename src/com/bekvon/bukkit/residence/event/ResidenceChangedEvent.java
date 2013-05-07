package com.bekvon.bukkit.residence.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceChangedEvent extends ResidencePlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    
    private ClaimedResidence from = null;
    private ClaimedResidence to = null;

    public ResidenceChangedEvent(ClaimedResidence from, ClaimedResidence to, Player player) {
        super("RESIDENCE_CHANGE", null, player);
        this.from = from;
        this.to = to;
    }
    
    public ClaimedResidence getFrom() {
    	return from;
    }
    
    public ClaimedResidence getTo() {
    	return to;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

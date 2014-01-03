package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class ResidenceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    ClaimedResidence res;

    public ResidenceEvent(ClaimedResidence resref) {
        res = resref;
    }

    public ClaimedResidence getResidence() {
        return res;
    }
}

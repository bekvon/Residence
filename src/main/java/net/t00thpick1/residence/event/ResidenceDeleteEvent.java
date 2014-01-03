package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.event.HandlerList;

public class ResidenceDeleteEvent extends ResidenceEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum DeleteCause {
        PLAYER_DELETE, OTHER
    }

    DeleteCause cause;

    public ResidenceDeleteEvent(ClaimedResidence resref, DeleteCause delcause) {
        super(resref);
        cause = delcause;
    }

    public DeleteCause getCause() {
        return cause;
    }

}

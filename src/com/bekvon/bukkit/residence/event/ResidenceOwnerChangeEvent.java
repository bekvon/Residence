package com.bekvon.bukkit.residence.event;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.HandlerList;

public class ResidenceOwnerChangeEvent extends ResidenceEvent {

    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected String newowner;

    public ResidenceOwnerChangeEvent(ClaimedResidence resref, String newOwner)
    {
        super("RESIDENCE_OWNER_CHANGE",resref);
        newowner = newOwner;
    }

    public String getNewOwner()
    {
        return newowner;
    }
}

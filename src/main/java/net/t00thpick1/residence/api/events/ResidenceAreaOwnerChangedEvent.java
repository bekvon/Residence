package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidenceAreaOwnerChangedEvent extends Event {
    private final ResidenceArea residence;
    private final String previousOwner;

    public ResidenceAreaOwnerChangedEvent(ResidenceArea residence, String previousOwner) {
        this.residence = residence;
        this.previousOwner = previousOwner;
    }

    /**
     * Gets the ResidenceArea involved.
     *
     * @return the ResidenceArea involved
     */
    public ResidenceArea getResidenceArea() {
        return residence;
    }

    /**
     * Gets the previous owner of the ResidenceArea
     *
     * @return the previous owner
     */
    public String getPreviousOwner() {
        return previousOwner;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    private HandlerList handlerlist = new HandlerList();
}

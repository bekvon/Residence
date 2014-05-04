package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidenceAreaEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ResidenceArea residence;

    public ResidenceAreaEvent(ResidenceArea residence) {
        this.residence = residence;
    }

    /**
     * Gets the ResidenceArea involved in this event.
     *
     * @return the ResidenceArea involved
     */
    public ResidenceArea getResidenceArea() {
        return residence;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

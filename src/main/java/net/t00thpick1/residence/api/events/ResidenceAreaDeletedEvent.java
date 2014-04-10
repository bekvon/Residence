package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event should be called any time a ResidenceArea is deleted.
 * 
 * @author t00thpick1
 */
public class ResidenceAreaDeletedEvent extends Event {
    private final ResidenceArea residence;

    public ResidenceAreaDeletedEvent(ResidenceArea residence) {
        this.residence = residence;
    }

    /**
     * Gets the ResidenceArea being deleted.
     *
     * @return the ResidenceArea involved
     */
    public ResidenceArea getResidenceArea() {
        return residence;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    private HandlerList handlerlist = new HandlerList();
}

package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event should be called any time a ResidenceArea is renamed.
 * 
 * @author t00thpick1
 */
public class ResidenceRenamedEvent extends Event {
    private ResidenceArea residence;

    public ResidenceRenamedEvent(ResidenceArea residence) {
        this.residence = residence;
    }

    /**
     * Gets the ResidenceArea being renamed.
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

package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever there has been edits made to the flags of a Residence.
 *
 * @author t00thpick1
 */
public class ResidenceAreaFlagsChangedEvent extends Event {
    private final ResidenceArea residence;

    public ResidenceAreaFlagsChangedEvent(ResidenceArea residence) {
        this.residence = residence;
    }

    /**
     * Gets the ResidenceArea in which flags were changed.
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

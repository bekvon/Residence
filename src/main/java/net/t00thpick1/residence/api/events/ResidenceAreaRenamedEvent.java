package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event should be called any time a ResidenceArea is renamed.
 * 
 * @author t00thpick1
 */
public class ResidenceAreaRenamedEvent extends Event {
    private final ResidenceArea residence;
    private final String oldName;

    public ResidenceAreaRenamedEvent(ResidenceArea residence, String oldName) {
        this.residence = residence;
        this.oldName = oldName;
    }

    /**
     * Gets the ResidenceArea being renamed.
     *
     * @return the ResidenceArea involved
     */
    public ResidenceArea getResidenceArea() {
        return residence;
    }

    /**
     * Gets the previous name of the ResidenceArea
     *
     * @return the previous name
     */
    public String getOldName() {
        return oldName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    private HandlerList handlerlist = new HandlerList();
}

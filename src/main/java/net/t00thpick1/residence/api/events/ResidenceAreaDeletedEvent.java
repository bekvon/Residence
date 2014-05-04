package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

/**
 * This event should be called any time a ResidenceArea is deleted.
 *
 * @author t00thpick1
 */
public class ResidenceAreaDeletedEvent extends ResidenceAreaEvent {
    public ResidenceAreaDeletedEvent(ResidenceArea residence) {
        super(residence);
    }
}

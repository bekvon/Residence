package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

/**
 * This event should be called any time a ResidenceArea is created.
 *
 * @author t00thpick1
 */
public class ResidenceAreaCreatedEvent extends ResidenceAreaEvent {
    public ResidenceAreaCreatedEvent(ResidenceArea residence) {
        super(residence);
    }
}

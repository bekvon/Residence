package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

/**
 * This event is called whenever there has been edits made to the flags of a Residence.
 *
 * @author t00thpick1
 */
public class ResidenceAreaFlagsChangedEvent extends ResidenceAreaEvent {
    public ResidenceAreaFlagsChangedEvent(ResidenceArea residence) {
        super(residence);
    }
}

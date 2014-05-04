package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

/**
 * This event should be called any time a ResidenceArea is renamed.
 *
 * @author t00thpick1
 */
public class ResidenceAreaRenamedEvent extends ResidenceAreaEvent {
    private final String oldName;

    public ResidenceAreaRenamedEvent(ResidenceArea residence, String oldName) {
        super(residence);
        this.oldName = oldName;
    }

    /**
     * Gets the previous name of the ResidenceArea
     *
     * @return the previous name
     */
    public String getOldName() {
        return oldName;
    }
}

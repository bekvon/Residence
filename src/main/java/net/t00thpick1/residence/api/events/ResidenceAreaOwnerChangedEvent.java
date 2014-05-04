package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

public class ResidenceAreaOwnerChangedEvent extends ResidenceAreaEvent {
    private final String previousOwner;

    public ResidenceAreaOwnerChangedEvent(ResidenceArea residence, String previousOwner) {
        super(residence);
        this.previousOwner = previousOwner;
    }

    /**
     * Gets the previous owner of the ResidenceArea
     *
     * @return the previous owner
     */
    public String getPreviousOwner() {
        return previousOwner;
    }
}

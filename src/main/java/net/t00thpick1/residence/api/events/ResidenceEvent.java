package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.event.Event;

public abstract class ResidenceEvent extends Event {
    private final ResidenceArea residence;

    public ResidenceEvent(ResidenceArea residence) {
        this.residence = residence;
    }

    public final ResidenceArea getResidence() {
        return residence;
    }
}

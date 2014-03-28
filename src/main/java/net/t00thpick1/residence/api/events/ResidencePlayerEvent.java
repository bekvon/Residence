package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.entity.Player;

public abstract class ResidencePlayerEvent extends ResidenceEvent {
    private final Player player;

    public ResidencePlayerEvent(ResidenceArea residence, Player player) {
        super(residence);
        this.player = player;
    }

    public final Player getPlayer() {
        return player;
    }
}

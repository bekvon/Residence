package net.t00thpick1.residence.api.events;

import net.t00thpick1.residence.api.areas.PermissionsArea;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player changes which PermissionsArea they are in.
 *
 * @author t00thpick1
 */
public class PlayerChangedAreaEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final PermissionsArea from;
    private final PermissionsArea to;
    private final Player player;

    public PlayerChangedAreaEvent(PermissionsArea from, PermissionsArea to, Player player) {
        this.from = from;
        this.to = to;
        this.player = player;
    }

    /**
     * Gets the PermissionsArea that the player is coming from.  This may be either a ResidenceArea or a WorldArea
     *
     * @return the PermissionsArea that the player is leaving
     */
    public final PermissionsArea getFrom() {
        return from;
    }

    /**
     * Gets the PermissionsArea that the player is entering.  This may be either a ResidenceArea or a WorldArea
     *
     * @return the PermissionsArea that the player is entering
     */
    public final PermissionsArea getTo() {
        return to;
    }

    /**
     * Gets the player changing areas.
     *
     * @return the player changing areas
     */
    public final Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

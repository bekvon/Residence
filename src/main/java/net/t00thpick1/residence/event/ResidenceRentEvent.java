/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * @author Administrator
 */
public class ResidenceRentEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    RentEventType eventtype;

    public enum RentEventType {
        RENT, UNRENT, RENTABLE, UNRENTABLE, RENT_EXPIRE
    }

    public ResidenceRentEvent(ClaimedResidence resref, Player player, RentEventType type) {
        super("RESIDENCE_RENT_EVENT", resref, player);
        eventtype = type;
    }

    public RentEventType getCause() {
        return eventtype;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidenceRentEvent extends CancellableResidencePlayerEvent {

    RentEventType eventtype;

    public enum RentEventType
    {
        RENT, UNRENT, RENTABLE, UNRENTABLE, RENT_EXPIRE
    }

    public ResidenceRentEvent(ClaimedResidence resref, Player player, RentEventType type)
    {
        super("RESIDENCE_RENT_EVENT", resref, player);
        eventtype = type;
    }

    public RentEventType getCause()
    {
        return eventtype;
    }

}

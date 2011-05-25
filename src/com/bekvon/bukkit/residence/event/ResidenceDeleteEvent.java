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
public class ResidenceDeleteEvent extends CancellableResidencePlayerEvent {

    public enum DeleteCause {
        LEASE_EXPIRE,PLAYER_DELETE,OTHER
    }
    
    DeleteCause cause;

    public ResidenceDeleteEvent(Player player, ClaimedResidence resref, DeleteCause delcause)
    {
        super("RESIDENCE_DELETE", resref, player);
        cause = delcause;
    }

    public DeleteCause getCause()
    {
        return cause;
    }

}

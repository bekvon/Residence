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
public class ResidenceLeaveEvent extends ResidencePlayerEvent {
    
    public ResidenceLeaveEvent(ClaimedResidence resref, Player player)
    {
        super("RESIDENCE_LEAVE", resref, player);
    }
}

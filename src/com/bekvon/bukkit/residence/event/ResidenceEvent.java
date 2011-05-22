/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.Event;

/**
 *
 * @author Administrator
 */
public class ResidenceEvent extends Event {

    ClaimedResidence res;

    public ResidenceEvent(String eventName, ClaimedResidence resref)
    {
        super(eventName);
        res = resref;
    }

    public ClaimedResidence getResidence()
    {
        return res;
    }
}

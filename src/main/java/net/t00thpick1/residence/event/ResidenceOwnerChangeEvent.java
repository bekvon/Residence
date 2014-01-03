/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.event.HandlerList;

/**
 * @author Administrator
 */
public class ResidenceOwnerChangeEvent extends ResidenceEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected String newowner;

    public ResidenceOwnerChangeEvent(ClaimedResidence resref, String newOwner) {
        super("RESIDENCE_OWNER_CHANGE", resref);
        newowner = newOwner;
    }

    public String getNewOwner() {
        return newowner;
    }
}

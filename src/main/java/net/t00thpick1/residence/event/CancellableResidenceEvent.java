/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.event.Cancellable;

/**
 * @author Administrator
 */
public class CancellableResidenceEvent extends ResidenceEvent implements Cancellable {

    protected boolean cancelled;

    public CancellableResidenceEvent(String eventName, ClaimedResidence resref) {
        super(eventName, resref);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean bln) {
        cancelled = bln;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * @author Administrator
 */
public class CancellableResidencePlayerEvent extends ResidencePlayerEvent implements Cancellable {

    protected boolean cancelled;

    public CancellableResidencePlayerEvent(String eventName, ClaimedResidence resref, Player player) {
        super(eventName, resref, player);
        cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean bln) {
        cancelled = bln;
    }

}

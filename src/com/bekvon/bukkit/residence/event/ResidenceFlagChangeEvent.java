/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceFlagChangeEvent extends CancellableResidencePlayerFlagEvent {

    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    FlagState newstate;

    public ResidenceFlagChangeEvent(ClaimedResidence resref, Player player, String flag, FlagType type,FlagState newState, String target)
    {
        super("RESIDENCE_FLAG_CHANGE", resref, player, flag, type, target);
        newstate = newState;
    }

    public FlagState getNewState()
    {
        return newstate;
    }

}

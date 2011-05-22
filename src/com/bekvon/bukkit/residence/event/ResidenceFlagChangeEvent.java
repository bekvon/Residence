/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidenceFlagChangeEvent extends CancellableResidencePlayerFlagEvent {

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

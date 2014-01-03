/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.protection.FlagPermissions.FlagState;

import org.bukkit.event.HandlerList;

/**
 * @author Administrator
 */
public class ResidenceFlagEvent extends ResidenceEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum FlagType {
        RESIDENCE, GROUP, PLAYER
    }

    String flagname;
    FlagType flagtype;
    FlagState flagstate;
    String flagtarget;

    public ResidenceFlagEvent(String eventName, ClaimedResidence resref, String flag, FlagType type, String target) {
        super(eventName, resref);
        flagname = flag;
        flagtype = type;
        flagtarget = target;
    }

    public String getFlag() {
        return flagname;
    }

    public FlagType getFlagType() {
        return flagtype;
    }

    public String getFlagTargetPlayerOrGroup() {
        return flagtarget;
    }
}

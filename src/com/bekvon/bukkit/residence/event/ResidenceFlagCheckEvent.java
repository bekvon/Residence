/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceFlagCheckEvent extends ResidenceFlagEvent {
private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean override;
    public boolean overridevalue;
    boolean defaultvalue;

    public ResidenceFlagCheckEvent(ClaimedResidence resref, String flag, FlagType type, String target, boolean defaultValue)
    {
        super("RESIDENCE_FLAG_CHECK", resref, flag, type, target);
        defaultvalue = defaultValue;
        override = false;
    }

    public boolean isOverriden()
    {
        return override;
    }

    public void overrideCheck(boolean flagval)
    {
        overridevalue = flagval;
    }

    public boolean getOverrideValue()
    {
        return overridevalue;
    }

    public boolean getDefaultValue()
    {
        return defaultvalue;
    }
}

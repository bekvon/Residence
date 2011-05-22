/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 *
 * @author Administrator
 */
public class ResidenceFlagCheckEvent extends ResidenceFlagEvent {

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

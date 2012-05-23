/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidencePlayerFlagEvent extends ResidenceFlagEvent implements ResidencePlayerEventInterface {
    Player p;

    public ResidencePlayerFlagEvent(String eventName,ClaimedResidence resref, Player player, String flag, FlagType type, String target)
    {
        super(eventName, resref, flag, type, target);
        p = player;
    }

    public boolean isPlayer()
    {
        return p!=null;
    }

    public boolean isAdmin()
    {
        if(isPlayer())
        {
            return Residence.getPermissionManager().isResidenceAdmin(p);
        }
        return true;
    }

    public Player getPlayer() {
        return p;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.entity.Player;

/**
 * @author Administrator
 */
public class ResidencePlayerFlagEvent extends ResidenceFlagEvent implements ResidencePlayerEventInterface {
    Player p;

    public ResidencePlayerFlagEvent(String eventName, ClaimedResidence resref, Player player, String flag, FlagType type, String target) {
        super(eventName, resref, flag, type, target);
        p = player;
    }

    public boolean isPlayer() {
        return p != null;
    }

    public boolean isAdmin() {
        if (isPlayer()) {
            return Residence.getPermissionManager().isResidenceAdmin(p);
        }
        return true;
    }

    public Player getPlayer() {
        return p;
    }
}

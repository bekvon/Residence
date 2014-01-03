package net.t00thpick1.residence.flags;

import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.entity.Player;

public abstract class Flag {
    public boolean allowAction(Player player, PermissionsArea area) {
        if (area instanceof ClaimedResidence) {
            return allowAction(player, ResidenceAPI.getResidenceWorld(area.getWorld()));
        }
        return false;
    }

    public boolean allowAction(PermissionsArea area) {
        if (area instanceof ClaimedResidence) {
            return allowAction(ResidenceAPI.getResidenceWorld(area.getWorld()));
        }
        return false;
    }

    public boolean isAdminMode(Player player) {
        return Utilities.isAdminMode(player);
    }
}

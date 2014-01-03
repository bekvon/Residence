package net.t00thpick1.residence.api;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface PermissionsArea {
    public abstract boolean allowAction(String flag, boolean defaultIfNotSet);
    public abstract boolean allowAction(Player player, String flag, boolean defaultIfNotSet);
    public abstract World getWorld();
}

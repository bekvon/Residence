package net.t00thpick1.residence.api;

import org.bukkit.World;

public interface PermissionsArea {
    public abstract boolean allow(String flag, boolean defaultIfNotSet);
    public abstract World getWorld();
}

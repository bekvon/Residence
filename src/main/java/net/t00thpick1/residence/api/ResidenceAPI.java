package net.t00thpick1.residence.api;

import org.bukkit.Location;
import org.bukkit.World;

import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.protection.ProtectionFactory;

public class ResidenceAPI {
    public static PermissionsArea getPermissionsAreaByLocation(Location location) {
        PermissionsArea area = getResidenceManager().getByLocation(location);
        if (area == null) {
            area = getResidenceWorld(location.getWorld());
        }
        return area;
    }

    public static ResidenceManager getResidenceManager() {
        return ProtectionFactory.getResidenceManager();
    }

    public static CuboidArea createCuboidArea(Location lowPoint, Location highPoint) {
        return ProtectionFactory.createNewCuboidArea(lowPoint, highPoint);
    }

    public static PermissionsArea getResidenceWorld(World world) {
        return ProtectionFactory.getWorldManager().getResidenceWorld(world);
    }

    public static UsernameUUIDCache getUsernameUUIDCache() {
        return ProtectionFactory.getUsernameUUIDCache();
    }

    public static EconomyManager getEconomyManager() {
        return ProtectionFactory.getEconomyManager();
    }
}

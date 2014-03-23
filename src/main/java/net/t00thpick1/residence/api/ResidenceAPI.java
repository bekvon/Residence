package net.t00thpick1.residence.api;

import org.bukkit.Location;
import org.bukkit.World;

import net.t00thpick1.residence.Residence;

public class ResidenceAPI {
    public static PermissionsArea getPermissionsAreaByLocation(Location location) {
        PermissionsArea area = Residence.getInstance().getResidenceManager().getByLoc(location);
        if (area == null) {
            area = getResidenceWorld(location.getWorld());
        }
        return area;
    }

    public static PermissionsArea getResidenceWorld(World world) {
        return Residence.getInstance().getWorldManager().getResidenceWorld(world);
    }
}

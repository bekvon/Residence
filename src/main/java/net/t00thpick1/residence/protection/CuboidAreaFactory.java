package net.t00thpick1.residence.protection;

import org.bukkit.Location;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.protection.yaml.YAMLCuboidArea;

public class CuboidAreaFactory {
    public static CuboidArea createNewCuboidArea(Location loc1, Location loc2) {
        switch (Residence.getInstance().getBackend()) {
            case MYSQL:
                throw new UnsupportedOperationException();
            case WORLDGUARD:
                throw new UnsupportedOperationException();
            case YAML:
                return new YAMLCuboidArea(loc1, loc2);
        }
        return null;
    }
}

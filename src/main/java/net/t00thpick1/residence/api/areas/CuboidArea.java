package net.t00thpick1.residence.api.areas;

import org.bukkit.Location;
import org.bukkit.World;

public interface CuboidArea {
    public abstract boolean isAreaWithin(CuboidArea area);

    public abstract boolean containsLocation(Location loc);

    public abstract boolean containsLocation(World world, int x, int y, int z);

    public abstract boolean checkCollision(CuboidArea area);

    public abstract long getSize();

    public abstract int getXSize();

    public abstract int getYSize();

    public abstract int getZSize();

    public abstract Location getHighLocation();

    public abstract Location getLowLocation();

    public abstract World getWorld();

    public abstract Location getCenter();

    public abstract int getHighX();

    public abstract int getHighY();

    public abstract int getHighZ();

    public abstract int getLowX();

    public abstract int getLowY();

    public abstract int getLowZ();
}

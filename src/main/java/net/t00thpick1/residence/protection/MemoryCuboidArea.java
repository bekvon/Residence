package net.t00thpick1.residence.protection;

import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.protection.MemoryResidenceManager.ChunkRef;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;

public class MemoryCuboidArea implements CuboidArea {
    protected World world;
    protected int highX;
    protected int highY;
    protected int highZ;
    protected int lowX;
    protected int lowY;
    protected int lowZ;

    public MemoryCuboidArea(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        highX = x1;
        highY = y1;
        highZ = z1;
        lowX = x2;
        lowY = y2;
        lowZ = z2;
    }

    public MemoryCuboidArea(Location startLoc, Location endLoc) {
        if (startLoc.getBlockX() > endLoc.getBlockX()) {
            highX = startLoc.getBlockX();
            lowX = endLoc.getBlockX();
        } else {
            highX = endLoc.getBlockX();
            lowX = startLoc.getBlockX();
        }
        if (startLoc.getBlockY() > endLoc.getBlockY()) {
            highY = startLoc.getBlockY();
            lowY = endLoc.getBlockY();
        } else {
            highY = endLoc.getBlockY();
            lowY = startLoc.getBlockY();
        }
        if (startLoc.getBlockZ() > endLoc.getBlockZ()) {
            highZ = startLoc.getBlockZ();
            lowZ = endLoc.getBlockZ();
        } else {
            highZ = endLoc.getBlockZ();
            lowZ = startLoc.getBlockZ();
        }
        world = startLoc.getWorld();
    }

    public MemoryCuboidArea(CuboidArea area) {
        this.world = area.getWorld();
        this.highX = area.getHighX();
        this.highY = area.getHighY();
        this.highZ = area.getHighZ();
        this.lowX = area.getLowX();
        this.lowY = area.getLowY();
        this.lowZ = area.getLowZ();
    }

    public boolean isAreaWithin(CuboidArea area) {
        if (!area.getWorld().equals(world)) {
            return false;
        }
        return (containsLocation(area.getHighX(), area.getHighY(), area.getHighZ()) && containsLocation(area.getLowX(), area.getLowY(), area.getLowZ()));
    }

    public boolean containsLocation(Location loc) {
        if (loc == null) {
            return false;
        }
        return containsLocation(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean containsLocation(World world, int x, int y, int z) {
        if (!world.equals(getWorld())) {
            return false;
        }
        return containsLocation(x, y, z);
    }

    private boolean containsLocation(int x, int y, int z) {
        if (lowX <= x && highX >= x) {
            if (lowZ <= z && highZ >= z) {
                if (lowY <= y && highY >= y) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCollision(CuboidArea area) {
        if (area.containsLocation(getWorld(), getHighX(), getHighY(), getHighZ()) || area.containsLocation(getWorld(), getLowX(), getLowY(), getLowZ()) || this.containsLocation(area.getHighX(), area.getHighY(), area.getHighZ()) || this.containsLocation(area.getLowX(), area.getLowY(), area.getLowZ())) {
            return true;
        }
        return advCuboidCheckCollision(area);
    }

    private boolean advCuboidCheckCollision(CuboidArea area) {
        if ((getHighX() >= area.getLowX() && getHighX() <= area.getHighX()) || (getLowX() >= area.getLowX() && getLowX() <= area.getHighX()) || (area.getHighX() >= getLowX() && area.getHighX() <= getHighX()) || (area.getLowX() >= getLowX() && area.getLowX() <= getHighX())) {
            if ((getHighY() >= area.getLowY() && getHighY() <= area.getHighY()) || (getLowY() >= area.getLowY() && getLowY() <= area.getHighY()) || (area.getHighY() >= getLowY() && area.getHighY() <= getHighY()) || (area.getLowY() >= getLowY() && area.getLowY() <= getHighY())) {
                if ((getHighZ() >= area.getLowZ() && getHighZ() <= area.getHighZ()) || (getLowZ() >= area.getLowZ() && getLowZ() <= area.getHighZ()) || (area.getHighZ() >= getLowZ() && area.getHighZ() <= getHighZ()) || (area.getLowZ() >= getLowZ() && area.getLowZ() <= getHighZ())) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getSize() {
        int xsize = (highX - lowX) + 1;
        int ysize = (highY - lowY) + 1;
        int zsize = (highZ - lowZ) + 1;
        return xsize * ysize * zsize;
    }

    public int getXSize() {
        return (highX - lowX);
    }

    public int getYSize() {
        return (highY - lowY);
    }

    public int getZSize() {
        return (highZ - lowZ);
    }

    public Location getHighLocation() {
        return new Location(world, highX, highY, highZ);
    }

    public Location getLowLocation() {
        return new Location(world, lowX, lowY, lowZ);
    }

    public World getWorld() {
        return world;
    }

    public Location getCenter() {
        return new Location(world, (highX + getLowX()) / 2, (highY + getLowY()) / 2, (highZ + lowZ) / 2);
    }

    public List<ChunkRef> getChunks() {
        List<ChunkRef> chunks = new ArrayList<ChunkRef>();
        int lowCX = ChunkRef.getBase(lowX);
        int lowCZ = ChunkRef.getBase(lowZ);
        int highCX = ChunkRef.getBase(highX);
        int highCZ = ChunkRef.getBase(highZ);

        for (int x = lowCX; x <= highCX; x++) {
            for (int z = lowCZ; z <= highCZ; z++) {
                chunks.add(new ChunkRef(x, z));
            }
        }
        return chunks;
    }

    @Override
    public int getHighX() {
        return highX;
    }

    @Override
    public int getHighY() {
        return highY;
    }

    @Override
    public int getHighZ() {
        return highZ;
    }

    @Override
    public int getLowX() {
        return lowX;
    }

    @Override
    public int getLowY() {
        return lowY;
    }

    @Override
    public int getLowZ() {
        return lowZ;
    }

    public static class MemoryCuboidAreaFactory implements CuboidAreaFactory {
        @Override
        public CuboidArea createArea(Location loc1, Location loc2) {
            return new MemoryCuboidArea(loc1, loc2);
        }

    }
}

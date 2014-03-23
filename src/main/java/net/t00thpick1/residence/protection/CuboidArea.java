package net.t00thpick1.residence.protection;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.protection.ResidenceManager.ChunkRef;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class CuboidArea {
    protected World world;
    private int highX;
    private int highY;
    private int highZ;
    private int lowX;
    private int lowY;
    private int lowZ;

    protected CuboidArea() {
    }

    public CuboidArea(Location startLoc, Location endLoc) {
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

    public boolean isAreaWithin(CuboidArea area) {
        if (!area.getWorld().equals(world)) {
            return false;
        }
        return (containsLocation(area.highX, area.highY, area.highZ) && containsLocation(area.lowX, area.lowY, area.lowZ));
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
        if (!area.getWorld().equals(this.getWorld())) {
            return false;
        }
        if (area.containsLocation(highX, highY, highZ) || area.containsLocation(lowX, lowY, lowZ) || this.containsLocation(area.highX, area.highY, area.highZ) || this.containsLocation(area.lowX, area.lowY, area.lowZ)) {
            return true;
        }
        return advCuboidCheckCollision(area);
    }

    private boolean advCuboidCheckCollision(CuboidArea area) {
        if ((highX >= area.lowX && highX <= area.highX) || (lowX >= area.lowX && lowX <= area.highX) || (area.highX >= lowX && area.highX <= highX) || (area.lowX >= lowX && area.lowX <= highX)) {
            if ((highY >= area.lowY && highY <= area.highY) || (lowY >= area.lowY && lowY <= area.highY) || (area.highY >= lowY && area.highY <= highY) || (area.lowY >= lowY && area.lowY <= highY)) {
                if ((highZ >= area.lowZ && highZ <= area.highZ) || (lowZ >= area.lowZ && lowZ <= area.highZ) || (area.highZ >= lowZ && area.highZ <= highZ) || (area.lowZ >= lowZ && area.lowZ <= highZ)) {
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
        return (highX - lowX) + 1;
    }

    public int getYSize() {
        return (highY - lowY) + 1;
    }

    public int getZSize() {
        return (highZ - lowZ) + 1;
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

    public void saveArea(ConfigurationSection section) {
        section.set("World", getWorld().getName());
        section.set("X1", highX);
        section.set("Y1", highY);
        section.set("Z1", highZ);
        section.set("X2", lowX);
        section.set("Y2", lowY);
        section.set("Z2", lowZ);
    }

    public void loadArea(ConfigurationSection section) throws Exception {
        String worldName = section.getString("World");
        world = Residence.getInstance().getServer().getWorld(worldName);
        if (world == null) {
            throw new Exception("Cant Find World: " + worldName);
        }
        highX = section.getInt("X1");
        highY = section.getInt("Y1");
        highZ = section.getInt("Z1");
        lowX = section.getInt("X2");
        lowY = section.getInt("Y2");
        lowZ = section.getInt("Z2");
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

    public Location getCenter() {
        return new Location(world, (highX + lowX) / 2, (highY + lowY) / 2, (highZ + lowZ) / 2);
    }
}

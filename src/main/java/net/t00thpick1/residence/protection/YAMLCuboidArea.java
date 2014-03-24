package net.t00thpick1.residence.protection;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.CuboidArea;
import net.t00thpick1.residence.protection.YAMLResidenceManager.ChunkRef;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class YAMLCuboidArea implements CuboidArea {
    protected World world;
    protected int highX;
    protected int highY;
    protected int highZ;
    protected int lowX;
    protected int lowY;
    protected int lowZ;

    protected YAMLCuboidArea() { }

    public YAMLCuboidArea(Location startLoc, Location endLoc) {
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
            highY = startLoc.getBlockY();
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
        if (getLowX() <= x && highX >= x) {
            if (lowZ <= z && highZ >= z) {
                if (getLowY() <= y && highY >= y) {
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
        int xsize = (highX - getLowX()) + 1;
        int ysize = (highY - getLowY()) + 1;
        int zsize = (highZ - lowZ) + 1;
        return xsize * ysize * zsize;
    }

    public int getXSize() {
        return (highX - getLowX()) + 1;
    }

    public int getYSize() {
        return (highY - getLowY()) + 1;
    }

    public int getZSize() {
        return (highZ - lowZ) + 1;
    }

    public Location getHighLocation() {
        return new Location(world, highX, highY, highZ);
    }

    public Location getLowLocation() {
        return new Location(world, getLowX(), getLowY(), lowZ);
    }

    public World getWorld() {
        return world;
    }

    public Location getCenter() {
        return new Location(world, (highX + getLowX()) / 2, (highY + getLowY()) / 2, (highZ + lowZ) / 2);
    }

    public void saveArea(ConfigurationSection section) {
        section.set("World", getWorld().getName());
        section.set("X1", highX);
        section.set("Y1", highY);
        section.set("Z1", highZ);
        section.set("X2", getLowX());
        section.set("Y2", getLowY());
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
        int lowCX = ChunkRef.getBase(getLowX());
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
        return getLowX();
    }

    @Override
    public int getLowY() {
        return getLowY();
    }

    @Override
    public int getLowZ() {
        return lowZ;
    }
}

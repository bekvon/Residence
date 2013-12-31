package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager.ChunkRef;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CuboidArea {
    protected Location highPoints;
    protected Location lowPoints;

    protected CuboidArea() {}

    protected CuboidArea(CuboidArea area) {
        this.highPoints = area.highPoints;
        this.lowPoints = area.lowPoints;
    }

    public CuboidArea(Location startLoc, Location endLoc) {
        int highx, highy, highz, lowx, lowy, lowz;
        if (startLoc.getBlockX() > endLoc.getBlockX()) {
            highx = startLoc.getBlockX();
            lowx = endLoc.getBlockX();
        } else {
            highx = endLoc.getBlockX();
            lowx = startLoc.getBlockX();
        }
        if (startLoc.getBlockY() > endLoc.getBlockY()) {
            highy = startLoc.getBlockY();
            lowy = endLoc.getBlockY();
        } else {
            highy = endLoc.getBlockY();
            lowy = startLoc.getBlockY();
        }
        if (startLoc.getBlockZ() > endLoc.getBlockZ()) {
            highz = startLoc.getBlockZ();
            lowz = endLoc.getBlockZ();
        } else {
            highz = endLoc.getBlockZ();
            lowz = startLoc.getBlockZ();
        }
        highPoints = new Location(startLoc.getWorld(), highx, highy, highz);
        lowPoints = new Location(startLoc.getWorld(), lowx, lowy, lowz);
    }

    public boolean isAreaWithinArea(CuboidArea area) {
        return (this.containsLoc(area.highPoints) && this.containsLoc(area.lowPoints));
    }

    public boolean containsLoc(Location loc) {
        if (loc == null) {
            return false;
        }
        if (!loc.getWorld().equals(highPoints.getWorld())) {
            return false;
        }
        if (lowPoints.getBlockX() <= loc.getBlockX() && highPoints.getBlockX() >= loc.getBlockX()) {
            if (lowPoints.getBlockZ() <= loc.getBlockZ() && highPoints.getBlockZ() >= loc.getBlockZ()) {
                if (lowPoints.getBlockY() <= loc.getBlockY() && highPoints.getBlockY() >= loc.getBlockY()) {
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
        if (area.containsLoc(lowPoints) || area.containsLoc(highPoints) || this.containsLoc(area.highPoints) || this.containsLoc(area.lowPoints)) {
            return true;
        }
        return advCuboidCheckCollision(highPoints, lowPoints, area.highPoints, area.lowPoints);
    }

    private boolean advCuboidCheckCollision(Location A1High, Location A1Low, Location A2High, Location A2Low) {
        int A1HX = A1High.getBlockX();
        int A1LX = A1Low.getBlockX();
        int A1HY = A1High.getBlockY();
        int A1LY = A1Low.getBlockY();
        int A1HZ = A1High.getBlockZ();
        int A1LZ = A1Low.getBlockZ();
        int A2HX = A2High.getBlockX();
        int A2LX = A2Low.getBlockX();
        int A2HY = A2High.getBlockY();
        int A2LY = A2Low.getBlockY();
        int A2HZ = A2High.getBlockZ();
        int A2LZ = A2Low.getBlockZ();
        if ((A1HX >= A2LX && A1HX <= A2HX) || (A1LX >= A2LX && A1LX <= A2HX) || (A2HX >= A1LX && A2HX <= A1HX) || (A2LX >= A1LX && A2LX <= A1HX)) {
            if ((A1HY >= A2LY && A1HY <= A2HY) || (A1LY >= A2LY && A1LY <= A2HY) || (A2HY >= A1LY && A2HY <= A1HY) || (A2LY >= A1LY && A2LY <= A1HY)) {
                if ((A1HZ >= A2LZ && A1HZ <= A2HZ) || (A1LZ >= A2LZ && A1LZ <= A2HZ) || (A2HZ >= A1LZ && A2HZ <= A1HZ) || (A2LZ >= A1LZ && A2LZ <= A1HZ)) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getSize() {
        int xsize = (highPoints.getBlockX() - lowPoints.getBlockX()) + 1;
        int ysize = (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
        int zsize = (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
        return xsize * ysize * zsize;
    }

    public int getXSize() {
        return (highPoints.getBlockX() - lowPoints.getBlockX()) + 1;
    }

    public int getYSize() {
        return (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
    }

    public int getZSize() {
        return (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
    }

    public Location getHighLoc() {
        return highPoints;
    }

    public Location getLowLoc() {
        return lowPoints;
    }

    public World getWorld() {
        return highPoints.getWorld();
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("X1", this.highPoints.getBlockX());
        root.put("Y1", this.highPoints.getBlockY());
        root.put("Z1", this.highPoints.getBlockZ());
        root.put("X2", this.lowPoints.getBlockX());
        root.put("Y2", this.lowPoints.getBlockY());
        root.put("Z2", this.lowPoints.getBlockZ());
        return root;
    }

    public void load(Map<String, Object> root) throws Exception {
        if (root == null) {
            throw new Exception("Invalid residence physical location...");
        }
        String worldName = (String) root.get("World");
        World world = Residence.getInstance().getServer().getWorld(worldName);
        if (world == null) {
            throw new Exception("Cant Find World: " + worldName);
        }
        int x1 = (Integer) root.get("X1");
        int y1 = (Integer) root.get("Y1");
        int z1 = (Integer) root.get("Z1");
        int x2 = (Integer) root.get("X2");
        int y2 = (Integer) root.get("Y2");
        int z2 = (Integer) root.get("Z2");
        highPoints = new Location(world, x1, y1, z1);
        lowPoints = new Location(world, x2, y2, z2);
    }

    public List<ChunkRef> getChunks() {
        List<ChunkRef> chunks = new ArrayList<ChunkRef>();
        Location high = this.highPoints;
        Location low = this.lowPoints;
        int lowX = ChunkRef.getBase(low.getBlockX());
        int lowZ = ChunkRef.getBase(low.getBlockZ());
        int highX = ChunkRef.getBase(high.getBlockX());
        int highZ = ChunkRef.getBase(high.getBlockZ());

        for (int x = lowX; x <= highX; x++) {
            for (int z = lowZ; z <= highZ; z++) {
                chunks.add(new ChunkRef(x, z));
            }
        }
        return chunks;
    }
}

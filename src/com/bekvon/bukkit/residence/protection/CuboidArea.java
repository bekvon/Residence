package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ResidenceManager.ChunkRef;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class CuboidArea {
    private Vector highPoints;
    private Vector lowPoints;
    protected String worldName;
    protected World world;

    public CuboidArea(Location startLoc, Location endLoc) {
	
	if (startLoc == null || endLoc == null)
	    return;
	
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
	highPoints = new Vector(highx, highy, highz);
	lowPoints = new Vector(lowx, lowy, lowz);

	world = startLoc.getWorld() != null ? startLoc.getWorld() : startLoc.getWorld() != null ? startLoc.getWorld() : null;

	worldName = world != null ? world.getName() : null;
    }

    public CuboidArea() {
    }

    public boolean isAreaWithinArea(CuboidArea area) {
	return (this.containsLoc(area.highPoints, area.getWorldName()) && this.containsLoc(area.lowPoints, area.getWorldName()));
    }

    public boolean containsLoc(Location loc) {
	return containsLoc(loc.toVector(), loc.getWorld().getName());
    }

    public boolean containsLoc(Vector loc, String world) {
	if (loc == null)
	    return false;

	if (!world.equals(worldName))
	    return false;

	if (lowPoints.getBlockX() > loc.getBlockX())
	    return false;

	if (highPoints.getBlockX() < loc.getBlockX())
	    return false;

	if (lowPoints.getBlockZ() > loc.getBlockZ())
	    return false;

	if (highPoints.getBlockZ() < loc.getBlockZ())
	    return false;

	if (lowPoints.getBlockY() > loc.getBlockY())
	    return false;

	if (highPoints.getBlockY() < loc.getBlockY())
	    return false;

	return true;
    }

    public boolean checkCollision(CuboidArea area) {
	if (!area.getWorld().equals(this.getWorld())) {
	    return false;
	}
	if (area.containsLoc(lowPoints, getWorldName()) || area.containsLoc(highPoints, getWorldName()) || this.containsLoc(area.highPoints, getWorldName()) || this.containsLoc(area.lowPoints,
	    getWorldName())) {
	    return true;
	}
	return advCuboidCheckCollision(highPoints, lowPoints, area.highPoints, area.lowPoints);
    }

    private static boolean advCuboidCheckCollision(Vector A1High, Vector A1Low, Vector A2High, Vector A2Low) {
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
	int zsize = (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
	if (!Residence.getInstance().getConfigManager().isNoCostForYBlocks()) {
	    int ysize = (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
	    return xsize * ysize * zsize;
	}
	return xsize * zsize;
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

    public Vector getHighVector() {
	return highPoints;
    }

    public Vector getLowVector() {
	return lowPoints;
    }

    @Deprecated
    public Location getHighLoc() {
	return getHighLocation();
    }

    @Deprecated
    public Location getLowLoc() {
	return getLowLocation();
    }

    public Location getHighLocation() {
	return highPoints.toLocation(getWorld());
    }

    public Location getLowLocation() {
	return lowPoints.toLocation(getWorld());
    }

    public World getWorld() {
	return world;
    }

    public String getWorldName() {
	return world != null ? world.getName() : worldName;
    }

    public void save(DataOutputStream out) throws IOException {
	out.writeUTF(getWorldName());
	out.writeInt(highPoints.getBlockX());
	out.writeInt(highPoints.getBlockY());
	out.writeInt(highPoints.getBlockZ());
	out.writeInt(lowPoints.getBlockX());
	out.writeInt(lowPoints.getBlockY());
	out.writeInt(lowPoints.getBlockZ());
    }

    public CuboidArea load(DataInputStream in) throws IOException {
	CuboidArea newArea = new CuboidArea();
	Server server = Residence.getInstance().getServ();
	int highx = in.readInt();
	int highy = in.readInt();
	int highz = in.readInt();
	int lowx = in.readInt();
	int lowy = in.readInt();
	int lowz = in.readInt();
	newArea.highPoints = new Vector(highx, highy, highz);
	newArea.lowPoints = new Vector(lowx, lowy, lowz);
	newArea.worldName = in.readUTF();
	newArea.world = Bukkit.getWorld(newArea.worldName);
	return newArea;
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new LinkedHashMap<>();
	root.put("X1", this.highPoints.getBlockX());
	root.put("Y1", this.highPoints.getBlockY());
	root.put("Z1", this.highPoints.getBlockZ());
	root.put("X2", this.lowPoints.getBlockX());
	root.put("Y2", this.lowPoints.getBlockY());
	root.put("Z2", this.lowPoints.getBlockZ());
	return root;
    }

    public String newSave() {
	return lowPoints.getBlockX() + ":" + lowPoints.getBlockY() + ":" + lowPoints.getBlockZ() + ":" + highPoints.getBlockX() + ":" + highPoints.getBlockY() + ":" + highPoints.getBlockZ();
    }

    public static CuboidArea newLoad(String root, String world) throws Exception {
	if (root == null || !root.contains(":")) {
	    throw new Exception("Invalid residence physical location...");
	}
	CuboidArea newArea = new CuboidArea();
	String[] split = root.split(":");
	try {
	    int x1 = Integer.parseInt(split[0]);
	    int y1 = Integer.parseInt(split[1]);
	    int z1 = Integer.parseInt(split[2]);
	    int x2 = Integer.parseInt(split[3]);
	    int y2 = Integer.parseInt(split[4]);
	    int z2 = Integer.parseInt(split[5]);
	    newArea.lowPoints = new Vector(x1, y1, z1);
	    newArea.highPoints = new Vector(x2, y2, z2);
	    newArea.worldName = world;
	    newArea.world = Bukkit.getWorld(newArea.worldName);
	} catch (Exception e) {
	    throw new Exception("Invalid residence physical location...");
	}

	return newArea;
    }

    public static CuboidArea load(Map<String, Object> root, String world) throws Exception {
	if (root == null) {
	    throw new Exception("Invalid residence physical location...");
	}
	CuboidArea newArea = new CuboidArea();
	int x1 = (Integer) root.get("X1");
	int y1 = (Integer) root.get("Y1");
	int z1 = (Integer) root.get("Z1");
	int x2 = (Integer) root.get("X2");
	int y2 = (Integer) root.get("Y2");
	int z2 = (Integer) root.get("Z2");
	newArea.highPoints = new Vector(x1, y1, z1);
	newArea.lowPoints = new Vector(x2, y2, z2);
	newArea.worldName = world;
	newArea.world = Bukkit.getWorld(newArea.worldName);
	return newArea;
    }

    public List<ChunkRef> getChunks() {
	List<ChunkRef> chunks = new ArrayList<>();
	Vector high = this.highPoints;
	Vector low = this.lowPoints;
	int lowX = ChunkRef.getChunkCoord(low.getBlockX());
	int lowZ = ChunkRef.getChunkCoord(low.getBlockZ());
	int highX = ChunkRef.getChunkCoord(high.getBlockX());
	int highZ = ChunkRef.getChunkCoord(high.getBlockZ());

	for (int x = lowX; x <= highX; x++) {
	    for (int z = lowZ; z <= highZ; z++) {
		chunks.add(new ChunkRef(x, z));
	    }
	}
	return chunks;
    }

    public void setHighLocation(Location highLocation) {
	this.highPoints = highLocation.toVector();
	this.world = highLocation.getWorld();
    }

    public void setLowLocation(Location lowLocation) {
	this.lowPoints = lowLocation.toVector();
	this.world = lowLocation.getWorld();
    }

    public double getCost(PermissionGroup group) {
	return (int) (getSize() * group.getCostPerBlock() * 100) / 100D;
    }
}

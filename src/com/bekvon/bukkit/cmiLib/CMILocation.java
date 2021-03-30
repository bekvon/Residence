package com.bekvon.bukkit.cmiLib;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Utility;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

public class CMILocation extends Location {

    public CMILocation(World world, double x, double y, double z, float yaw, float pitch) {
	super(world, x, y, z, yaw, pitch);
	this.worldName = world.getName();
    }

    public CMILocation(World world, double x, double y, double z) {
	super(world, x, y, z);
	if (world != null)
	    this.worldName = world.getName();
    }

    private String worldName;

    public CMILocation(Location loc) {
	super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	if (loc.getWorld() != null)
	    this.worldName = loc.getWorld().getName();
    }

    public CMILocation(String world, double x, double y, double z, float yaw, float pitch) {
	super(Bukkit.getWorld(world), x, y, z, yaw, pitch);
	this.worldName = world;
    }

    public CMILocation(String world, double x, double y, double z) {
	super(Bukkit.getWorld(world), x, y, z);
	this.worldName = world;
    }

    public void recheck() {
	updateWorld();
    }

    private void updateWorld() {
	try {
	    if (Version.isCurrentEqualOrHigher(Version.v1_16_R1) && super.getWorld() != null && !super.isWorldLoaded())
		return;
	} catch (Throwable e) {
	}

	try {
	    if (super.getWorld() == null && worldName != null) {
		World w = Bukkit.getWorld(worldName);
		if (w != null) {
		    super.setWorld(w);
		    this.worldName = super.getWorld().getName();
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public String getWorldName() {
	return this.worldName != null ? this.worldName : this.getWorld() == null ? null : this.getWorld().getName();
    }

    public Location getBukkitLoc() {
	updateWorld();
	return this;
    }

    public boolean isValid() {
	updateWorld();
	return this.getWorld() != null && this.getWorld().getName() != null && Bukkit.getWorld(this.getWorld().getUID()) != null;
    }

    @Override
    public World getWorld() {
	updateWorld();
	try {
	    if (Version.isCurrentEqualOrHigher(Version.v1_16_R1) && !super.isWorldLoaded())
		return null;
	} catch (Throwable e) {
	}
	return super.getWorld();
    }

    @Override
    public Chunk getChunk() {
	updateWorld();
	return super.getChunk();
    }

    @Override
    public Block getBlock() {
	updateWorld();
	if (super.getWorld() == null)
	    return null;
	return super.getBlock();
    }

    @Override
    public Location add(Location vec) {
	updateWorld();
	return super.add(vec);
    }

    @Override
    public Location subtract(Location vec) {
	updateWorld();
	return super.subtract(vec);
    }

    @Override
    public double distanceSquared(Location o) {
	if (o == null)
	    return Integer.MAX_VALUE;
	if (!this.isValid())
	    return Integer.MAX_VALUE;
	return super.distanceSquared(o);
    }

    @Override
    public double distance(Location o) {
	if (o == null)
	    return Integer.MAX_VALUE;
	if (!this.isValid())
	    return Integer.MAX_VALUE;
	return super.distance(o);
    }

    @Override
    public boolean equals(Object obj) {
	updateWorld();
	return super.equals(obj);
    }

    @Override
    public int hashCode() {
	updateWorld();
	return super.hashCode();
    }

    @Override
    public String toString() {
	updateWorld();
	return super.toString();
    }

    @Override
    public CMILocation clone() {
	updateWorld();
	return new CMILocation(super.clone());
    }

    @Override
    @Utility
    public Map<String, Object> serialize() {
	updateWorld();
	return super.serialize();
    }

    public int getHighestBlockYAt() {
//	Location loc = this.getBukkitLoc();
//	if (loc == null)
//	    return 0;
//	return this.getWorld().getHighestBlockYAt(this);

	if (this.getWorld() == null)
	    return 63;

	ChunkSnapshot chunk = this.getWorld().getEmptyChunkSnapshot(this.getBlockX() >> 4, this.getBlockZ() >> 4, true, true);

	int x = this.getBlockX() % 16;
	x = x < 0 ? 16 + x : x;
	int z = this.getBlockZ() % 16;
	z = z < 0 ? 16 + z : z;

	return chunk.getHighestBlockYAt(x, z);

//	ChunkSnapshot snap = loc.getChunk().getChunkSnapshot(true, false, false);
//
//	int x = this.getBlockX() - (snap.getX() * 16);
//	int z = this.getBlockZ() - (snap.getZ() * 16);
//	
//	x = x < 0 ? 0 : x > 15 ? 15 : x;
//	z = z < 0 ? 0 : z > 15 ? 15 : z;
//	
//	return snap.getHighestBlockYAt(x, z);
    }

    public Material getBlockType() {
	Location loc = this.getBukkitLoc();
	if (loc == null)
	    return Material.AIR;

	try {
	    if (Version.isCurrentEqualOrHigher(Version.v1_16_R1) && !super.isWorldLoaded())
		return Material.AIR;
	} catch (Throwable e) {
	}
	return this.getBlock().getType();
    }

    private static MethodHandle getBlockTypeId = null;
    private static MethodHandle getBlockData = null;

    public static Material getBlockTypeSafe(Location loc) {
	if (loc == null)
	    return Material.AIR;

	int x = loc.getBlockX();
	int z = loc.getBlockZ();
	int y = loc.getBlockY();

	int cx = Math.abs(loc.getBlockX() % 16);
	int cz = Math.abs(loc.getBlockZ() % 16);
	World world = loc.getWorld();

	ChunkSnapshot chunkSnapshot = null;

	if (!world.getBlockAt(x, 0, z).getChunk().isLoaded()) {
	    world.getBlockAt(x, 0, z).getChunk().load();
	    chunkSnapshot = world.getBlockAt(x, 0, z).getChunk().getChunkSnapshot(false, false, false);
	    world.getBlockAt(x, 0, z).getChunk().unload();
	} else {
	    chunkSnapshot = world.getBlockAt(x, 0, z).getChunk().getChunkSnapshot();
	}

	if (chunkSnapshot == null)
	    return Material.AIR;

	if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
	    BlockData type = chunkSnapshot.getBlockData(cx, y, cz);
	    return type.getMaterial();
	}

	if (getBlockTypeId == null) {
	    MethodHandles.Lookup lookup = MethodHandles.lookup();
	    try {
		getBlockTypeId = lookup.findVirtual(ChunkSnapshot.class, "getBlockTypeId", MethodType.methodType(int.class, int.class, int.class)).asType(MethodType.methodType(int.class,
		    ChunkSnapshot.class));
		getBlockData = lookup.findVirtual(ChunkSnapshot.class, "getBlockData", MethodType.methodType(int.class, int.class, int.class)).asType(MethodType.methodType(int.class,
		    ChunkSnapshot.class));
	    } catch (NoSuchMethodException | IllegalAccessException e) {
		e.printStackTrace();
	    }
	}

	if (getBlockTypeId == null || getBlockData == null) {
	    return Material.AIR;
	}
	try {
	    int type = (int) getBlockTypeId.invokeExact(chunkSnapshot);
	    if (type == 0) {
		return Material.AIR;
	    }
	    int data = (int) getBlockData.invokeExact(chunkSnapshot);
	    return CMIMaterial.get(type, data).getMaterial();
	} catch (Throwable e) {
	    e.printStackTrace();
	}

	return Material.AIR;
    }

    public CMIMaterial getBlockCMIType() {
	Location loc = this.getBukkitLoc();
	if (loc == null)
	    return CMIMaterial.AIR;

	try {
	    if (Version.isCurrentEqualOrHigher(Version.v1_16_R1) && !super.isWorldLoaded())
		return CMIMaterial.AIR;
	} catch (Throwable e) {
	}

	CMIMaterial mat = CMIMaterial.get(this.getBlock());
	return mat == CMIMaterial.NONE ? CMIMaterial.AIR : mat;
    }

    public static CMILocation fromString(String map) {
	return fromString(map, ";");
    }

    public static CMILocation fromString(String map, String separator) {
	CMILocation loc = null;
	if (map == null)
	    return null;
	if (!map.contains(separator))
	    return null;

	String[] split = map.replace(",", ".").split(separator);

	double x = 0;
	double y = 0;
	double z = 0;
	float yaw = 0;
	float pitch = 0;

	if (split.length > 0)
	    try {
		x = Double.parseDouble(split[1]);
	    } catch (Exception e) {
		return loc;
	    }

	if (split.length > 1)
	    try {
		y = Double.parseDouble(split[2]);
	    } catch (Exception e) {
		return loc;
	    }

	if (split.length > 2)
	    try {
		z = Double.parseDouble(split[3]);
	    } catch (Exception e) {
		return loc;
	    }

	if (split.length > 3)
	    try {
		yaw = Float.parseFloat(split[4]);
	    } catch (Exception e) {
	    }

	if (split.length > 4)
	    try {
		pitch = Float.parseFloat(split[5]);
	    } catch (Exception e) {
	    }

	World world = getWorld(split[0]);
	String worldName = world == null ? split[0] : world.getName();
	loc = new CMILocation(worldName, x, y, z);
	loc.setYaw(yaw);
	loc.setPitch(pitch);

	return loc;
    }

    private static World getWorld(String name) {
	World w = Bukkit.getWorld(name);

	if (w != null)
	    return w;

	name = name.replace("_", "").replace(".", "").replace("-", "");

	for (World one : Bukkit.getWorlds()) {
	    String n = one.getName().replace("_", "").replace(".", "").replace("-", "");
	    if (!n.equalsIgnoreCase(name))
		continue;
	    return one;
	}

	return null;
    }
}

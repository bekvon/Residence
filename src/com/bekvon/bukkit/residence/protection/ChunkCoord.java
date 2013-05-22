package com.bekvon.bukkit.residence.protection;

import org.bukkit.Location;

/**
 * Represents a coordinate of a chunk.
 * It consists of a X-coordinate and a Z-coordinate.
 * {@code ChunkCoord} class overrides {@code equals} and {@code hashCode} methods
 * so that is suitable for keys in hash table based structures.
 * {@code ChunkCoord} object is immutable.
 */
public final class ChunkCoord {

    /** The X-coordinate. */
    private final int x;

    /** The Z-coordinate. */
    private final int z;

    /**
     * Constructs a new chunk coordinate.
     * @param x the X-coordinate
     * @param z the Z-coordinate
     */
    public ChunkCoord(final int x, final int z) {
        super();
        this.x = x;
        this.z = z;
    }

    /**
     * Constructs a new chunk coordinate.
     * @param location the location
     * @return the chunk coordinate
     */
    public ChunkCoord(final Location location) {
        this(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * Returns the X-coordinate.
     * @return the X-coordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Returns the Z-coordinate.
     * @return the Z-coordinate
     */
    public int getZ() {
        return this.z;
    }

    @Override
    public int hashCode() {
        return this.x ^ this.z;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChunkCoord other = (ChunkCoord) obj;
        return this.x == other.x && this.z == other.z;
    }

}

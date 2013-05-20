package com.bekvon.bukkit.residence.protection;

import org.bukkit.Chunk;
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
    public final int x;

    /** The Z-coordinate. */
    public final int z;

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
    public static ChunkCoord valueOf(final Location location) {
        final Chunk chunk = location.getChunk();
        return new ChunkCoord(chunk.getX(), chunk.getZ());
    }

    @Override
    public int hashCode() {
        return 251 * (251 + x) + z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChunkCoord other = (ChunkCoord) obj;
        return this.x == other.x && this.z == other.z;
    }

}

package net.t00thpick1.residence.protection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import net.t00thpick1.residence.api.ResidenceManager;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.utils.immutable.ImmutableWrapperCollection;

public abstract class MemoryResidenceManager implements ResidenceManager {
    protected Map<String, Map<String, ResidenceArea>> residencesByWorld;
    protected Map<String, Map<ChunkRef, List<String>>> residenceNamesByChunk;
    protected Map<String, ResidenceArea> residencesByName;
    protected Map<UUID, ResidenceArea> residencesByUUID;

    public MemoryResidenceManager() {
        residenceNamesByChunk = new HashMap<String, Map<ChunkRef, List<String>>>();
        residencesByWorld = new HashMap<String, Map<String, ResidenceArea>>();
        residencesByName = new HashMap<String, ResidenceArea>();
        residencesByUUID = new HashMap<UUID, ResidenceArea>();
    }

    public ResidenceArea getByLocation(Location loc) {
        return getByLocation(loc, true);
    }

    public ResidenceArea getByLocation(Location loc, boolean recurse) {
        if (loc == null) {
            return null;
        }
        ResidenceArea res = null;
        boolean found = false;
        String world = loc.getWorld().getName();
        ChunkRef chunk = new ChunkRef(loc);
        if (residenceNamesByChunk.get(world).get(chunk) != null) {
            for (String key : residenceNamesByChunk.get(world).get(chunk)) {
                ResidenceArea entry = residencesByWorld.get(world).get(key);
                if (entry.containsLocation(loc)) {
                    res = entry;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            return null;
        }
        if (!recurse) {
            return res;
        }
        ResidenceArea subres = res.getSubzoneByLocation(loc);
        if (subres == null) {
            return res;
        }
        return subres;
    }

    public ResidenceArea getByName(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase();
        String[] split = name.split("\\.");
        if (split.length == 1) {
            return residencesByName.get(name);
        }
        ResidenceArea res = residencesByName.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (res != null) {
                res = res.getSubzoneByName(split[i]);
            } else {
                return null;
            }
        }
        return res;
    }

    @Override
    public ResidenceArea getByUUID(UUID uuid) {
        return residencesByUUID.get(uuid);
    }

    @Override
    public Collection<ResidenceArea> getResidencesInWorld(World world) {
        return new ImmutableWrapperCollection<ResidenceArea>(residencesByWorld.get(world.getName()).values());
    }

    public void removeChunkList(ResidenceArea res) {
        String world = res.getWorld().getName();
        if (residenceNamesByChunk.get(world) != null) {
            for (ChunkRef chunk : ((MemoryResidenceArea) res).getChunks()) {
                List<String> ress = new ArrayList<String>();
                if (residenceNamesByChunk.get(world).containsKey(chunk)) {
                    ress.addAll(residenceNamesByChunk.get(world).get(chunk));
                }
                ress.remove(res.getName().toLowerCase());
                residenceNamesByChunk.get(world).put(chunk, ress);
            }
        }
    }

    public void calculateChunks(ResidenceArea res) {
        calculateChunks(res, res.getName());
    }

    public void calculateChunks(ResidenceArea res, String name) {
        String world = res.getWorld().getName();
        if (residenceNamesByChunk.get(world) == null) {
            residenceNamesByChunk.put(world, new HashMap<ChunkRef, List<String>>());
        }
        for (ChunkRef chunk : ((MemoryResidenceArea) res).getChunks()) {
            List<String> ress = new ArrayList<String>();
            if (residenceNamesByChunk.get(world).containsKey(chunk)) {
                ress.addAll(residenceNamesByChunk.get(world).get(chunk));
            }
            ress.add(name.toLowerCase());
            residenceNamesByChunk.get(world).put(chunk, ress);
        }
    }

    public static final class ChunkRef {
        public static int getBase(final int val) {
            // get chunk base
            return val >> 4;
        }

        private final int z;
        private final int x;

        public ChunkRef(Location loc) {
            this.x = getBase(loc.getBlockX());
            this.z = getBase(loc.getBlockZ());
        }

        public ChunkRef(int x, int z) {
            this.x = (int) x;
            this.z = (int) z;
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
            ChunkRef other = (ChunkRef) obj;
            return this.x == other.x && this.z == other.z;
        }

        public int hashCode() {
            return x ^ z;
        }
    }
}

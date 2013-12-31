package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class ResidenceManager {
    private Map<String, ClaimedResidence> residences;
    private Map<String, Map<ChunkRef, List<String>>> residenceNamesByChunk;

    public ResidenceManager() {
        residences = new HashMap<String, ClaimedResidence>();
        residenceNamesByChunk = new HashMap<String, Map<ChunkRef, List<String>>>();
    }

    public void cleanUp() {
        residences = null;
        residenceNamesByChunk = null;
    }

    public ClaimedResidence getByLoc(Location loc) {
        if (loc == null) {
            return null;
        }
        ClaimedResidence res = null;
        boolean found = false;
        String world = loc.getWorld().getName();
        ChunkRef chunk = new ChunkRef(loc);
        if (residenceNamesByChunk.get(world) != null) {
            if (residenceNamesByChunk.get(world).get(chunk) != null) {
                for (String key : residenceNamesByChunk.get(world).get(chunk)) {
                    ClaimedResidence entry = residences.get(key);
                    if (entry.containsLoc(loc)) {
                        res = entry;
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!found) {
            return null;
        }
        ClaimedResidence subres = res.getSubzoneByLoc(loc);
        if (subres == null) {
            return res;
        }
        return subres;
    }

    public ClaimedResidence getByName(String name) {
        if (name == null) {
            return null;
        }
        String[] split = name.split("\\.");
        if (split.length == 1) {
            return residences.get(name);
        }
        ClaimedResidence res = residences.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (res != null) {
                res = res.getSubzone(split[i]);
            } else {
                return null;
            }
        }
        return res;
    }

    public boolean createResidence(String name, Location loc1, Location loc2) {
        return this.createResidence(name, "Server Land", loc1, loc2);
    }

    public boolean createResidence(String name, String owner, Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || !loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        if (residences.containsKey(name)) {
            return false;
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        ClaimedResidence newRes = new ClaimedResidence(owner, name, newArea);
        residences.put(name, newRes);
        calculateChunks(newRes);
        return true;
    }

    public int getOwnedZoneCount(String player) {
        Collection<ClaimedResidence> set = residences.values();
        int count = 0;
        for (ClaimedResidence res : set) {
            if (res.getPermissions().getOwner().equalsIgnoreCase(player)) {
                count++;
            }
        }
        return count;
    }

    public boolean rename(ClaimedResidence res, String newName) {
        if (res.getParent() != null) {
            return res.rename(newName);
        }

        if (residences.get(newName) != null) {
            return false;
        }

        removeChunkList(res);
        residences.remove(res.getName());
        residences.put(newName, res);
        calculateChunks(res);
        return true;
    }

    public boolean removeAllFromWorld(String world) {
        boolean removed = false;
        Iterator<ClaimedResidence> it = residences.values().iterator();
        while (it.hasNext()) {
            ClaimedResidence next = it.next();
            if (next.getWorld().equals(world)) {
                it.remove();
                removed = true;
            }
        }
        residenceNamesByChunk.remove(world);
        residenceNamesByChunk.put(world, new HashMap<ChunkRef, List<String>>());
        return removed;
    }

    public void remove(ClaimedResidence res) {
        if (res.getParent() == null) {
            removeChunkList(res);
            residences.remove(res.getName());
        } else {
            res.getParent().removeSubzone(res.getName());
        }
    }

    public void removeChunkList(ClaimedResidence res) {
        String world = res.getWorld().getName();
        if (residenceNamesByChunk.get(world) != null) {
            for (ChunkRef chunk : res.getChunks()) {
                List<String> ress = new ArrayList<String>();
                if (residenceNamesByChunk.get(world).containsKey(chunk)) {
                    ress.addAll(residenceNamesByChunk.get(world).get(chunk));
                }
                ress.remove(res.getName());
                residenceNamesByChunk.get(world).put(chunk, ress);
            }
        }
    }

    public void calculateChunks(ClaimedResidence res) {
        String world = res.getWorld().getName();
        if (residenceNamesByChunk.get(world) == null) {
            residenceNamesByChunk.put(world, new HashMap<ChunkRef, List<String>>());
        }
        for (ChunkRef chunk : res.getChunks()) {
            List<String> ress = new ArrayList<String>();
            if (residenceNamesByChunk.get(world).containsKey(chunk)) {
                ress.addAll(residenceNamesByChunk.get(world).get(chunk));
            }
            ress.add(res.getName());
            residenceNamesByChunk.get(world).put(chunk, ress);
        }
    }

    public static final class ChunkRef {
        public static int getBase(final int val) {
            // >> 4 << 4 will convert any block coordinate to its chunk coordinate base,
            // negatives need to go up in value so -16 first
            // so -35 will go to -48, 18 will be 16
            return (val < 0 ? val - 16 : val) >> 4 << 4;
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

    public static ResidenceManager load(Map<String, Object> root) throws Exception {
        ResidenceManager resm = new ResidenceManager();
        if (root == null) {
            return resm;
        }
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            Map<String, Object> reslist = (Map<String, Object>) root.get(world.getName());
            if (reslist != null) {
                try {
                    resm.residenceNamesByChunk.put(world.getName(), loadMap(reslist, resm));
                } catch (Exception ex) {
                    Residence.getInstance().getLogger().severe("Error in loading save file for world: " + world.getName());
                    if (Residence.getInstance().getConfigManager().stopOnSaveError()) {
                        throw (ex);
                    }
                }
            }
        }
        return resm;
    }

    public static Map<ChunkRef, List<String>> loadMap(Map<String, Object> root, ResidenceManager resm) throws Exception {
        Map<ChunkRef, List<String>> retRes = new HashMap<ChunkRef, List<String>>();
        if (root != null) {
            for (Entry<String, Object> res : root.entrySet()) {
                try {
                    ClaimedResidence residence = ClaimedResidence.load((Map<String, Object>) res.getValue(), null);
                    for (ChunkRef chunk : residence.getChunks()) {
                        List<String> ress = new ArrayList<String>();
                        if (retRes.containsKey(chunk)) {
                            ress.addAll(retRes.get(chunk));
                        }
                        ress.add(res.getKey());
                        retRes.put(chunk, ress);
                    }
                    resm.residences.put(res.getKey(), residence);
                } catch (Exception ex) {
                    Residence.getInstance().getLogger().severe("Failed to load residence (" + res.getKey() + ")! Reason:" + ex.getMessage() + " Error Log:");
                    Residence.getInstance().getLogger().log(Level.SEVERE, null, ex);
                    if (Residence.getInstance().getConfigManager().stopOnSaveError()) {
                        throw (ex);
                    }
                }
            }
        }
        return retRes;
    }

    public Map<String, Object> save() {
        Map<String, Object> worldmap = new LinkedHashMap<String, Object>();
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            Map<String, Object> resmap = new LinkedHashMap<String, Object>();
            for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
                if (res.getValue().getWorld().equals(world.getName())) {
                    try {
                        resmap.put(res.getKey(), res.getValue().save());
                    } catch (Exception ex) {
                        Residence.getInstance().getLogger().severe("Failed to save residence (" + res.getKey() + ")!");
                        Residence.getInstance().getLogger().log(Level.SEVERE, null, ex);
                    }
                }
            }
            worldmap.put(world.getName(), resmap);
        }
        return worldmap;
    }
}

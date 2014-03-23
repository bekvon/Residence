package net.t00thpick1.residence.protection;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.flags.move.StateAssurance;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ResidenceManager {
    private Map<String, ClaimedResidence> residences;
    private Map<String, Map<ChunkRef, List<String>>> residenceNamesByChunk;
    private Map<String, FileConfiguration> worldFiles;
    private File worldFolder;

    public ResidenceManager() {
        residenceNamesByChunk = new HashMap<String, Map<ChunkRef, List<String>>>();
    }

    public void cleanUp() {
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
                    if (entry.containsLocation(loc)) {
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

    public boolean createResidence(String name, CuboidArea area) {
        return createResidence(name, "Server Land", area);
    }

    public boolean createResidence(String name, String owner, CuboidArea area) {
        if (area == null) {
            return false;
        }
        if (residences.containsKey(name)) {
            return false;
        }

        try {
            ConfigurationSection res = worldFiles.get(area.getWorld().getName()).createSection(name);
            ConfigurationSection data = res.createSection("Data");
            data.set("Owner", owner);
            data.set("CreationDate", System.currentTimeMillis());
            data.set("EnterMessage", GroupManager.getDefaultEnterMessage(owner));
            data.set("LeaveMessage", GroupManager.getDefaultLeaveMessage(owner));
            area.saveArea(data.createSection("Area"));
            ConfigurationSection marketData = data.createSection("MarketData");
            marketData.set("ForSale", false);
            marketData.set("ForRent", false);
            marketData.set("Cost", 0);
            marketData.set("IsAutoRenew", ConfigManager.getInstance().isAutoRenewDefault());
            res.createSection("Flags");
            res.createSection("Groups");
            res.createSection("Players");
            res.createSection("Subzones");
            ClaimedResidence newRes = new ClaimedResidence(res, null);
            residences.put(name, newRes);
            calculateChunks(newRes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getOwnedZoneCount(String player) {
        Collection<ClaimedResidence> set = residences.values();
        int count = 0;
        for (ClaimedResidence res : set) {
            if (res.getOwner().equalsIgnoreCase(player)) {
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
        FileConfiguration file = worldFiles.get(res.getWorld());
        file.createSection(res.getName(), file.getConfigurationSection(res.getName()).getValues(true));
        file.set(res.getName(), null);
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
            for (Player player : res.getPlayersInResidence()) {
                StateAssurance.handleNewLocation(player, player.getLocation());
            }
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

    public static ResidenceManager load(File worldFolder) throws Exception {
        ResidenceManager resm = new ResidenceManager();
        resm.worldFolder = worldFolder;
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            File worldFile = new File(worldFolder, "res_" + world.getName() + ".yml");
            boolean newFile = false;
            if (!worldFile.isFile()) {
                worldFile.createNewFile();
                newFile = true;
            }

            FileConfiguration worldSave = YamlConfiguration.loadConfiguration(worldFile);
            resm.worldFiles.put(world.getName(), worldSave);
            if (newFile) {
                worldSave.set("Version", Residence.saveVersion);
                worldSave.set("Seed", world.getSeed());
                worldSave.createSection("Residences");
            }
            try {
                resm.residenceNamesByChunk.put(world.getName(), loadWorld(worldSave.getConfigurationSection("Residences"), resm));
            } catch (Exception ex) {
                Residence.getInstance().getLogger().severe("Error in loading save file for world: " + world.getName());
                if (ConfigManager.getInstance().stopOnLoadError()) {
                    throw (ex);
                }
            }
        }
        return resm;
    }

    public static Map<ChunkRef, List<String>> loadWorld(ConfigurationSection section, ResidenceManager resm) throws Exception {
        Map<ChunkRef, List<String>> retRes = new HashMap<ChunkRef, List<String>>();
        for (String res : section.getKeys(false)) {
            try {
                ClaimedResidence residence = new ClaimedResidence(section.getConfigurationSection(res), null);
                for (ChunkRef chunk : residence.getChunks()) {
                    List<String> ress = new ArrayList<String>();
                    if (retRes.containsKey(chunk)) {
                        ress.addAll(retRes.get(chunk));
                    }
                    ress.add(res);
                    retRes.put(chunk, ress);
                }
                resm.residences.put(res, residence);
            } catch (Exception ex) {
                Residence.getInstance().getLogger().severe("Failed to load residence (" + res + ")! Reason:" + ex.getMessage() + " Error Log:");
                Residence.getInstance().getLogger().log(Level.SEVERE, null, ex);
                if (ConfigManager.getInstance().stopOnLoadError()) {
                    throw (ex);
                }
            }
        }
        return retRes;
    }

    public void save() throws IOException {
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            File saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
            if (!saveFile.isFile()) {
                saveFile.createNewFile();
            }
            FileConfiguration worldSave = worldFiles.get(world.getName());
            worldSave.save(saveFile);
        }
    }
}

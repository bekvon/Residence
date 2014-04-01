package net.t00thpick1.residence.protection.yaml;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceManager;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.listeners.StateAssurance;
import net.t00thpick1.residence.utils.zip.ZipLibrary;

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

public class YAMLResidenceManager implements ResidenceManager {
    private Map<String, ResidenceArea> residences;
    private Map<String, Map<ChunkRef, List<String>>> residenceNamesByChunk;
    private Map<String, FileConfiguration> worldFiles;
    private File worldFolder;

    public YAMLResidenceManager(File dataFolder) throws Exception {
        residenceNamesByChunk = new HashMap<String, Map<ChunkRef, List<String>>>();
        residences = new HashMap<String, ResidenceArea>();
        worldFiles = new HashMap<String, FileConfiguration>();
        worldFolder = new File(new File(dataFolder, "Save"), "Worlds");
        if (!worldFolder.isDirectory()) {
            worldFolder.mkdirs();
        }
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            File worldFile = new File(worldFolder, "res_" + world.getName() + ".yml");
            boolean newFile = false;
            if (!worldFile.isFile()) {
                worldFile.createNewFile();
                newFile = true;
            }

            FileConfiguration worldSave = YamlConfiguration.loadConfiguration(worldFile);
            worldFiles.put(world.getName(), worldSave);
            if (newFile) {
                worldSave.set("Version", Residence.saveVersion);
                worldSave.set("Seed", world.getSeed());
                worldSave.createSection("Residences");
            }
            try {
                residenceNamesByChunk.put(world.getName(), loadWorld(worldSave.getConfigurationSection("Residences")));
            } catch (Exception ex) {
                Residence.getInstance().getLogger().severe("Error in loading save file for world: " + world.getName());
                if (ConfigManager.getInstance().stopOnLoadError()) {
                    throw (ex);
                }
            }
        }
    }

    public Map<ChunkRef, List<String>> loadWorld(ConfigurationSection section) throws Exception {
        Map<ChunkRef, List<String>> retRes = new HashMap<ChunkRef, List<String>>();
        for (String res : section.getKeys(false)) {
            try {
                YAMLResidenceArea residence = new YAMLResidenceArea(section.getConfigurationSection(res), null);
                for (ChunkRef chunk : residence.getChunks()) {
                    List<String> ress = new ArrayList<String>();
                    if (retRes.containsKey(chunk)) {
                        ress.addAll(retRes.get(chunk));
                    }
                    ress.add(res);
                    retRes.put(chunk, ress);
                }
                residences.put(res, residence);
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

    public ResidenceArea getByLocation(Location loc) {
        if (loc == null) {
            return null;
        }
        ResidenceArea res = null;
        boolean found = false;
        String world = loc.getWorld().getName();
        ChunkRef chunk = new ChunkRef(loc);
        if (residenceNamesByChunk.get(world) != null) {
            if (residenceNamesByChunk.get(world).get(chunk) != null) {
                for (String key : residenceNamesByChunk.get(world).get(chunk)) {
                    ResidenceArea entry = residences.get(key);
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
        String[] split = name.split("\\.");
        if (split.length == 1) {
            return residences.get(name);
        }
        ResidenceArea res = residences.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (res != null) {
                res = res.getSubzoneByName(split[i]);
            } else {
                return null;
            }
        }
        return res;
    }

    public boolean createResidence(String name, CuboidArea area) {
        return createResidence(name, null, area);
    }

    public boolean createResidence(String name, String owner, CuboidArea area) {
        if (area == null) {
            return false;
        }
        if (residences.containsKey(name)) {
            return false;
        }

        try {
            ConfigurationSection res = worldFiles.get(area.getWorld().getName()).getConfigurationSection("Residences").createSection(name);
            YAMLResidenceArea newRes = new YAMLResidenceArea(res, (YAMLCuboidArea) area, owner, null);
            residences.put(name, newRes);
            calculateChunks(newRes);
            newRes.applyDefaultFlags();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getOwnedZoneCount(String player) {
        Collection<ResidenceArea> set = residences.values();
        int count = 0;
        for (ResidenceArea res : set) {
            if (res.getOwner().equals(player)) {
                count++;
            }
        }
        return count;
    }

    public List<ResidenceArea> getOwnedResidences(String player) {
        Collection<ResidenceArea> set = residences.values();
        List<ResidenceArea> owned = new ArrayList<ResidenceArea>();
        for (ResidenceArea res : set) {
            if (res.getOwner().equals(player)) {
                owned.add(res);
            }
        }
        return owned;
    }

    public boolean rename(YAMLResidenceArea res, String newName) {
        if (res.getParent() != null) {
            return res.rename(newName);
        }

        if (residences.get(newName) != null) {
            return false;
        }

        removeChunkList(res);
        residences.remove(res.getName());
        FileConfiguration file = worldFiles.get(res.getWorld().getName());
        ConfigurationSection residenceSection = file.getConfigurationSection("Residences");
        res.newSection(residenceSection.createSection(newName));
        residenceSection.set(res.getName(), null);
        residences.put(newName, res);
        calculateChunks(res);
        return true;
    }

    public boolean removeAllFromWorld(String world) {
        boolean removed = false;
        Iterator<ResidenceArea> it = residences.values().iterator();
        while (it.hasNext()) {
            ResidenceArea next = it.next();
            if (next.getWorld().equals(world)) {
                it.remove();
                removed = true;
            }
        }
        residenceNamesByChunk.remove(world);
        residenceNamesByChunk.put(world, new HashMap<ChunkRef, List<String>>());
        FileConfiguration file = worldFiles.get(world);
        file.set("Residences", null);
        file.createSection("Residences");
        return removed;
    }

    public void remove(ResidenceArea res) {
        if (res.getParent() == null) {
            FileConfiguration file = worldFiles.get(res.getWorld().getName());
            ConfigurationSection residenceSection = file.getConfigurationSection("Residences");
            residenceSection.set(res.getName(), null);
            removeChunkList(res);
            residences.remove(res.getName());
            for (Player player : res.getPlayersInResidence()) {
                StateAssurance.getLastOutsideLocation(player.getName()).zero().add(player.getLocation());
            }
        } else {
            res.getParent().removeSubzone(res.getName());
        }
    }

    private void removeChunkList(ResidenceArea res) {
        String world = res.getWorld().getName();
        if (residenceNamesByChunk.get(world) != null) {
            for (ChunkRef chunk : ((YAMLResidenceArea) res).getChunks()) {
                List<String> ress = new ArrayList<String>();
                if (residenceNamesByChunk.get(world).containsKey(chunk)) {
                    ress.addAll(residenceNamesByChunk.get(world).get(chunk));
                }
                ress.remove(res.getName());
                residenceNamesByChunk.get(world).put(chunk, ress);
            }
        }
    }

    public void calculateChunks(YAMLResidenceArea res) {
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

    public void save() throws IOException {
        for (ResidenceArea residence : residences.values()) {
            ((YAMLResidenceArea) residence).save();
        }
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            File saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
            if (!saveFile.isFile()) {
                saveFile.createNewFile();
            }

            FileConfiguration worldSave = worldFiles.get(world.getName());
            worldSave.save(saveFile);
        }
        ZipLibrary.backup();
    }

    public void newWorld(World world) {
        if (worldFiles.get(world.getName()) != null) {
            return;
        }
        File saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
        if (!saveFile.isFile()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration worldSave = worldFiles.get(world.getName());
        worldSave = YamlConfiguration.loadConfiguration(saveFile);
        worldSave.set("Version", Residence.saveVersion);
        worldSave.set("Seed", world.getSeed());
        worldSave.createSection("Residences");
        residenceNamesByChunk.put(world.getName(), new HashMap<ChunkRef, List<String>>());
    }
}

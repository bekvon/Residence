package net.t00thpick1.residence.protection.yaml;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.listeners.StateAssurance;
import net.t00thpick1.residence.protection.MemoryResidenceManager;
import net.t00thpick1.residence.utils.zip.ZipLibrary;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class YAMLResidenceManager extends MemoryResidenceManager {
    private Map<String, FileConfiguration> worldFiles;
    private File worldFolder;

    public YAMLResidenceManager(File dataFolder) throws Exception {
        super();
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
                residenceNamesByChunk.put(world.getName(), loadWorld(world.getName(), worldSave.getConfigurationSection("Residences")));
            } catch (Exception ex) {
                Residence.getInstance().getLogger().severe("Error in loading save file for world: " + world.getName());
                if (ConfigManager.getInstance().stopOnLoadError()) {
                    throw (ex);
                }
            }
        }
    }

    public Map<ChunkRef, List<String>> loadWorld(String world, ConfigurationSection section) throws Exception {
        Map<ChunkRef, List<String>> retRes = new HashMap<ChunkRef, List<String>>();
        Map<String, ResidenceArea> residences = new HashMap<String, ResidenceArea>();
        for (String res : section.getKeys(false)) {
            try {
                YAMLResidenceArea residence = new YAMLResidenceArea(section.getConfigurationSection(res), null);
                for (ChunkRef chunk : residence.getChunks()) {
                    List<String> ress = new ArrayList<String>();
                    if (retRes.containsKey(chunk)) {
                        ress.addAll(retRes.get(chunk));
                    }
                    ress.add(res.toLowerCase());
                    retRes.put(chunk, ress);
                }
                this.residences.put(res.toLowerCase(), residence);
                residences.put(res.toLowerCase(), residence);
            } catch (Exception ex) {
                Residence.getInstance().getLogger().severe("Failed to load residence (" + res + ")! Reason:" + ex.getMessage() + " Error Log:");
                Residence.getInstance().getLogger().log(Level.SEVERE, null, ex);
                if (ConfigManager.getInstance().stopOnLoadError()) {
                    throw (ex);
                }
            }
        }
        residencesByWorld.put(world, residences);
        return retRes;
    }

    public boolean createResidence(String name, CuboidArea area) {
        return createResidence(name, null, area);
    }

    public boolean createResidence(String name, String owner, CuboidArea area) {
        if (area == null) {
            return false;
        }
        if (residences.containsKey(name.toLowerCase())) {
            return false;
        }

        try {
            ConfigurationSection res = worldFiles.get(area.getWorld().getName()).getConfigurationSection("Residences").createSection(name);
            YAMLResidenceArea newRes = new YAMLResidenceArea(res, area, owner, null);
            residencesByWorld.get(area.getWorld().getName()).put(name.toLowerCase(), newRes);
            residences.put(name.toLowerCase(), newRes);
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
        residencesByWorld.get(res.getWorld().getName()).remove(res.getName().toLowerCase());
        residences.remove(res.getName().toLowerCase());
        FileConfiguration file = worldFiles.get(res.getWorld().getName());
        ConfigurationSection residenceSection = file.getConfigurationSection("Residences");
        res.newSection(residenceSection.createSection(newName));
        residenceSection.set(res.getName(), null);
        residencesByWorld.get(res.getWorld().getName()).put(newName.toLowerCase(), res);
        residences.put(newName.toLowerCase(), res);
        calculateChunks(res, newName);
        return true;
    }

    public void removeAllFromWorld(String world) {
        for (String res : residencesByWorld.get(world).keySet()) {
            residences.remove(res);
        }
        residencesByWorld.remove(world);
        residencesByWorld.put(world, new HashMap<String, ResidenceArea>());
        residenceNamesByChunk.remove(world);
        residenceNamesByChunk.put(world, new HashMap<ChunkRef, List<String>>());
        FileConfiguration file = worldFiles.get(world);
        file.set("Residences", null);
        file.createSection("Residences");
    }

    public void remove(ResidenceArea res) {
        if (res.getParent() == null) {
            FileConfiguration file = worldFiles.get(res.getWorld().getName());
            ConfigurationSection residenceSection = file.getConfigurationSection("Residences");
            residenceSection.set(res.getName(), null);
            removeChunkList(res);
            residencesByWorld.get(res.getWorld().getName()).remove(res.getName().toLowerCase());
            residences.remove(res.getName().toLowerCase());
            for (Player player : res.getPlayersInResidence()) {
                StateAssurance.getLastOutsideLocation(player.getName()).zero().add(player.getLocation());
            }
        } else {
            res.getParent().removeSubzone(res.getName());
        }
    }

    public void save() throws IOException {

        for (World world : Residence.getInstance().getServer().getWorlds()) {
            File saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
            if (!saveFile.isFile()) {
                saveFile.createNewFile();
            }
            Map<String, ResidenceArea> residences = residencesByWorld.get(world.getName());

            for (ResidenceArea residence : residences.values()) {
                try {
                    ((YAMLResidenceArea) residence).save();
                } catch (Exception e) {
                    Residence.getInstance().getLogger().log(Level.SEVERE, "Failed to save residence (" + residence.getFullName() + ")! Reason:" + e.getMessage() + " Error Log:", e);
                }
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
        worldFiles.put(world.getName(), worldSave);
        residenceNamesByChunk.put(world.getName(), new HashMap<ChunkRef, List<String>>());
        residencesByWorld.put(world.getName(), new HashMap<String, ResidenceArea>());
    }
}

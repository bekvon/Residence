package net.t00thpick1.residence.protection.yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.WorldManager;
import net.t00thpick1.residence.api.areas.WorldArea;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YAMLWorldManager implements WorldManager {
    private Map<String, WorldArea> worldAreas;
    private File worldFolder;

    public YAMLWorldManager(File worldFolder) throws IOException {
        this.worldFolder = worldFolder;
        worldAreas = new HashMap<String, WorldArea>();
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            File worldFile = new File(worldFolder, world.getName() + "_configuration.yml");
            boolean newFile = false;
            if (!worldFile.isFile()) {
                worldFile.createNewFile();
                newFile = true;
            }

            FileConfiguration worldSave = YamlConfiguration.loadConfiguration(worldFile);
            if (newFile) {
                worldSave.set("World", world.getName());
            }
            worldAreas.put(world.getName(), new YAMLWorldArea(worldSave, worldFile, world));
        }
    }

    public WorldArea getResidenceWorld(World world) {
        WorldArea area = worldAreas.get(world.getName());
        if (area == null) {
            File worldFile = new File(worldFolder, world.getName() + "_configuration.yml");
            if (!worldFile.isFile()) {
                try {
                    worldFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileConfiguration worldSave = YamlConfiguration.loadConfiguration(worldFile);
            area = new YAMLWorldArea(worldSave, worldFile, world);
            worldAreas.put(world.getName(), area);
            ((YAMLResidenceManager) ResidenceAPI.getResidenceManager()).newWorld(world);
        }
        return area;
    }

    public void save() throws IOException {
        for (WorldArea area: worldAreas.values()) {
            ((YAMLWorldArea) area).save();
        }
    }
}

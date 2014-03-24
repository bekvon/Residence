package net.t00thpick1.residence.protection.yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.t00thpick1.residence.Residence;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YAMLWorldManager {
    private Map<String, YAMLWorldArea> worldAreas;

    public YAMLWorldManager(File worldFolder) throws IOException {
        worldAreas = new HashMap<String, YAMLWorldArea>();
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
            worldAreas.put(world.getName(), new YAMLWorldArea(worldSave, worldFile));
        }
    }

    public YAMLWorldArea getResidenceWorld(World world) {
        return worldAreas.get(world.getName());
    }

    public void save() throws IOException {
        for (YAMLWorldArea area: worldAreas.values()) {
            area.save();
        }
    }
}

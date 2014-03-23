package net.t00thpick1.residence;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.t00thpick1.residence.protection.WorldArea;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WorldManager {
    private Map<String, WorldArea> worldAreas;

    public WorldManager(File worldFolder) throws IOException {
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
            worldAreas.put(world.getName(), new WorldArea(worldSave, worldFile));
        }
    }

    public WorldArea getResidenceWorld(World world) {
        return worldAreas.get(world.getName());
    }

    public void save() throws IOException {
        for (WorldArea area: worldAreas.values()) {
            area.save();
        }
    }
}

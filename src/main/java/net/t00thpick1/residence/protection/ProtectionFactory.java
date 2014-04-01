package net.t00thpick1.residence.protection;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.EconomyManager;
import net.t00thpick1.residence.api.ResidenceManager;
import net.t00thpick1.residence.api.UsernameUUIDCache;
import net.t00thpick1.residence.api.WorldManager;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.protection.yaml.YAMLCuboidArea;
import net.t00thpick1.residence.protection.yaml.YAMLGroupManager;
import net.t00thpick1.residence.protection.yaml.YAMLResidenceManager;
import net.t00thpick1.residence.protection.yaml.YAMLUsernameUUIDCache;
import net.t00thpick1.residence.protection.yaml.YAMLWorldManager;

public class ProtectionFactory {
    private static UsernameUUIDCache usernameUUIDCache;
    private static ResidenceManager residenceManager;
    private static CuboidAreaFactory cuboidAreaFactory;
    private static WorldManager worldManager;
    private static EconomyManager economyManager;

    public static void init(Residence residence) throws Exception {
        switch (residence.getBackend()) {
            case MYSQL:
                throw new UnsupportedOperationException();
            case WORLDGUARD:
                throw new UnsupportedOperationException();
            case YAML:
                File dataFolder = residence.getDataFolder();
                residenceManager = new YAMLResidenceManager(dataFolder);
                usernameUUIDCache = new YAMLUsernameUUIDCache(new File(dataFolder, "UsernameUUIDCache.yml"));
                economyManager = new MemoryEconomyManager();
                File groupsFile = new File(dataFolder, "groups.yml");
                if (!groupsFile.isFile()) {
                    groupsFile.createNewFile();
                    FileConfiguration internalConfig = YamlConfiguration.loadConfiguration(residence.getResource("groups.yml"));
                    internalConfig.save(groupsFile);
                }

                YAMLGroupManager.init(YamlConfiguration.loadConfiguration(groupsFile));
                File worldFolder = new File(dataFolder, "WorldConfigurations");
                if (!worldFolder.isDirectory()) {
                    worldFolder.mkdirs();
                }
                worldManager = new YAMLWorldManager(worldFolder);
                cuboidAreaFactory = new YAMLCuboidArea.YAMLCuboidAreaFactory();
        }
    }

    public static CuboidArea createNewCuboidArea(Location loc1, Location loc2) {
        return cuboidAreaFactory.createArea(loc1, loc2);
    }

    public static ResidenceManager getResidenceManager() {
        return residenceManager;
    }

    public static WorldManager getWorldManager() {
        return worldManager;
    }

    public static EconomyManager getEconomyManager() {
        return economyManager;
    }

    public static UsernameUUIDCache getUsernameUUIDCache() {
        return usernameUUIDCache;
    }

    public static void save() {
        try {
            residenceManager.save();
            usernameUUIDCache.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

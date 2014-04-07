package net.t00thpick1.residence.protection.yaml;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.protection.MemoryCuboidArea;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class YAMLCuboidAreaSerializer {
    public static CuboidArea deserialize(ConfigurationSection section) throws Exception {
        String worldName = section.getString("World");
        World world = Residence.getInstance().getServer().getWorld(worldName);
        if (world == null) {
            throw new Exception("Cant Find World: " + worldName);
        }
        int highX = section.getInt("X1");
        int highY = section.getInt("Y1");
        int highZ = section.getInt("Z1");
        int lowX = section.getInt("X2");
        int lowY = section.getInt("Y2");
        int lowZ = section.getInt("Z2");
        return new MemoryCuboidArea(world, highX, highY, highZ, lowX, lowY, lowZ);
    }

    public static void serialize(CuboidArea area, ConfigurationSection section) {
        section.set("World", area.getWorld().getName());
        section.set("X1", area.getHighX());
        section.set("Y1", area.getHighY());
        section.set("Z1", area.getHighZ());
        section.set("X2", area.getLowX());
        section.set("Y2", area.getLowY());
        section.set("Z2", area.getLowZ());
    }
}

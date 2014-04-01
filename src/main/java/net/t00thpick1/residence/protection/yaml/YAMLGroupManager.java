package net.t00thpick1.residence.protection.yaml;

import java.util.HashMap;
import java.util.Map;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class YAMLGroupManager {

    private static FileConfiguration config;

    public static void init(FileConfiguration groupConfig) {
        config = groupConfig;
    }

    public static String getPlayerGroup(String player) {
        return Residence.getInstance().getPermissions().getPrimaryGroup((String) null, player);
    }

    public static ConfigurationSection getGroup(Player player) {
        return getGroup(player.getName());
    }

    public static ConfigurationSection getGroup(String player) {
        if (!config.isConfigurationSection(getPlayerGroup(player))) {
            return config.getConfigurationSection("DefaultSettings");
        }
        return config.getConfigurationSection(getPlayerGroup(player));
    }

    public static double getCostPerBlock(Player player) {
        return getGroup(player).getDouble("CostPerBlock");
    }

    public static int getMaxHeight(Player player) {
        return getGroup(player).getInt("MaxHeight");
    }

    public static int getMaxY(Player player) {
        return getGroup(player).getInt("MaxY");
    }

    public static int getMinHeight(Player player) {
        return getGroup(player).getInt("MinHeight");
    }

    public static int getMinY(Player player) {
        return getGroup(player).getInt("MinY");
    }

    public static int getMaxWidth(Player player) {
        return getGroup(player).getInt("MaxWidth");
    }

    public static int getMinWidth(Player player) {
        return getGroup(player).getInt("MinWidth");
    }

    public static int getMaxLength(Player player) {
        return getGroup(player).getInt("MaxLength");
    }

    public static int getMinLength(Player player) {
        return getGroup(player).getInt("MinLength");
    }

    public static int getMaxResidences(String player) {
        return getGroup(player).getInt("MaxResidences");
    }

    public static Map<Flag, Boolean> getDefaultAreaFlags(String player) {
        Map<Flag, Boolean> defFlags = new HashMap<Flag, Boolean>();
        ConfigurationSection flags = getGroup(player).getConfigurationSection("AreaFlags");
        for (String key : flags.getKeys(false)) {
            Flag flag = FlagManager.getFlag(key);
            if (flag != null) {
                defFlags.put(flag, flags.getBoolean(key));
            }
        }
        return defFlags;
    }

    public static Map<Flag, Boolean> getDefaultOwnerFlags(String player) {
        Map<Flag, Boolean> defFlags = new HashMap<Flag, Boolean>();
        ConfigurationSection flags = getGroup(player).getConfigurationSection("OwnerFlags");
        for (String key : flags.getKeys(false)) {
            Flag flag = FlagManager.getFlag(key);
            if (flag != null) {
                defFlags.put(flag, flags.getBoolean(key));
            }
        }
        return defFlags;
    }

    public static String getDefaultEnterMessage(String owner) {
        return getGroup(owner).getString("DefaultEnterMessage");
    }

    public static String getDefaultLeaveMessage(String owner) {
        return getGroup(owner).getString("DefaultLeaveMessage");
    }
}

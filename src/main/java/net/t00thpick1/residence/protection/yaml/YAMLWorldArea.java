package net.t00thpick1.residence.protection.yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.areas.WorldArea;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class YAMLWorldArea implements WorldArea {
    private World world;
    private ConfigurationSection perms;
    private ConfigurationSection groups;
    private File saveFile;
    private FileConfiguration file;

    public YAMLWorldArea(FileConfiguration section, File worldFile) {
        this.world = Residence.getInstance().getServer().getWorld(section.getString("World"));
        if (!section.isConfigurationSection("Permissions")) {
            this.perms = section.createSection("Permissions");
        } else {
            this.perms = section.getConfigurationSection("Permissions");
        }
        if (!section.isConfigurationSection("Groups")) {
            this.groups = section.createSection("Groups");
        } else {
            this.groups = section.getConfigurationSection("Groups");
        }
        this.saveFile = worldFile;
        this.file = section;
    }

    @Override
    public boolean allowAction(Flag flag) {
        if (perms.contains(flag.getName())) {
            return perms.getBoolean(flag.getName());
        }
        if (flag.getParent() != null) {
            return allowAction(flag.getParent());
        }
        return true;
    }

    @Override
    public boolean allowAction(String player, Flag flag) {
        String group = YAMLGroupManager.getPlayerGroup(player);
        while (true) {
            if (groups.isConfigurationSection(group)) {
                ConfigurationSection groupPerms = groups.getConfigurationSection(group);
                if (groupPerms.contains(flag.getName())) {
                    return groupPerms.getBoolean(flag.getName());
                }
            }
            if (perms.contains(flag.getName())) {
                return perms.getBoolean(flag.getName());
            }
            if (flag.getParent() == null) {
                return true;
            } else {
                flag = flag.getParent();
            }
        }
    }

    public void setAreaFlag(Flag flag, Boolean value) {
        perms.set(flag.getName(), value);
    }

    public void setGroupFlag(String group, Flag flag, Boolean value) {
        if (!groups.isConfigurationSection(group)) {
            groups.createSection(group);
        }
        groups.getConfigurationSection(group).set(flag.getName(), value);
    }

    @Override
    public World getWorld() {
        return world;
    }

    public void save() throws IOException {
        file.save(saveFile);
    }

    @Override
    public Map<Flag, Boolean> getAreaFlags() {
        HashMap<Flag, Boolean> areaFlags = new HashMap<Flag, Boolean>();
        for (String flag : perms.getKeys(false)) {
            Flag flagObj = FlagManager.getFlag(flag);
            if (flagObj != null) {
                areaFlags.put(flagObj, perms.getBoolean(flag));
            }
        }
        return areaFlags;
    }

    @Override
    public void removeAllAreaFlags() {
        ConfigurationSection parent = perms.getParent();
        parent.set("Permissions", null);
        perms = parent.createSection("Permissions");
    }

    @Override
    public Map<String, Map<Flag, Boolean>> getGroupFlags() {
        Map<String, Map<Flag, Boolean>> groupFlags = new HashMap<String, Map<Flag, Boolean>>();
        for (String group : this.groups.getKeys(false)) {
            ConfigurationSection groupSection = this.groups.getConfigurationSection(group);
            HashMap<Flag, Boolean> gFlags = new HashMap<Flag, Boolean>();
            for (String flag : groupSection.getKeys(false)) {
                Flag flagObj = FlagManager.getFlag(flag);
                if (flagObj != null) {
                    gFlags.put(flagObj, groupSection.getBoolean(flag));
                }
            }
            groupFlags.put(group, gFlags);
        }
        return groupFlags;
    }

    @Override
    public void removeAllGroupFlags(String group) {
        groups.set(group, null);
    }

    public void removeAllGroupFlags() {
        ConfigurationSection parent = groups.getParent();
        parent.set("Groups", null);
        groups = parent.createSection("Groups");
    }

    @Override
    public void clearFlags() {
        removeAllAreaFlags();
        removeAllGroupFlags();
    }
}

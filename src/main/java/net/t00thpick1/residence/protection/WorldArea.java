package net.t00thpick1.residence.protection;

import java.io.File;
import java.io.IOException;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.PermissionsArea;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldArea implements PermissionsArea {
    private World world;
    private ConfigurationSection perms;
    private ConfigurationSection groups;
    private File saveFile;
    private FileConfiguration file;

    public WorldArea(FileConfiguration section, File worldFile) {
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
    public boolean allowAction(Player player, Flag flag) {
        String group = GroupManager.getPlayerGroup(player.getName());
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

    public void setFlag(Flag flag, Boolean value) {
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
}

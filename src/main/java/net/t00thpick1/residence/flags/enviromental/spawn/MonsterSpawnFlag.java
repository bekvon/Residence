package net.t00thpick1.residence.flags.enviromental.spawn;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
import net.t00thpick1.residence.utils.Utilities;

public class MonsterSpawnFlag extends SpawnFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("MonsterSpawnFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(area));
    }

    protected boolean shouldCheck(EntityType type) {
        return !Utilities.isAnimal(type);
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new MonsterSpawnFlag(), plugin);
    }
}

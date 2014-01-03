package net.t00thpick1.residence.flags.enviromental.explosion;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public class CreeperFlag extends ExplosionFlag {
    public static final String FLAG = LocaleLoader.getString("CreeperFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    protected boolean shouldCheck(EntityType type) {
        return type == EntityType.CREEPER;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new CreeperFlag(), plugin);
    }
}

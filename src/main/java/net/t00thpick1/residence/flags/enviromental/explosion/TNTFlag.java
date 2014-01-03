package net.t00thpick1.residence.flags.enviromental.explosion;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public class TNTFlag extends ExplosionFlag {
    public static final String FLAG = LocaleLoader.getString("TNTFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(area));
    }

    protected boolean shouldCheck(EntityType type) {
        return type == EntityType.PRIMED_TNT || type == EntityType.MINECART_TNT;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new TNTFlag(), plugin);
    }
}

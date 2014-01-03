package net.t00thpick1.residence.flags.build.bucket;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class WaterBucketFlag extends BucketFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("WaterBucketFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(player, area));
    }

    private WaterBucketFlag() {
        fillMaterial = Material.WATER;
        emptyMaterial = Material.WATER_BUCKET;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new WaterBucketFlag(), plugin);
    }
}

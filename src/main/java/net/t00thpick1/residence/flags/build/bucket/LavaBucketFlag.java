package net.t00thpick1.residence.flags.build.bucket;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class LavaBucketFlag extends BucketFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.LavaBucket");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    private LavaBucketFlag() {
        fillMaterial = Material.LAVA;
        emptyMaterial = Material.LAVA_BUCKET;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new LavaBucketFlag(), plugin);
    }
}

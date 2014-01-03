package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.flags.build.bucket.BucketFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.Player;

public abstract class BuildFlag extends Flag {
    public static final String FLAG = LocaleLoader.getString("BuildFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        PlaceFlag.initialize();
        DestroyFlag.initialize();
        BucketFlag.initialize();
        TrampleFlag.initialize();
    }
}

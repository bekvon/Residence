package net.t00thpick1.residence.flags.use.container;

import org.bukkit.entity.Player;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public class ContainerFlag extends UseFlag {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.Container");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        ChestFlag.initialize();
        FurnaceFlag.initialize();
        BrewFlag.initialize();
        ItemFrameFlag.initialize();
    }
}

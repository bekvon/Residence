package net.t00thpick1.residence.flags.use.redstone;

import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.Player;

public class RedstoneFlag extends UseFlag {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.Redstone");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        ButtonFlag.initialize();
        PressurePlateFlag.initialize();
        LeverFlag.initialize();
        DiodeFlag.initialize();
    }
}

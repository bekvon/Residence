package net.t00thpick1.residence.flags.use.redstone;

import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.flags.use.utilities.UtilityFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.Player;

public class RedstoneFlag extends UtilityFlag {
    public static final String FLAG = LocaleLoader.getString("RedstoneFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(player, area));
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        ButtonFlag.initialize();
        PressurePlateFlag.initialize();
        LeverFlag.initialize();
        DiodeFlag.initialize();
    }
}

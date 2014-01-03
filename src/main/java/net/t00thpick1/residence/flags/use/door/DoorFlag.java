package net.t00thpick1.residence.flags.use.door;

import org.bukkit.entity.Player;

import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public class DoorFlag extends UseFlag {
    public static final String FLAG = LocaleLoader.getString("DoorFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(player, area));
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        TrapDoorFlag.initialize();
        HingedDoorFlag.initialize();
        FenceGateFlag.initialize();
    }
}

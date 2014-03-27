package net.t00thpick1.residence.flags.use.door;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class DoorFlag extends Flag {
    private DoorFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final DoorFlag FLAG = new DoorFlag(LocaleLoader.getString("Flags.Flags.Door"), FlagType.ANY, UseFlag.FLAG);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        TrapDoorFlag.initialize();
        HingedDoorFlag.initialize();
        FenceGateFlag.initialize();
    }
}

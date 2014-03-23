package net.t00thpick1.residence.flags.use;

import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.flags.use.container.ContainerFlag;
import net.t00thpick1.residence.flags.use.door.DoorFlag;
import net.t00thpick1.residence.flags.use.redstone.RedstoneFlag;
import net.t00thpick1.residence.flags.use.utilities.UtilityFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class UseFlag extends Flag {
    private UseFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final UseFlag FLAG = new UseFlag(LocaleLoader.getString("Flags.Flags.Use"), FlagType.ANY, null);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        DoorFlag.initialize();
        UtilityFlag.initialize();
        DragonEggFlag.initialize();
        CakeFlag.initialize();
        RedstoneFlag.initialize();
        ContainerFlag.initialize();
    }
}

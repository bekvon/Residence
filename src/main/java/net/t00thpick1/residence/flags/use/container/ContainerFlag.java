package net.t00thpick1.residence.flags.use.container;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class ContainerFlag extends Flag {
    private ContainerFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final ContainerFlag FLAG = new ContainerFlag(LocaleLoader.getString("Flags.Flags.Container"), FlagType.ANY, UseFlag.FLAG);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        ChestFlag.initialize();
        FurnaceFlag.initialize();
        BrewFlag.initialize();
        ItemFrameFlag.initialize();
    }
}

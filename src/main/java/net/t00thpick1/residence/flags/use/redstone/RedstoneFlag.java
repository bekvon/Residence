package net.t00thpick1.residence.flags.use.redstone;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class RedstoneFlag extends Flag {
    private RedstoneFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final RedstoneFlag FLAG = new RedstoneFlag(LocaleLoader.getString("Flags.Flags.Redstone"), FlagType.ANY, UseFlag.FLAG);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        ButtonFlag.initialize();
        PressurePlateFlag.initialize();
        LeverFlag.initialize();
        DiodeFlag.initialize();
    }
}

package net.t00thpick1.residence.flags.use.utilities;

import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class UtilityFlag extends Flag {
    private UtilityFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final UtilityFlag FLAG = new UtilityFlag(LocaleLoader.getString("Flags.Flags.Utility"), FlagType.ANY, UseFlag.FLAG);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        WorkBenchFlag.initialize();
        EnchantmentTableFlag.initialize();
        BeaconFlag.initialize();
        AnvilFlag.initialize();
        EnderChestFlag.initialize();
        BedFlag.initialize();
    }
}

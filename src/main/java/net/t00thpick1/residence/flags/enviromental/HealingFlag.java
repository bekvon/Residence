package net.t00thpick1.residence.flags.enviromental;

import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class HealingFlag extends Flag {

    private HealingFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }
    public static final HealingFlag FLAG = new HealingFlag(LocaleLoader.getString("Flags.Flags.Healing"), FlagType.AREA_ONLY, null);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
    }
}

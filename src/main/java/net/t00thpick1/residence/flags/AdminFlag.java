package net.t00thpick1.residence.flags;

import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class AdminFlag extends Flag {
    private AdminFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final AdminFlag FLAG = new AdminFlag(LocaleLoader.getString("Flags.Flags.Admin"), FlagType.PLAYER_ONLY, null);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
    }
}

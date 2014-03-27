package net.t00thpick1.residence.flags.enviromental.explosion;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class ExplosionFlag extends Flag {
    private ExplosionFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final ExplosionFlag FLAG = new ExplosionFlag(LocaleLoader.getString("Flags.Flags.Explosion"), FlagType.AREA_ONLY, null);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        CreeperFlag.initialize();
        BedExplosionFlag.initialize();
        TNTFlag.initialize();
        WitherExplosionFlag.initialize();
        FireballFlag.initialize();
    }
}

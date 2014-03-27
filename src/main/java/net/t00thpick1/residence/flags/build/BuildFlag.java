package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.flags.build.bucket.BucketFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class BuildFlag extends Flag {
    public final static BuildFlag FLAG = new BuildFlag(LocaleLoader.getString("Flags.Flags.Build"), FlagType.ANY, null);

    private BuildFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        PlaceFlag.initialize();
        DestroyFlag.initialize();
        EndermanPickupFlag.initialize();
        BucketFlag.initialize();
        TrampleFlag.initialize();
    }
}

package net.t00thpick1.residence.flags.build.bucket;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.flags.build.BuildFlag;
import net.t00thpick1.residence.locale.LocaleLoader;

public class BucketFlag extends Flag {
    private BucketFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final BucketFlag FLAG = new BucketFlag(LocaleLoader.getString("Flags.Flags.Bucket"), FlagType.ANY, BuildFlag.FLAG);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        LavaBucketFlag.initialize();
        WaterBucketFlag.initialize();
    }
}

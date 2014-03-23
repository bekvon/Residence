package net.t00thpick1.residence.flags.enviromental.spawn;

import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class SpawnFlag extends Flag {
    private SpawnFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final SpawnFlag FLAG = new SpawnFlag(LocaleLoader.getString("Flags.Flags.Spawn"), FlagType.AREA_ONLY, null);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        AnimalSpawnFlag.initialize();
        MonsterSpawnFlag.initialize();
    }
}

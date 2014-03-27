package net.t00thpick1.residence.flags.enviromental.flow;

import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

public class FlowFlag extends Flag {
    private FlowFlag(String flag, net.t00thpick1.residence.api.flags.Flag.FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final FlowFlag FLAG = new FlowFlag(LocaleLoader.getString("Flags.Flags.Flow"), FlagType.AREA_ONLY, null);

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        LavaFlowFlag.initialize();
        WaterFlowFlag.initialize();
    }
}

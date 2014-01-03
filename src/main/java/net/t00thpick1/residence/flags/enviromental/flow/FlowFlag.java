package net.t00thpick1.residence.flags.enviromental.flow;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

public abstract class FlowFlag extends Flag {
    public static final String FLAG = LocaleLoader.getString("FlowFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(area));
    }

    public abstract boolean shouldCheck(Material material);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Material mat = event.getBlock().getType();
        if (!shouldCheck(mat)) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        LavaFlowFlag.initialize();
        WaterFlowFlag.initialize();
    }
}

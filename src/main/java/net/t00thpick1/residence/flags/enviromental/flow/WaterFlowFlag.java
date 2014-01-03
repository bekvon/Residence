package net.t00thpick1.residence.flags.enviromental.flow;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class WaterFlowFlag extends FlowFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("WaterFlowFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    public boolean shouldCheck(Material material) {
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new WaterFlowFlag(), plugin);
    }
}

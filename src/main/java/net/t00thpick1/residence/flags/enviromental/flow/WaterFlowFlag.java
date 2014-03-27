package net.t00thpick1.residence.flags.enviromental.flow;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.Plugin;

public class WaterFlowFlag extends Flag implements Listener {
    private WaterFlowFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final WaterFlowFlag FLAG = new WaterFlowFlag(LocaleLoader.getString("Flags.Flags.WaterFlow"), FlagType.AREA_ONLY, null);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Material mat = event.getBlock().getType();
        if (!shouldCheck(mat)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    public boolean shouldCheck(Material material) {
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

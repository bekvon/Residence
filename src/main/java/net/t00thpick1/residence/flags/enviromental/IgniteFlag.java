package net.t00thpick1.residence.flags.enviromental;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.plugin.Plugin;

public class IgniteFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("IgniteFlag");

    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation());
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();
            if (isAdminMode(player)) {
                return;
            }
            if (!allowAction(player, area)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("FlagDeny", FLAG));
            }
        } else {
           if (!allowAction(area)) {
                event.setCancelled(true);
            }
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new IgniteFlag(), plugin);
    }
}

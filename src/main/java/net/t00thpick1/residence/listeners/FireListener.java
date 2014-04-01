package net.t00thpick1.residence.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

public class FireListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(FlagManager.FIRESPREAD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == IgniteCause.SPREAD) {
            if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation()).allowAction(FlagManager.FIRESPREAD)) {
                event.setCancelled(true);
            }
            return;
        }
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation());
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();
            if (Utilities.isAdminMode(player)) {
                return;
            }
            if (!area.allowAction(player.getName(), FlagManager.IGNITE)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", FlagManager.IGNITE.getName()));
            }
            return;
        }
        if (!area.allowAction(FlagManager.IGNITE)) {
            event.setCancelled(true);
        }
    }
}

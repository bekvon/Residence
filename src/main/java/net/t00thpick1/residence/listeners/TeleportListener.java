package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!StateAssurance.canSpawn(player, event.getTo())) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.CannotSpawnAtDestination"));
            return;
        }
        if (!player.hasPermission("residence.admin.tp") && event.getCause() == TeleportCause.PLUGIN) {
            if (!ResidenceAPI.getPermissionsAreaByLocation(event.getFrom()).allowAction(player.getName(), FlagManager.TELEPORT)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.TPOutDeny"));
                return;
            }
            if (!ResidenceAPI.getPermissionsAreaByLocation(event.getTo()).allowAction(player.getName(), FlagManager.TELEPORT)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.TPDeny"));
                return;
            }
        }
        StateAssurance.handleNewLocation(player, event.getFrom(), event.getTo());
    }
}

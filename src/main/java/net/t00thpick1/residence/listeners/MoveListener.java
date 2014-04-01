package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }
        if (event.getFrom().distance(event.getTo()) == 0) {
            return;
        }
        if (Utilities.isAdminMode(player) || player.hasPermission("residence.admin.move")) {
            StateAssurance.handleNewLocation(player, event.getFrom(), event.getTo());
            return;
        }
        ResidenceArea res = ResidenceAPI.getResidenceManager().getByLocation(event.getTo());
        if (res == null) {
            StateAssurance.handleNewLocation(player, event.getFrom(), event.getTo());
            return;
        }
        if (!res.allowAction(player.getName(), FlagManager.MOVE)) {
            Location lastLoc = StateAssurance.getLastOutsideLocation(player.getName());
            if (lastLoc != null) {
                player.teleport(lastLoc);
            } else {
                player.teleport(res.getOutsideFreeLoc(player.getLocation()));
            }
            player.sendMessage(LocaleLoader.getString("Flags.Messages.MoveDeny"));
            return;
        }
        StateAssurance.handleNewLocation(player, event.getFrom(), event.getTo());
    }
}

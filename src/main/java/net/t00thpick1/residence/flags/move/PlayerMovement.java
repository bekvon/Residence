package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

public class PlayerMovement implements Listener {
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
            MoveFlag.handleNewLocation(player, event.getTo());
            return;
        }
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(event.getTo());
        if (res == null) {
            MoveFlag.handleNewLocation(player, event.getTo());
            return;
        }
        if (!res.allowAction(player, MoveFlag.FLAG)) {
            Location lastLoc = MoveFlag.lastOutsideLoc.get(player.getName());
            if (lastLoc != null) {
                player.teleport(lastLoc);
            } else {
                player.teleport(res.getOutsideFreeLoc(player.getLocation()));
            }
            player.sendMessage(LocaleLoader.getString("Flags.Message.MoveDeny"));
            return;
        }
        MoveFlag.handleNewLocation(player, event.getTo());
    }

    public static void initialize() {
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new PlayerMovement(), plugin);
    }
}

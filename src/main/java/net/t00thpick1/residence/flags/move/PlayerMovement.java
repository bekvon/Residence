package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

public class PlayerMovement extends MoveFlag {
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
        if (isAdminMode(player) || player.hasPermission("residence.admin.move")) {
            handleNewLocation(player, event.getTo());
            return;
        }
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(event.getTo());
        if (res == null) {
            handleNewLocation(player, event.getTo());
            return;
        }
        if (!allowAction(player, res)) {
            Location lastLoc = lastOutsideLoc.get(player.getName());
            if (lastLoc != null) {
                player.teleport(lastLoc);
            } else {
                player.teleport(res.getOutsideFreeLoc(player.getLocation()));
            }
            player.sendMessage(LocaleLoader.getString("Flags.Message.MoveDeny"));
            return;
        }
        handleNewLocation(player, event.getTo());
    }

    public static void initialize() {
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new PlayerMovement(), plugin);
    }
}

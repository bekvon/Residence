package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

public class TPFlag extends MoveFlag {
    public static final String FLAG = LocaleLoader.getString("TPFlag");

    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location loc = event.getTo();
        Player player = event.getPlayer();

        if (!StateAssurance.canSpawn(player, loc)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("CannotMoveAtDestination"));
            return;
        }
        if (!player.hasPermission("residence.admin.tp") && event.getCause() == TeleportCause.PLUGIN) {
            if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(event.getFrom()))) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("TPOutDeny"));
                return;
            }
            if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(loc))) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("TPDeny"));
                return;
            }
        }
        handleNewLocation(player, loc);
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new TPFlag(), plugin);
    }
}

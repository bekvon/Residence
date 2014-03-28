package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

public class TPFlag extends Flag implements Listener {
    private TPFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final TPFlag FLAG = new TPFlag(LocaleLoader.getString("Flags.Flags.TP"), FlagType.ANY, MoveFlag.FLAG);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location loc = event.getTo();
        Player player = event.getPlayer();

        if (!StateAssurance.canSpawn(player, loc)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.CannotSpawnAtDestination"));
            return;
        }
        if (!player.hasPermission("residence.admin.tp") && event.getCause() == TeleportCause.PLUGIN) {
            if (!ResidenceAPI.getPermissionsAreaByLocation(event.getFrom()).allowAction(player.getName(), this)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.TPOutDeny"));
                return;
            }
            if (!ResidenceAPI.getPermissionsAreaByLocation(loc).allowAction(player.getName(), this)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.TPDeny"));
                return;
            }
        }
        StateAssurance.handleNewLocation(player, loc);
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

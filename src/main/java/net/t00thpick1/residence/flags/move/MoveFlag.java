package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveFlag extends Flag implements Listener {
    private MoveFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final MoveFlag FLAG = new MoveFlag(LocaleLoader.getString("Flags.Flags.Move"), FlagType.ANY, null);

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
            StateAssurance.handleNewLocation(player, event.getTo());
            return;
        }
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(event.getTo());
        if (res == null) {
            StateAssurance.handleNewLocation(player, event.getTo());
            return;
        }
        if (!res.allowAction(player, MoveFlag.FLAG)) {
            Location lastLoc = StateAssurance.getLastOutsideLocation(player.getName());
            if (lastLoc != null) {
                player.teleport(lastLoc);
            } else {
                player.teleport(res.getOutsideFreeLoc(player.getLocation()));
            }
            player.sendMessage(LocaleLoader.getString("Flags.Messages.MoveDeny"));
            return;
        }
        StateAssurance.handleNewLocation(player, event.getTo());
    }

    public static void initialize() {
        TPFlag.initialize();
        StateAssurance.initialize();
        VehicleMoveFlag.initialize();
        FlagManager.addFlag(FLAG);
        Residence plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

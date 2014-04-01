package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class VehicleMoveListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        Entity passenger = event.getVehicle().getPassenger();
        if (!(passenger instanceof Player)) {
            return;
        }
        Player player = (Player) passenger;

        if (Utilities.isAdminMode(player) || player.hasPermission("residence.admin.move")) {
            StateAssurance.handleNewLocation(player, event.getFrom(), event.getTo());
            return;
        }

        Location loc = event.getTo();

        ResidenceArea res = ResidenceAPI.getResidenceManager().getByLocation(event.getTo());
        if (res == null) {
            StateAssurance.handleNewLocation(player, event.getFrom(), event.getTo());
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getTo()).allowAction(player.getName(), FlagManager.VEHICLEMOVE)) {
            player.teleport(event.getFrom());
            player.sendMessage(LocaleLoader.getString("Flags.Messages.VehicleMoveDeny"));
            return;
        }

        StateAssurance.handleNewLocation(player, event.getFrom(), loc);
    }
}

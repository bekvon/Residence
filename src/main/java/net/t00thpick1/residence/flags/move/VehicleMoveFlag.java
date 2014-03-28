package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.flags.Flag;
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
import org.bukkit.plugin.Plugin;

public class VehicleMoveFlag extends Flag implements Listener {
    private VehicleMoveFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final VehicleMoveFlag FLAG = new VehicleMoveFlag(LocaleLoader.getString("Flags.Flags.VehicleMove"), FlagType.ANY, MoveFlag.FLAG);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        Entity passenger = event.getVehicle().getPassenger();
        if (!(passenger instanceof Player)) {
            return;
        }
        Player player = (Player) passenger;

        if (Utilities.isAdminMode(player) || player.hasPermission("residence.admin.move")) {
            StateAssurance.handleNewLocation(player, event.getTo());
            return;
        }

        Location loc = event.getTo();

        ResidenceArea res = Residence.getInstance().getResidenceManager().getByLocation(event.getTo());
        if (res == null) {
            StateAssurance.handleNewLocation(player, event.getTo());
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getTo()).allowAction(player.getName(), this)) {
            event.getVehicle().teleport(event.getFrom());
            player.sendMessage(LocaleLoader.getString("Flags.Messages.VehicleMoveDeny"));
            return;
        }

        StateAssurance.handleNewLocation(player, loc);
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

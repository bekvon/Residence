package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.Plugin;

public class VehicleMoveFlag extends MoveFlag {
    public static final String FLAG = LocaleLoader.getString("VehicleMoveFlag");

    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(player, area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        Entity passenger = event.getVehicle().getPassenger();
        if (!(passenger instanceof Player)) {
            return;
        }
        Player player = (Player) passenger;

        if (isAdminMode(player) || player.hasPermission("residence.admin.move")) {
            handleNewLocation(player, event.getTo());
            return;
        }

        Location loc = event.getTo();

        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(event.getTo());
        if (res == null) {
            handleNewLocation(player, event.getTo());
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(event.getTo()))) {
            event.getVehicle().teleport(event.getFrom());
            player.sendMessage(LocaleLoader.getString("VehicleMoveDeny"));
            return;
        }

        handleNewLocation(player, loc);
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new VehicleMoveFlag(), plugin);
    }
}

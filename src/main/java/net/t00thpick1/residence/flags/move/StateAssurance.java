package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.event.ResidenceDeleteEvent;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class StateAssurance extends MoveFlag {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        currentRes.remove(pname);
        lastOutsideLoc.remove(pname);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!canSpawn(player, player.getLocation())) {
            player.teleport(getSpawnLocation(player, player.getLocation()));
        }
        handleNewLocation(player, player.getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!canSpawn(player, event.getRespawnLocation())) {
            event.setRespawnLocation(getSpawnLocation(player, event.getRespawnLocation()));
        }
        handleNewLocation(player, event.getRespawnLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        for (Player player : event.getResidence().getPlayersInResidence()) {
            handleNewLocation(player, player.getLocation());
        }
    }

    protected static boolean canSpawn(Player player, Location location) {
        if (player.hasPermission("residence.admin.move") || Utilities.isAdminMode(player)) {
            return true;
        }
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(location);
        if (player.getVehicle() != null && !area.allow(VehicleMoveFlag.FLAG, area.allow(MoveFlag.FLAG, false))) {
            return false;
        }
        if (!area.allow(MoveFlag.FLAG, false)) {
            return false;
        }
        return true;
    }

    protected static Location getSpawnLocation(Player player, Location location) {
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(location);
        Location loc = null;
        if (area instanceof ClaimedResidence) {
            loc = ((ClaimedResidence) area).getOutsideFreeLoc(location);
            if (!canSpawn(player, loc)) {
                loc = null;
            }
        }
        if (loc == null) {
            loc = area.getWorld().getSpawnLocation();
        }
        return loc;
    }

    public static void initialize() {
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new StateAssurance(), plugin);
    }
}

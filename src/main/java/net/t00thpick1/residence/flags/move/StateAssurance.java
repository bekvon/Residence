package net.t00thpick1.residence.flags.move;

import java.util.HashMap;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.areas.WorldArea;
import net.t00thpick1.residence.api.events.PlayerChangedAreaEvent;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class StateAssurance implements Listener {

    private StateAssurance() {
        lastOutsideLoc = new HashMap<String, Location>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        lastOutsideLoc.remove(pname);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!canSpawn(player, player.getLocation())) {
            player.teleport(getSpawnLocation(player, player.getLocation()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!canSpawn(player, event.getRespawnLocation())) {
            event.setRespawnLocation(getSpawnLocation(player, event.getRespawnLocation()));
        }
    }

    public static boolean canSpawn(Player player, Location location) {
        if (player.hasPermission("residence.admin.move") || Utilities.isAdminMode(player)) {
            return true;
        }
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(location);
        if (player.getVehicle() != null && !area.allowAction(player.getName(), VehicleMoveFlag.FLAG)) {
            return false;
        }
        if (!area.allowAction(player.getName(), MoveFlag.FLAG)) {
            return false;
        }
        return true;
    }

    private static Location getSpawnLocation(Player player, Location location) {
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(location);
        Location loc = null;
        if (area instanceof ResidenceArea) {
            loc = ((ResidenceArea) area).getOutsideFreeLoc(location);
            if (!canSpawn(player, loc)) {
                loc = null;
            }
        }
        if (loc == null) {
            loc = area.getWorld().getSpawnLocation();
        }
        return loc;
    }

    private HashMap<String, Location> lastOutsideLoc;

    public static void handleNewLocation(Player player, Location from, Location to) {
        String pname = player.getName();

        PermissionsArea newArea = ResidenceAPI.getPermissionsAreaByLocation(to);
        PermissionsArea oldArea = ResidenceAPI.getPermissionsAreaByLocation(from);
        if (newArea instanceof WorldArea) {
            instance.lastOutsideLoc.put(pname, to);
            if (oldArea instanceof ResidenceArea) {
                ResidenceArea oldRes = (ResidenceArea) oldArea;
                String leave = oldRes.getLeaveMessage();

                if (leave != null) {
                    player.sendMessage(formatString(leave, oldRes.getName(), player));
                }
            }
            PlayerChangedAreaEvent event = new PlayerChangedAreaEvent(oldArea, newArea, player);
            Residence.getInstance().getServer().getPluginManager().callEvent(event);
            return;
        }
        instance.lastOutsideLoc.put(pname, to);
        if (!oldArea.equals(newArea)) {
            ResidenceArea oldRes = null;
            ResidenceArea newRes = (ResidenceArea) newArea;
            if (oldArea instanceof ResidenceArea) {
                oldRes = (ResidenceArea) oldArea;
                String leaveMessage = oldRes.getLeaveMessage();

                if (leaveMessage != null && !oldArea.equals(newRes.getTopParent())) {
                    player.sendMessage(formatString(leaveMessage, oldRes.getName(), player));
                }
            }
            String enterMessage = newRes.getEnterMessage();

            if (enterMessage != null && (oldRes == null || !newArea.equals(oldRes.getTopParent()))) {
                 player.sendMessage(formatString(enterMessage, newRes.getName(), player));
            }
            PlayerChangedAreaEvent event = new PlayerChangedAreaEvent(oldArea, newArea, player);
            Residence.getInstance().getServer().getPluginManager().callEvent(event);
        }
    }

    private static String formatString(String message, String areaName, Player player) {
        return ChatColor.translateAlternateColorCodes('&', message.replaceAll("(%player%)", player.getName()).replaceAll("(%area%)", areaName));
    }

    public static void initialize() {
        Plugin plugin = Residence.getInstance();
        instance = new StateAssurance();
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
    }

    private static StateAssurance instance;

    public static Location getLastOutsideLocation(String name) {
        return instance.lastOutsideLoc.get(name);
    }
}

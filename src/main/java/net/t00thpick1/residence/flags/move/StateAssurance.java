package net.t00thpick1.residence.flags.move;

import java.util.HashMap;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
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
        currentRes = new HashMap<String, ResidenceArea>();
        lastOutsideLoc = new HashMap<String, Location>();
    }

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

    public static boolean canSpawn(Player player, Location location) {
        if (player.hasPermission("residence.admin.move") || Utilities.isAdminMode(player)) {
            return true;
        }
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(location);
        if (player.getVehicle() != null && !area.allowAction(player, VehicleMoveFlag.FLAG)) {
            return false;
        }
        if (!area.allowAction(player, MoveFlag.FLAG)) {
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

    private HashMap<String, ResidenceArea> currentRes;
    private HashMap<String, Location> lastOutsideLoc;

    public static void handleNewLocation(Player player, Location loc) {
        String pname = player.getName();

        ResidenceArea res = Residence.getInstance().getResidenceManager().getByLocation(loc);
        if (res != null) {
            if (res.getSubzoneByLoc(loc) != null) {
                res = res.getSubzoneByLoc(loc);
            }
        }

        ResidenceArea resOld = null;
        if (instance.currentRes.containsKey(pname)) {
            resOld = instance.currentRes.get(pname);
        }
        if (res == null) {
            instance.lastOutsideLoc.put(pname, loc);
            if (resOld != null) {
                String leave = resOld.getLeaveMessage();

                if (leave != null && !leave.equals("")) {
                    player.sendMessage(formatString(leave, resOld.getName(), player));
                }
                instance.currentRes.remove(pname);
            }
            return;
        }
        instance.lastOutsideLoc.put(pname, loc);
        if (!instance.currentRes.containsKey(pname) || resOld != res) {
            instance.currentRes.put(pname, res);

            if (resOld != res && resOld != null) {
                String leaveMessage = resOld.getLeaveMessage();

                if (leaveMessage != null && !leaveMessage.equals("") && resOld != res.getParent()) {
                    player.sendMessage(formatString(leaveMessage, resOld.getName(), player));
                }
            }
            String enterMessage = res.getEnterMessage();

            if (enterMessage != null && !enterMessage.equals("") && !(resOld != null && res == resOld.getParent())) {
                player.sendMessage(formatString(enterMessage, res.getName(), player));
            }
        }
    }

    private static String formatString(String message, String areaName, Player player) {
        return ChatColor.translateAlternateColorCodes('&', message.replaceAll("(%player%)", player.getName()).replaceAll("(%area%)", areaName));
    }

    public static ResidenceArea getCurrentResidence(String player) {
        return instance.currentRes.get(player);
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

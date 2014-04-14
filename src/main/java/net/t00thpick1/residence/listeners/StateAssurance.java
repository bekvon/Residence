package net.t00thpick1.residence.listeners;

import java.util.HashMap;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.areas.WorldArea;
import net.t00thpick1.residence.api.events.PlayerChangedAreaEvent;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
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

public class StateAssurance implements Listener {
    private static boolean messages = !ConfigManager.getInstance().noMessages();

    public StateAssurance() {
        lastOutsideLoc = new HashMap<String, Location>();
        instance = this;
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
        if (player.getVehicle() != null && !area.allowAction(player.getName(), FlagManager.VEHICLEMOVE)) {
            return false;
        }
        if (!area.allowAction(player.getName(), FlagManager.MOVE)) {
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

                if (messages && leave != null && !leave.equals("")) {
                    player.sendMessage(formatString(leave, oldRes, player));
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

                if (messages && leaveMessage != null && !leaveMessage.equals("") && !oldArea.equals(newRes.getTopParent())) {
                    player.sendMessage(formatString(leaveMessage, oldRes, player));
                }
            }
            String enterMessage = newRes.getEnterMessage();

            if (messages  && enterMessage != null && !enterMessage.equals("") && (oldRes == null || !newArea.equals(oldRes.getTopParent()))) {
                 player.sendMessage(formatString(enterMessage, newRes, player));
            }
            PlayerChangedAreaEvent event = new PlayerChangedAreaEvent(oldArea, newArea, player);
            Residence.getInstance().getServer().getPluginManager().callEvent(event);
        }
    }

    private static String formatString(String message, ResidenceArea res, Player player) {
        message = message.replaceAll("(%renter%)", res.isRented() ? res.getRenter() : LocaleLoader.getString("Info.Unrented"));
        message = message.replaceAll("(%player%)", player.getName());
        message = message.replaceAll("(%area%)", res.getName().replaceAll("(_)", " "));
        message = message.replaceAll("(%owner%)", res.getOwner());
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static StateAssurance instance;

    public static Location getLastOutsideLocation(String name) {
        return instance.lastOutsideLoc.get(name);
    }
}

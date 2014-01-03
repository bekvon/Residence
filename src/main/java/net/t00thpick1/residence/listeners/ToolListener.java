package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.permissions.PermissionGroup;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class ToolListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (player.getItemInHand().getType() == ConfigManager.getInstance().getSelectionToolType()) {
            if (Residence.getInstance().getCompatabilityManager().isUsingExternalSelectionTool()) {
                return;
            }
            if (!Utilities.isAdminMode(player) && (!player.hasPermission("residence.select") && !(player.hasPermission("residence.create") && !player.isPermissionSet("residence.select")))) {
                return;
            }
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location loc = block.getLocation();
                Residence.getInstance().getSelectionManager().placeLoc1(player, loc);
                player.sendMessage(LocaleLoader.getString("SelectPoint", LocaleLoader.getString("Primary"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location loc = block.getLocation();
                Residence.getInstance().getSelectionManager().placeLoc2(player, loc);
                player.sendMessage(LocaleLoader.getString("SelectPoint", LocaleLoader.getString("Secondary"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            }
            event.setCancelled(true);
            return;
        }
        if (player.getItemInHand().getType() == ConfigManager.getInstance().getInfoToolID()) {
            if (!player.hasPermission("residence.info")) {
                return;
            }
            if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
                return;
            }
            PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(block.getLocation());
            if (area instanceof ClaimedResidence) {
                ((ClaimedResidence) area).printInformation(player);
            } else {
                player.sendMessage(LocaleLoader.getString("NoResidenceHere"));
            }
            event.setCancelled(true);
        }
    }
}

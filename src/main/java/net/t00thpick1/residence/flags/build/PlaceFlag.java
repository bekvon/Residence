package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.use.UseFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;

public class PlaceFlag extends BuildFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("PlaceFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(player, area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        place(event.getPlayer(), event.getEntity().getLocation(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        place(event.getPlayer(), event.getBlock().getLocation(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(PlayerInteractEvent event) {
        if (UseFlag.isSelectionTool(event)) {
            return;
        }
        Player player = event.getPlayer();
        if (isAdminMode(player)) {
            return;
        }
        if (player.getItemInHand() == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (player.getItemInHand().getType() != Material.INK_SACK) {
            return;
        }
        if (((Dye) player.getItemInHand().getData()).getColor() != DyeColor.WHITE) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.GRASS) {
            return;
        }
        place(player, block.getLocation().add(0, 1, 0), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlacePotPlant(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.FLOWER_POT) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getPlayer().getItemInHand();
        if (item == null) {
            return;
        }
        Material mat = item.getType();
        if (mat != Material.SAPLING && mat != Material.RED_ROSE && mat != Material.YELLOW_FLOWER
                && mat != Material.RED_MUSHROOM && mat != Material.BROWN_MUSHROOM && mat != Material.DEAD_BUSH
                && mat != Material.CACTUS) {
            return;
        }
        place(player, block.getLocation(), event);
    }

    private void place(Player player, Location location, Cancellable cancellable) {
        if (isAdminMode(player)) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(location))) {
            cancellable.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("FlagDeny", FLAG));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new PlaceFlag(), plugin);
    }
}

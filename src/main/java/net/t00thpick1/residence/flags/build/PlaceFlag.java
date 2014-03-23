package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

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

public class PlaceFlag extends Flag implements Listener {
    private PlaceFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final PlaceFlag FLAG = new PlaceFlag(LocaleLoader.getString("Flags.Flags.Place"), FlagType.ANY, BuildFlag.FLAG);

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
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
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
        if (Utilities.isAdminMode(player)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(location).allowAction(player, this)) {
            cancellable.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", getName()));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

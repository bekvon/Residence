package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.ResidenceCommandExecutor;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            pressure(player, block, event);
            return;
        }
        if (player.getItemInHand().getType() == ConfigManager.getInstance().getSelectionToolType()) {
            select(player, block, event);
            return;
        }
        if (player.getItemInHand().getType() == ConfigManager.getInstance().getInfoToolType()) {
            info(player, block, event);
            return;
        }
        interact(player, block, event);
    }

    private void interact(Player player, Block block, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (Utilities.isAdminMode(player)) {
            return;
        }
        Flag flag = getFlag(block.getType());
        if (flag == null) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(player.getName(), flag)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.ContainerFlagDeny", flag.getName())));
        }
    }

    private Flag getFlag(Material mat) {
        switch (mat) {
            case CHEST:
            case TRAPPED_CHEST:
                return FlagManager.CHEST;
            case FURNACE:
            case BURNING_FURNACE:
                return FlagManager.FURNACE;
            case BREWING_STAND:
                return FlagManager.BREW;
            case STONE_BUTTON:
            case WOOD_BUTTON:
                return FlagManager.BUTTON;
            case LEVER:
                return FlagManager.LEVER;
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case DIODE:
                return FlagManager.DIODE;
            case CAKE_BLOCK:
                return FlagManager.CAKE;
            case DRAGON_EGG:
                return FlagManager.DRAGONEGG;
            case FENCE_GATE:
                return FlagManager.FENCEGATE;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                return FlagManager.HINGEDDOOR;
            case TRAP_DOOR:
                return FlagManager.TRAPDOOR;
            case ANVIL:
                return FlagManager.ANVIL;
            case BED:
            case BED_BLOCK:
                return FlagManager.BED;
            case ENCHANTMENT_TABLE:
                return FlagManager.ENCHANTMENTTABLE;
            case ENDER_CHEST:
                return FlagManager.ENDERCHEST;
            case WORKBENCH:
                return FlagManager.WORKBENCH;
            default:
                return null;
        }
    }

    private void info(Player player, Block block, PlayerInteractEvent event) {
        if (!player.hasPermission("residence.info")) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(block.getLocation());
        if (area instanceof ResidenceArea) {
            ResidenceCommandExecutor.printInformation(player, (ResidenceArea) area);
        } else {
            player.sendMessage(LocaleLoader.getString("Tool.Info.NoResidenceHere"));
        }
        event.setCancelled(true);
    }

    private void select(Player player, Block block, PlayerInteractEvent event) {
        if (Residence.getInstance().getCompatabilityManager().isUsingExternalSelectionTool()) {
            return;
        }
        if (!Utilities.isAdminMode(player) && !player.hasPermission("residence.select")) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location loc = block.getLocation();
            Residence.getInstance().getSelectionManager().placeLoc1(player, loc);
            player.sendMessage(LocaleLoader.getString("Commands.Select.PrimaryPoint", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location loc = block.getLocation();
            Residence.getInstance().getSelectionManager().placeLoc2(player, loc);
            player.sendMessage(LocaleLoader.getString("Commands.Select.SecondaryPoint", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
        event.setCancelled(true);
    }

    private void pressure(Player player, Block block, PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Material mat = block.getType();
        if (mat == Material.SOIL || mat == Material.SOUL_SAND) {
            if (Utilities.isAdminMode(player)) {
                return;
            }
            if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(player.getName(), FlagManager.TRAMPLE)) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE
                && block.getType() != Material.GOLD_PLATE && block.getType() != Material.IRON_PLATE) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(player.getName(), FlagManager.PRESSUREPLATE)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.UseFlagDeny", FlagManager.PRESSUREPLATE.getName())));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
            return;
        }
        Entity ent = event.getRightClicked();
        if (ent == null) {
            return;
        }
        if (ent.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()).allowAction(player.getName(), FlagManager.ITEMFRAME)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.ContainerFlagDeny", FlagManager.ITEMFRAME.getName())));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        Material mat = block.getType();
        Entity entity = event.getEntity();
        if ((entity.getType() == EntityType.FALLING_BLOCK) || !(mat == Material.SOIL || mat == Material.SOUL_SAND)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(FlagManager.TRAMPLE)) {
            event.setCancelled(true);
        }
    }
}

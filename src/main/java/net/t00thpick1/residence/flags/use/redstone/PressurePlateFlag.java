package net.t00thpick1.residence.flags.use.redstone;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class PressurePlateFlag extends RedstoneFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("PressurePlateFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void OnClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isAdminMode(player)) {
            return;
        }
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE
                && block.getType() != Material.GOLD_PLATE && block.getType() != Material.IRON_PLATE) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()))) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("FlagDeny", LocaleLoader.getString("UseFlagDeny", FLAG)));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new PressurePlateFlag(), plugin);
    }
}

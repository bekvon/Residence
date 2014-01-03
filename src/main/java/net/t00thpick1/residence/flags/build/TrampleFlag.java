package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class TrampleFlag extends BuildFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.Trample");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        Material mat = block.getType();
        Entity entity = event.getEntity();
        if ((entity.getType() == EntityType.FALLING_BLOCK) || !(mat == Material.SOIL || mat == Material.SOUL_SAND)) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
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
        Material mat = block.getType();
        if (!(mat == Material.SOIL || mat == Material.SOUL_SAND)) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()))) {
            event.setCancelled(true);
            return;
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new TrampleFlag(), plugin);
    }
}

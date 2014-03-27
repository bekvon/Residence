package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

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

public class TrampleFlag extends Flag implements Listener {
    private TrampleFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final TrampleFlag FLAG = new TrampleFlag(LocaleLoader.getString("Flags.Flags.Trample"), FlagType.ANY, BuildFlag.FLAG);

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        Material mat = block.getType();
        Entity entity = event.getEntity();
        if ((entity.getType() == EntityType.FALLING_BLOCK) || !(mat == Material.SOIL || mat == Material.SOUL_SAND)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
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
        if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(player, this)) {
            event.setCancelled(true);
            return;
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.plugin.Plugin;

public class DestroyFlag extends Flag implements Listener {
    public static final DestroyFlag FLAG = new DestroyFlag(LocaleLoader.getString("Flags.Flags.Destroy"), FlagType.ANY, BuildFlag.FLAG);

    private DestroyFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Player player = null;
        Entity remover = event.getRemover();
        if (remover instanceof Player) {
            player = (Player) remover;
        } else if (remover instanceof Projectile) {
            Projectile projectile = (Projectile) remover;
            LivingEntity shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                player = (Player) shooter;
            }
        }
        if (player != null) {
            breakBlock(player, event.getEntity().getLocation(), event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        breakBlock(event.getPlayer(), event.getBlock().getLocation(), event);
    }

    private void breakBlock(Player player, Location location, Cancellable cancellable) {
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

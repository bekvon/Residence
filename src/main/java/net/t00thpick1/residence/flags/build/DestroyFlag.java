package net.t00thpick1.residence.flags.build;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
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

public class DestroyFlag extends BuildFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.Destroy");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
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
            place(player, event.getEntity().getLocation(), event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        place(event.getPlayer(), event.getBlock().getLocation(), event);
    }

    private void place(Player player, Location location, Cancellable cancellable) {
        if (isAdminMode(player)) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(location))) {
            cancellable.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", FLAG));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new DestroyFlag(), plugin);
    }
}

package net.t00thpick1.residence.flags;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;

public class PVPFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.PVP");
    public boolean allowAction(PermissionsArea area) {
        return area.allowAction(FLAG, super.allowAction(area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) {
        Entity ent = event.getEntity();
        boolean srcpvp = allowAction(ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()));
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity target = it.next();
            if (target.getType() == EntityType.PLAYER) {
                if (!srcpvp || !allowAction(ResidenceAPI.getPermissionsAreaByLocation(target.getLocation()))) {
                    event.setIntensity(target, 0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity ent = event.getEntity();
        if (ent.hasMetadata("NPC")) {
            return;
        }
        if (!(ent instanceof Player || (ent instanceof Wolf && ((Wolf) ent).isTamed()))) {
            return;
        }
        Entity damager = event.getDamager();
        if (damager == null) {
            return;
        }
        Player player = null;
        if (damager instanceof Player) {
            player = (Player) damager;
        }
        if (damager instanceof Wolf && ((Wolf) damager).isTamed()) {
            player = ((Wolf) damager).getKiller();
        }
        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
            player = (Player) ((Projectile) damager).getShooter();
        }
        if (player == null) {
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()))) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("PVPDeny"));
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(player.getLocation()))) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("PVPDeny"));
            return;
        }
        if (!allowAction(ResidenceAPI.getPermissionsAreaByLocation(damager.getLocation()))) {
            event.setCancelled(true);
            return;
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new PVPFlag(), plugin);
    }
}

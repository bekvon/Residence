package net.t00thpick1.residence.flags;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;

public class PVPFlag extends Flag implements Listener {
    public static final PVPFlag FLAG = new PVPFlag(LocaleLoader.getString("Flags.Flags.PVP"), FlagType.AREA_ONLY, null);

    private PVPFlag(String name, FlagType type, Flag parent) {
        super(name, type, parent);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) {
        Entity ent = event.getEntity();
        boolean srcpvp = ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()).allowAction(this);
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity target = it.next();
            if (target.getType() == EntityType.PLAYER) {
                if (!srcpvp || !ResidenceAPI.getPermissionsAreaByLocation(target.getLocation()).allowAction(this)) {
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
        if (!ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()).allowAction(this)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("PVPDeny"));
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(player.getLocation()).allowAction(this)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("PVPDeny"));
            return;
        }
        if (damager == player) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(damager.getLocation()).allowAction(this)) {
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

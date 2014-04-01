package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

import java.util.Iterator;

public class EntityDamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) {
        Entity ent = event.getEntity();
        boolean srcpvp = ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()).allowAction(FlagManager.PVP);
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity target = it.next();
            if (target.getType() == EntityType.PLAYER) {
                if (!srcpvp || !ResidenceAPI.getPermissionsAreaByLocation(target.getLocation()).allowAction(FlagManager.PVP)) {
                    event.setIntensity(target, 0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity ent = event.getEntity();
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation());
        if (!area.allowAction(FlagManager.DAMAGE)) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                ent.setFireTicks(0);
            }
            return;
        }
        if (event instanceof EntityDamageByEntityEvent) {
            onEntityDamageByEntity((EntityDamageByEntityEvent) event, area);
        }
    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event, PermissionsArea area) {
        Entity ent = event.getEntity();
        if (ent.hasMetadata("NPC")) {
            return;
        }
        if (ent.getType() == EntityType.ITEM_FRAME) {
            Entity damager = event.getDamager();
            Player player = getPlayer(damager);
            if (player == null) {
                return;
            }
            if (Utilities.isAdminMode(player)) {
                return;
            }
            if (!area.allowAction(player.getName(), FlagManager.ITEMFRAME)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.ContainerFlagDeny", FlagManager.ITEMFRAME.getName())));
            }
            return;
        }
        Player receiver = getPlayer(ent);
        if (receiver == null) {
            return;
        }
        Entity damager = event.getDamager();
        Player player = getPlayer(damager);
        if (player == null) {
            return;
        }
        if (!area.allowAction(FlagManager.PVP)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.PVPDeny"));
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(player.getLocation()).allowAction(FlagManager.PVP)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.PVPDeny"));
            return;
        }
        if (damager == player) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(damager.getLocation()).allowAction(FlagManager.PVP)) {
            event.setCancelled(true);
            return;
        }
    }

    public Player getPlayer(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        }
        if (entity instanceof Wolf && ((Wolf) entity).isTamed()) {
            return ((Wolf) entity).getKiller();
        }
        if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof Player) {
            return (Player) ((Projectile) entity).getShooter();
        }
        return null;
    }
}

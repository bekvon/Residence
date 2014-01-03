package net.t00thpick1.residence.flags.use.container;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

public class ItemFrameFlag extends ContainerFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("ItemFrameFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (isAdminMode(player)) {
            return;
        }
        Entity ent = event.getRightClicked();
        if (ent == null) {
            return;
        }
        if (ent.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()))) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("FlagDeny", LocaleLoader.getString("ContainerFlagDeny", FLAG)));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRemoveFromFrame(EntityDamageByEntityEvent event) {
        Entity frame = event.getEntity();
        if (frame.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        Entity ent = event.getDamager();
        if (ent.getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) ent;
        if (isAdminMode(player)) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(frame.getLocation()))) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("FlagDeny", LocaleLoader.getString("ContainerFlagDeny", FLAG)));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new ItemFrameFlag(), plugin);
    }
}

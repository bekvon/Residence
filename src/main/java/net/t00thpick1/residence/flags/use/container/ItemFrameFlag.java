package net.t00thpick1.residence.flags.use.container;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

public class ItemFrameFlag extends Flag implements Listener {
    private ItemFrameFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final ItemFrameFlag FLAG = new ItemFrameFlag(LocaleLoader.getString("Flags.Flags.ItemFrame"), FlagType.ANY, ContainerFlag.FLAG);

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
        if (!ResidenceAPI.getPermissionsAreaByLocation(ent.getLocation()).allowAction(player, this)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.ContainerFlagDeny", this.getName())));
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
        if (Utilities.isAdminMode(player)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(frame.getLocation()).allowAction(player, this)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.ContainerFlagDeny", this.getName())));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

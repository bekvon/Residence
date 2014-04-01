package net.t00thpick1.residence.listeners;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BucketListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Flag flag = null;
        if (event.getBucket() == Material.LAVA_BUCKET) {
            flag = FlagManager.LAVABUCKET;
        }
        if (event.getBucket() == Material.WATER_BUCKET) {
            flag = FlagManager.WATERBUCKET;
        }
        if (flag == null) {
            return;
        }
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
            return;
        }
        BlockFace face = event.getBlockFace();
        Location blockLocation = event.getBlockClicked().getLocation().add(face.getModX(), face.getModY(), face.getModZ());
        if (!ResidenceAPI.getPermissionsAreaByLocation(blockLocation).allowAction(player.getName(), flag)) {
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", flag.getName()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Flag flag = null;
        if (event.getBlockClicked().getType() == Material.LAVA) {
            flag = FlagManager.LAVABUCKET;
        }
        if (event.getBlockClicked().getType() == Material.WATER) {
            flag = FlagManager.WATERBUCKET;
        }
        if (flag == null) {
            return;
        }
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlockClicked().getLocation()).allowAction(player.getName(), flag)) {
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", flag.getName()));
            event.setCancelled(true);
        }
    }
}

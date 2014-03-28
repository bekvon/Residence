package net.t00thpick1.residence.flags.build.bucket;

import net.t00thpick1.residence.Residence;
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
import org.bukkit.plugin.Plugin;

public class WaterBucketFlag extends Flag implements Listener {
    public static final WaterBucketFlag FLAG = new WaterBucketFlag(LocaleLoader.getString("Flags.Flags.WaterBucket"), FlagType.ANY, BucketFlag.FLAG);

    public WaterBucketFlag(String name, FlagType type, Flag parent) {
        super(name, type, parent);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.WATER_BUCKET) {
            return;
        }
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
            return;
        }
        BlockFace face = event.getBlockFace();
        Location blockLocation = event.getBlockClicked().getLocation().add(face.getModX(), face.getModY(), face.getModZ());
        if (!ResidenceAPI.getPermissionsAreaByLocation(blockLocation).allowAction(player.getName(), this)) {
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", getName()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.getBlockClicked().getType() != Material.WATER) {
            return;
        }
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getBlockClicked().getLocation()).allowAction(player.getName(), this)) {
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", getName()));
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

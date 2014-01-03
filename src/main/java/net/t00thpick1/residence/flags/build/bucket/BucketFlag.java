package net.t00thpick1.residence.flags.build.bucket;

import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.build.BuildFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public abstract class BucketFlag extends BuildFlag {
    public static final String FLAG = LocaleLoader.getString("BucketFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    protected Material emptyMaterial;
    protected Material fillMaterial;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != emptyMaterial) {
            return;
        }
        Player player = event.getPlayer();
        if (isAdminMode(player)) {
            return;
        }
        BlockFace face = event.getBlockFace();
        Location blockLocation = event.getBlockClicked().getLocation().add(face.getModX(), face.getModY(), face.getModZ());
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(blockLocation))) {
            player.sendMessage(LocaleLoader.getString("FlagDeny", LocaleLoader.getString("DenyBucketEmpty", FLAG)));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.getBlockClicked().getType() != fillMaterial) {
            return;
        }
        Player player = event.getPlayer();
        if (isAdminMode(player)) {
            return;
        }
        if (!allowAction(player, ResidenceAPI.getPermissionsAreaByLocation(event.getBlockClicked().getLocation()))) {
            player.sendMessage(LocaleLoader.getString("FlagDeny", LocaleLoader.getString("DenyBucketFill", FLAG)));
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        LavaBucketFlag.initialize();
        WaterBucketFlag.initialize();
    }
}

package net.t00thpick1.residence.flags.use;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.flags.build.DestroyFlag;
import net.t00thpick1.residence.flags.build.PlaceFlag;
import net.t00thpick1.residence.flags.build.bucket.BucketFlag;
import net.t00thpick1.residence.flags.use.container.ContainerFlag;
import net.t00thpick1.residence.flags.use.door.DoorFlag;
import net.t00thpick1.residence.flags.use.redstone.RedstoneFlag;
import net.t00thpick1.residence.flags.use.utilities.UtilityFlag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.permissions.PermissionGroup;
import net.t00thpick1.residence.protection.FlagManager;

public class UseFlag extends Flag {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.Use");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    public static boolean isTool(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand() == null) {
            return false;
        }
        Material handMat = event.getPlayer().getItemInHand().getType();
        return handMat == ConfigManager.getInstance().getSelectionToolType() || handMat == ConfigManager.getInstance().getInfoToolType();
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        DoorFlag.initialize();
        UtilityFlag.initialize();
        DragonEggFlag.initialize();
        CakeFlag.initialize();
        RedstoneFlag.initialize();
        ContainerFlag.initialize();
    }
}

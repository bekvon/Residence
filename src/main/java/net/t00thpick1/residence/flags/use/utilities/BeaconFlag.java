package net.t00thpick1.residence.flags.use.utilities;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class BeaconFlag extends UtilityFlag implements Listener {
    public static final String FLAG = LocaleLoader.getString("BrewFlag");
    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allow(FLAG, super.allowAction(player, area));
    }

    protected boolean shouldCheck(Material material) {
        return material == Material.BEACON;
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new BeaconFlag(), plugin);
    }
}

package net.t00thpick1.residence.flags.enviromental;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.PermissionsArea;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.plugin.Plugin;

public class IgniteFlag extends Flag implements Listener {
    private IgniteFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final IgniteFlag FLAG = new IgniteFlag(LocaleLoader.getString("Flags.Flags.Ignite"), FlagType.ANY, null);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        PermissionsArea area = ResidenceAPI.getPermissionsAreaByLocation(event.getBlock().getLocation());
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();
            if (Utilities.isAdminMode(player)) {
                return;
            }
            if (!area.allowAction(player.getName(), this)) {
                event.setCancelled(true);
                player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", this.getName()));
            }
        } else {
           if (!area.allowAction(this)) {
                event.setCancelled(true);
            }
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

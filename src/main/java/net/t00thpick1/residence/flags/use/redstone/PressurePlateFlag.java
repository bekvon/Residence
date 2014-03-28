package net.t00thpick1.residence.flags.use.redstone;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class PressurePlateFlag extends Flag implements Listener {
    private PressurePlateFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final PressurePlateFlag FLAG = new PressurePlateFlag(LocaleLoader.getString("Flags.Flags.PressurePlate"), FlagType.ANY, RedstoneFlag.FLAG);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void OnClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (Utilities.isAdminMode(player)) {
            return;
        }
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE
                && block.getType() != Material.GOLD_PLATE && block.getType() != Material.IRON_PLATE) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(block.getLocation()).allowAction(player.getName(), this)) {
            event.setCancelled(true);
            player.sendMessage(LocaleLoader.getString("Flags.Messages.FlagDeny", LocaleLoader.getString("Flags.Messages.UseFlagDeny", this.getName())));
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

package net.t00thpick1.residence.flags.enviromental.explosion;

import java.util.Iterator;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class BedExplosionFlag extends Flag implements Listener {
    private BedExplosionFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final BedExplosionFlag FLAG = new BedExplosionFlag(LocaleLoader.getString("Flags.Flags.BedExplosion"), FlagType.AREA_ONLY, ExplosionFlag.FLAG);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Should only be bed blocks
        if (event.getEntity() != null) {
            return;
        }
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Location loc = it.next().getLocation();
            if (ResidenceAPI.getPermissionsAreaByLocation(loc).allowAction(this)) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplosion(EntityDamageByBlockEvent event) {
        if (event.getCause() != DamageCause.BLOCK_EXPLOSION) {
            return;
        }
        Block block = event.getDamager();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.BED_BLOCK) {
            return;
        }
        if (ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

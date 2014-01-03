package net.t00thpick1.residence.flags.enviromental.explosion;

import java.util.Iterator;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.FlagManager;

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
    public static final String FLAG = LocaleLoader.getString("BedExplosionFlag");
    public boolean allowAction(PermissionsArea area) {
        return area.allow(FLAG, area.allow(ExplosionFlag.FLAG, super.allowAction(area)));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Should only be bed blocks
        if (event.getEntity() != null) {
            return;
        }
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Location loc = it.next().getLocation();
            if (allowAction(ResidenceAPI.getPermissionsAreaByLocation(loc))) {
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
        if (allowAction(ResidenceAPI.getPermissionsAreaByLocation(event.getEntity().getLocation()))) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new BedExplosionFlag(), plugin);
    }
}

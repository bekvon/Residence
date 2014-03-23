package net.t00thpick1.residence.flags.enviromental.spawn;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.utils.Utilities;

public class AnimalSpawnFlag extends Flag implements Listener {
    private AnimalSpawnFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final AnimalSpawnFlag FLAG = new AnimalSpawnFlag(LocaleLoader.getString("Flags.Flags.AnimalSpawn"), FlagType.AREA_ONLY, SpawnFlag.FLAG);

    private boolean shouldCheck(EntityType type) {
        return Utilities.isAnimal(type);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EntityType type = event.getEntity().getType();
        if (!shouldCheck(type)) {
            return;
        }
        if (!ResidenceAPI.getPermissionsAreaByLocation(event.getLocation()).allowAction(this)) {
            event.setCancelled(true);
        }
    }

    public static void initialize() {
        FlagManager.addFlag(FLAG);
        Plugin plugin = Residence.getInstance();
        plugin.getServer().getPluginManager().registerEvents(FLAG, plugin);
    }
}

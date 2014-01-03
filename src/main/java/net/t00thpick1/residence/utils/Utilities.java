package net.t00thpick1.residence.utils;

import net.t00thpick1.residence.Residence;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Utilities {
    public static boolean isAnimal(EntityType type) {
        return type == EntityType.BAT || type == EntityType.CHICKEN || type == EntityType.COW
                || type == EntityType.HORSE || type == EntityType.IRON_GOLEM || type == EntityType.MUSHROOM_COW
                || type == EntityType.OCELOT || type == EntityType.PIG || type == EntityType.SHEEP
                || type == EntityType.SNOWMAN || type == EntityType.SQUID || type == EntityType.VILLAGER
                || type == EntityType.WOLF || Residence.getInstance().getCompatabilityManager().isAnimal(type);
    }

    public static boolean isAdminMode(Player player) {
        return Residence.getInstance().isAdminMode(player);
    }
}

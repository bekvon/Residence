package net.t00thpick1.residence.protection;

import java.io.File;

import net.t00thpick1.residence.Residence;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class GroupManager {

    public GroupManager(File dataFolder) {
        // TODO Auto-generated constructor stub
    }

    public static String getPlayerGroup(Player player, World world) {
        return Residence.getInstance().getPermissions().getPrimaryGroup(world, player.getName());
    }

    public static double getCostPerBlock(Player player) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static double getMaxSize(Player player) {
        
        return 0;
    }

    public static int getMaxHeight(Player player) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int getMaxY(Player player) {
        // TODO Auto-generated method stub
        return 0;
    }
    public static int getMinHeight(Player player) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int getMinY(Player player) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int getMaxResidences(String name) {
        // TODO Auto-generated method stub
        return 0;
    }
}

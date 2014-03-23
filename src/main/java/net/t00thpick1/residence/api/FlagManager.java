package net.t00thpick1.residence.api;

import java.util.HashMap;
import java.util.Map;

import net.t00thpick1.residence.flags.AdminFlag;
import net.t00thpick1.residence.flags.PVPFlag;
import net.t00thpick1.residence.flags.build.BuildFlag;
import net.t00thpick1.residence.flags.build.DestroyFlag;
import net.t00thpick1.residence.flags.build.PlaceFlag;
import net.t00thpick1.residence.flags.enviromental.DamageFlag;
import net.t00thpick1.residence.flags.enviromental.FireSpreadFlag;
import net.t00thpick1.residence.flags.enviromental.HealingFlag;
import net.t00thpick1.residence.flags.enviromental.IgniteFlag;
import net.t00thpick1.residence.flags.enviromental.PistonFlag;
import net.t00thpick1.residence.flags.enviromental.explosion.ExplosionFlag;
import net.t00thpick1.residence.flags.enviromental.flow.FlowFlag;
import net.t00thpick1.residence.flags.enviromental.spawn.SpawnFlag;
import net.t00thpick1.residence.flags.move.MoveFlag;
import net.t00thpick1.residence.flags.use.UseFlag;

public class FlagManager {
    public static Flag ADMIN = AdminFlag.FLAG;
    public static Flag PVP = PVPFlag.FLAG;
    public static Flag BUILD = BuildFlag.FLAG;
    public static Flag PLACE = PlaceFlag.FLAG;
    public static Flag DESTROY = DestroyFlag.FLAG;
    public static Flag HEALING = HealingFlag.FLAG;
    
    private static Map<String, Flag> validFlags;


    public static void addFlag(Flag flag) {
        validFlags.put(flag.getName(), flag);
    }

    public static Flag getFlag(String flag) {
        return validFlags.get(flag.toLowerCase());
    }

    public static void initFlags() {
        validFlags = new HashMap<String, Flag>();
        BuildFlag.initialize();
        PVPFlag.initialize();
        HealingFlag.initialize();
        DamageFlag.initialize();
        FireSpreadFlag.initialize();
        IgniteFlag.initialize();
        PistonFlag.initialize();
        ExplosionFlag.initialize();
        FlowFlag.initialize();
        SpawnFlag.initialize();
        MoveFlag.initialize();
        UseFlag.initialize();
        AdminFlag.initialize();
    }
}

package net.t00thpick1.residence.api.flags;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.flags.Flag.FlagType;
import net.t00thpick1.residence.locale.LocaleLoader;

public class FlagManager {
    public static final Flag ADMIN = new Flag(LocaleLoader.getString("Flags.Flags.Admin"), FlagType.PLAYER_ONLY, null);
    public static final Flag HEALING = new Flag(LocaleLoader.getString("Flags.Flags.Healing"), FlagType.AREA_ONLY, null);
    public static final Flag DAMAGE = new Flag(LocaleLoader.getString("Flags.Flags.Damage"), FlagType.ANY, null);
    public static final Flag PVP = new Flag(LocaleLoader.getString("Flags.Flags.PVP"), FlagType.AREA_ONLY, null);

    public static final Flag BUILD = new Flag(LocaleLoader.getString("Flags.Flags.Build"), FlagType.ANY, null);
    public static final Flag PLACE = new Flag(LocaleLoader.getString("Flags.Flags.Place"), FlagType.ANY, BUILD);
    public static final Flag DESTROY = new Flag(LocaleLoader.getString("Flags.Flags.Destroy"), FlagType.ANY, BUILD);

    public static final Flag ENDERMANPICKUP = new Flag(LocaleLoader.getString("Flags.Flags.EndermanPickup"), FlagType.AREA_ONLY, BUILD);
    public static final Flag TRAMPLE = new Flag(LocaleLoader.getString("Flags.Flags.Trample"), FlagType.ANY, BUILD);

    public static final Flag BUCKET = new Flag(LocaleLoader.getString("Flags.Flags.Bucket"), FlagType.ANY, BUILD);
    public static final Flag LAVABUCKET = new Flag(LocaleLoader.getString("Flags.Flags.LavaBucket"), FlagType.ANY, BUCKET);
    public static final Flag WATERBUCKET = new Flag(LocaleLoader.getString("Flags.Flags.WaterBucket"), FlagType.ANY, BUCKET);

    public static final Flag FIRESPREAD = new Flag(LocaleLoader.getString("Flags.Flags.FireSpread"), FlagType.AREA_ONLY, null);
    public static final Flag IGNITE = new Flag(LocaleLoader.getString("Flags.Flags.Ignite"), FlagType.ANY, null);
    public static final Flag PISTON = new Flag(LocaleLoader.getString("Flags.Flags.Piston"), FlagType.AREA_ONLY, null);

    public static final Flag USE = new Flag(LocaleLoader.getString("Flags.Flags.Use"), FlagType.ANY, null);

    public static final Flag REDSTONE = new Flag(LocaleLoader.getString("Flags.Flags.Redstone"), FlagType.ANY, USE);
    public static final Flag BUTTON = new Flag(LocaleLoader.getString("Flags.Flags.Button"), FlagType.ANY, REDSTONE);
    public static final Flag PRESSUREPLATE = new Flag(LocaleLoader.getString("Flags.Flags.PressurePlate"), FlagType.ANY, REDSTONE);
    public static final Flag LEVER = new Flag(LocaleLoader.getString("Flags.Flags.Lever"), FlagType.ANY, REDSTONE);
    public static final Flag DIODE = new Flag(LocaleLoader.getString("Flags.Flags.Diode"), FlagType.ANY, REDSTONE);

    public static final Flag CAKE = new Flag(LocaleLoader.getString("Flags.Flags.Cake"), FlagType.ANY, USE);
    public static final Flag DRAGONEGG = new Flag(LocaleLoader.getString("Flags.Flags.DragonEgg"), FlagType.ANY, USE);

    public static final Flag DOOR = new Flag(LocaleLoader.getString("Flags.Flags.Door"), FlagType.ANY, USE);
    public static final Flag FENCEGATE = new Flag(LocaleLoader.getString("Flags.Flags.FenceGate"), FlagType.ANY, DOOR);
    public static final Flag HINGEDDOOR = new Flag(LocaleLoader.getString("Flags.Flags.HingedDoor"), FlagType.ANY, DOOR);
    public static final Flag TRAPDOOR = new Flag(LocaleLoader.getString("Flags.Flags.TrapDoor"), FlagType.ANY, DOOR);

    public static final Flag UTILITY = new Flag(LocaleLoader.getString("Flags.Flags.Utility"), FlagType.ANY, USE);
    public static final Flag ANVIL = new Flag(LocaleLoader.getString("Flags.Flags.Anvil"), FlagType.ANY, UTILITY);
    public static final Flag BEACON = new Flag(LocaleLoader.getString("Flags.Flags.Beacon"), FlagType.ANY, UTILITY);
    public static final Flag BED = new Flag(LocaleLoader.getString("Flags.Flags.Bed"), FlagType.ANY, UTILITY);
    public static final Flag ENCHANTMENTTABLE = new Flag(LocaleLoader.getString("Flags.Flags.EnchantmentTable"), FlagType.ANY, UTILITY);
    public static final Flag ENDERCHEST = new Flag(LocaleLoader.getString("Flags.Flags.EnderChest"), FlagType.ANY, UTILITY);
    public static final Flag WORKBENCH = new Flag(LocaleLoader.getString("Flags.Flags.WorkBench"), FlagType.ANY, UTILITY);

    public static final Flag CONTAINER = new Flag(LocaleLoader.getString("Flags.Flags.Container"), FlagType.ANY, USE);
    public static final Flag ITEMFRAME = new Flag(LocaleLoader.getString("Flags.Flags.ItemFrame"), FlagType.ANY, CONTAINER);
    public static final Flag CHEST = new Flag(LocaleLoader.getString("Flags.Flags.Chest"), FlagType.ANY, CONTAINER);
    public static final Flag FURNACE = new Flag(LocaleLoader.getString("Flags.Flags.Furnace"), FlagType.ANY, CONTAINER);
    public static final Flag BREW = new Flag(LocaleLoader.getString("Flags.Flags.Brew"), FlagType.ANY, CONTAINER);
    public static final Flag HOPPER = new Flag(LocaleLoader.getString("Flags.Flags.Hopper"), FlagType.ANY, CONTAINER);
    public static final Flag DROPPER = new Flag(LocaleLoader.getString("Flags.Flags.Dropper"), FlagType.ANY, CONTAINER);
    public static final Flag DISPENSER = new Flag(LocaleLoader.getString("Flags.Flags.Dispenser"), FlagType.ANY, CONTAINER);

    public static final Flag FLOW = new Flag(LocaleLoader.getString("Flags.Flags.Flow"), FlagType.AREA_ONLY, null);
    public static final Flag LAVAFLOW = new Flag(LocaleLoader.getString("Flags.Flags.LavaFlow"), FlagType.AREA_ONLY, FLOW);
    public static final Flag WATERFLOW = new Flag(LocaleLoader.getString("Flags.Flags.WaterFlow"), FlagType.AREA_ONLY, FLOW);

    public static final Flag SPAWN = new Flag(LocaleLoader.getString("Flags.Flags.Spawn"), FlagType.AREA_ONLY, null);
    public static final Flag MONSTERSPAWN = new Flag(LocaleLoader.getString("Flags.Flags.MonsterSpawn"), FlagType.AREA_ONLY, SPAWN);
    public static final Flag ANIMALSPAWN = new Flag(LocaleLoader.getString("Flags.Flags.AnimalSpawn"), FlagType.AREA_ONLY, SPAWN);

    public static final Flag EXPLOSION = new Flag(LocaleLoader.getString("Flags.Flags.Explosion"), FlagType.AREA_ONLY, null);
    public static final Flag BEDEXPLOSION = new Flag(LocaleLoader.getString("Flags.Flags.BedExplosion"), FlagType.AREA_ONLY, EXPLOSION);
    public static final Flag CREEPEREXPLOSION = new Flag(LocaleLoader.getString("Flags.Flags.Creeper"), FlagType.AREA_ONLY, EXPLOSION);
    public static final Flag FIREBALLEXPLOSION = new Flag(LocaleLoader.getString("Flags.Flags.Fireball"), FlagType.AREA_ONLY, EXPLOSION);
    public static final Flag TNTEXPLOSION = new Flag(LocaleLoader.getString("Flags.Flags.TNT"), FlagType.AREA_ONLY, EXPLOSION);
    public static final Flag WITHEREXPLOSION = new Flag(LocaleLoader.getString("Flags.Flags.WitherExplosion"), FlagType.AREA_ONLY, EXPLOSION);

    public static final Flag MOVE = new Flag(LocaleLoader.getString("Flags.Flags.Move"), FlagType.ANY, null);
    public static final Flag VEHICLEMOVE = new Flag(LocaleLoader.getString("Flags.Flags.VehicleMove"), FlagType.ANY, MOVE);
    public static final Flag TELEPORT = new Flag(LocaleLoader.getString("Flags.Flags.TP"), FlagType.ANY, MOVE);

    private static Map<String, Flag> validFlags;


    public static void addFlag(Flag flag) {
        validFlags.put(flag.getName(), flag);
        Residence.getInstance().getServer().getPluginManager().addPermission(flag.getPermission());
    }

    public static Flag getFlag(String flag) {
        return validFlags.get(flag.toLowerCase());
    }

    public static Collection<Flag> getFlags() {
        return Collections.unmodifiableCollection(validFlags.values());
    }

    public static void initFlags() {
        validFlags = new HashMap<String, Flag>();
        addFlag(ADMIN);
        addFlag(HEALING);

        addFlag(DAMAGE);
        addFlag(PVP);

        addFlag(BUILD);
        addFlag(PLACE);
        addFlag(DESTROY);

        addFlag(ENDERMANPICKUP);
        addFlag(TRAMPLE);

        addFlag(BUCKET);
        addFlag(LAVABUCKET);
        addFlag(WATERBUCKET);

        addFlag(FIRESPREAD);
        addFlag(IGNITE);
        addFlag(PISTON);

        addFlag(USE);

        addFlag(REDSTONE);
        addFlag(BUTTON);
        addFlag(PRESSUREPLATE);
        addFlag(LEVER);
        addFlag(DIODE);

        addFlag(CAKE);
        addFlag(DRAGONEGG);

        addFlag(DOOR);
        addFlag(FENCEGATE);
        addFlag(HINGEDDOOR);
        addFlag(TRAPDOOR);

        addFlag(UTILITY);
        addFlag(ANVIL);
        addFlag(BEACON);
        addFlag(BED);
        addFlag(ENCHANTMENTTABLE);
        addFlag(ENDERCHEST);
        addFlag(WORKBENCH);

        addFlag(CONTAINER);
        addFlag(ITEMFRAME);
        addFlag(CHEST);
        addFlag(FURNACE);
        addFlag(BREW);
        addFlag(HOPPER);
        addFlag(DROPPER);
        addFlag(DISPENSER);

        addFlag(FLOW);
        addFlag(WATERFLOW);
        addFlag(LAVAFLOW);

        addFlag(SPAWN);
        addFlag(MONSTERSPAWN);
        addFlag(ANIMALSPAWN);

        addFlag(EXPLOSION);
        addFlag(BEDEXPLOSION);
        addFlag(CREEPEREXPLOSION);
        addFlag(FIREBALLEXPLOSION);
        addFlag(TNTEXPLOSION);
        addFlag(WITHEREXPLOSION);

        addFlag(MOVE);
        addFlag(TELEPORT);
        addFlag(VEHICLEMOVE);
    }
}

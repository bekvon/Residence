package com.bekvon.bukkit.residence.utils;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.NewLanguage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class RandomTp {

    static int miny = 63;

    public static Location getRandomlocation(String WorldName) {

	Random random = new Random(System.currentTimeMillis());

	boolean ok = false;
	double x = 0;
	double z = 0;

	int tries = 0;

	RandomTeleport rtloc = null;

	for (RandomTeleport one : Residence.getConfigManager().getRandomTeleport()) {

	    if (!one.getWorld().equalsIgnoreCase(WorldName))
		continue;

	    rtloc = one;
	    break;
	}

	if (rtloc == null)
	    return null;

	World world = rtloc.getCenter().getWorld();

	if (world == null)
	    return null;

	int inerrange = rtloc.getMinCord();
	int outerrange = rtloc.getMaxCord();
	int maxtries = Residence.getConfigManager().getrtMaxTries();

	int centerX = rtloc.getCenter().getBlockX();
	int centerY = rtloc.getCenter().getBlockZ();

	Location loc = null;

	while (!ok) {
	    tries++;
	    if (tries > maxtries)
		return null;

	    x = random.nextInt(outerrange * 2) - outerrange + 0.5 + centerX;

	    if (x > inerrange * -1 && x < inerrange)
		continue;

	    z = random.nextInt(outerrange * 2) - outerrange + 0.5 + centerY;
	    if (z > inerrange * -1 && z < inerrange)
		continue;

	    loc = new Location(world, x, world.getMaxHeight(), z);

	    int from = (int) loc.getY();
	    for (int i = 0; i < loc.getWorld().getMaxHeight(); i++) {
		loc.setY(from - i);
		Block block = loc.getBlock();
		if (!Residence.getNms().isEmptyBlock(block)) {
		    break;
		}
	    }

	    if (loc.getBlock().getState().getType() == Material.LAVA || loc.getBlock().getState().getType() == Material.STATIONARY_LAVA)
		continue;

	    if (loc.getBlock().getState().getType() == Material.WATER || loc.getBlock().getState().getType() == Material.STATIONARY_WATER)
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);

	    if (res != null)
		continue;

	    loc.setY(loc.getY() + 2);
	    break;
	}
	return loc;
    }

    public static void performDelaydTp(final Location loc, final Player targetPlayer) {
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Residence.instance, new Runnable() {
	    public void run() {
		if (!ResidenceCommandListener.teleportDelayMap.contains(targetPlayer.getName()) && Residence.getConfigManager().getTeleportDelay() > 0)
		    return;
		else if (ResidenceCommandListener.teleportDelayMap.contains(targetPlayer.getName()))
		    ResidenceCommandListener.teleportDelayMap.remove(targetPlayer.getName());
		targetPlayer.teleport(loc);
		targetPlayer.sendMessage(ChatColor.YELLOW + NewLanguage.getMessage("Language.RandomTeleport.TeleportSuccess").replace("%1", String.valueOf(loc.getX()))
		    .replace("%2", String.valueOf(loc.getY())).replace("%3", String.valueOf(loc.getZ())));
		return;
	    }
	}, Residence.getConfigManager().getTeleportDelay() * 20L);
    }

    public static void performInstantTp(Location loc, Player targetPlayer) {
	targetPlayer.teleport(loc);
	targetPlayer.sendMessage(ChatColor.YELLOW + NewLanguage.getMessage("Language.RandomTeleport.TeleportSuccess").replace("%1", String.valueOf(loc.getX()))
	    .replace("%2", String.valueOf(loc.getY())).replace("%3", String.valueOf(loc.getZ())));
    }
}

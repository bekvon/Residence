package com.bekvon.bukkit.residence.utils;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class RandomTp {

    static int miny = 63;

    private Residence plugin;

    public RandomTp(Residence plugin) {
	this.plugin = plugin;
    }

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

	    int max = loc.getWorld().getMaxHeight();
	    max = loc.getWorld().getEnvironment() == Environment.NETHER ? 100 : max;

	    for (int i = max; i > 0; i--) {
		loc.setY(i);
		Block block = loc.getBlock();
		Block block2 = loc.clone().add(0, 1, 0).getBlock();
		Block block3 = loc.clone().add(0, -1, 0).getBlock();
		if (!Residence.getNms().isEmptyBlock(block3) && Residence.getNms().isEmptyBlock(block) && Residence.getNms().isEmptyBlock(block2)) {
		    break;
		}
	    }

	    if (!Residence.getNms().isEmptyBlock(loc.getBlock()))
		continue;

	    if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.LAVA || loc.clone().add(0, -1, 0).getBlock().getState()
		.getType() == Material.STATIONARY_LAVA)
		continue;

	    if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.WATER || loc.clone().add(0, -1, 0).getBlock().getState()
		.getType() == Material.STATIONARY_WATER)
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);

	    if (res != null)
		continue;

	    loc.setY(loc.getY() + 2);
	    break;
	}
	return loc;
    }

    public void performDelaydTp(final Location loc, final Player targetPlayer) {
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    public void run() {
		if (!ResidenceCommandListener.getTeleportDelayMap().contains(targetPlayer.getName()) && Residence.getConfigManager().getTeleportDelay() > 0)
		    return;
		else if (ResidenceCommandListener.getTeleportDelayMap().contains(targetPlayer.getName()))
		    ResidenceCommandListener.getTeleportDelayMap().remove(targetPlayer.getName());
		targetPlayer.teleport(loc);
		targetPlayer.sendMessage(ChatColor.YELLOW + Residence.getLM().getMessage("Language.RandomTeleport.TeleportSuccess", loc.getX() + "%" + loc.getY() + "%"
		    + loc.getZ()));
		return;
	    }
	}, Residence.getConfigManager().getTeleportDelay() * 20L);
    }

    public static void performInstantTp(Location loc, Player targetPlayer) {
	targetPlayer.teleport(loc);
	targetPlayer.sendMessage(ChatColor.YELLOW + Residence.getLM().getMessage("Language.RandomTeleport.TeleportSuccess", loc.getX() + "%" + loc.getY() + "%" + loc
	    .getZ()));
    }
}

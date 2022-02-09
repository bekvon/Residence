package com.bekvon.bukkit.residence.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.FileHandler.ConfigReader;

public class auto implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 150, regVar = { 0, 1, 2 }, consoleVar = { 666 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

	Player player = (Player) sender;

	String resName = null;

	int lenght = -1;
	if (args.length == 1)
	    try {
		lenght = Integer.parseInt(args[0]);
	    } catch (Exception | Error e) {

	    }

	if (args.length > 0 && lenght == -1)
	    resName = args[0];
	else
	    resName = player.getName();

	if (args.length == 2) {
	    resName = args[0];
	    try {
		lenght = Integer.parseInt(args[1]);
	    } catch (Exception ex) {
	    }
	}

	Residence.getInstance().getPlayerManager().getResidencePlayer(player).forceUpdateGroup();

	Location loc = player.getLocation();

	int minY = loc.getBlockY() - 1;
	int maxY = loc.getBlockY() + 1;
	if (plugin.getConfigManager().isSelectionIgnoreY()) {
	    minY = plugin.getSelectionManager().getSelection(player).getMinYAllowed();
	    maxY = plugin.getSelectionManager().getSelection(player).getMaxYAllowed();
	}
	loc.setY(minY);
	plugin.getSelectionManager().placeLoc1(player, loc.clone(), false);
	loc.setY(maxY);
	plugin.getSelectionManager().placeLoc2(player, loc.clone(), false);

	boolean result = resize(plugin, player, plugin.getSelectionManager().getSelectionCuboid(player), true, lenght);

	if (!result) {
	    Residence.getInstance().msg(player, lm.Area_SizeLimit);
	    return true;
	}

	ClaimedResidence collision = Residence.getInstance().getResidenceManager().collidesWithResidence(plugin.getSelectionManager().getSelectionCuboid(player));

	if (collision != null) {
	    Residence.getInstance().msg(player, lm.Area_Collision, collision.getResidenceName());
	    return null;
	}

	if (plugin.getResidenceManager().getByName(resName) != null) {
	    for (int i = 1; i < 50; i++) {
		String tempName = resName + plugin.getConfigManager().ARCIncrementFormat().replace("[number]", i + "");
		if (plugin.getResidenceManager().getByName(tempName) == null) {
		    resName = tempName;
		    break;
		}
	    }
	}

	if (resName == null)
	    resName = sender.getName() + (new Random().nextInt(99950) + 50);

	player.performCommand((resadmin ? "resadmin" : "res") + " create " + resName);

	return true;
    }

    private static int getMax(int max) {
	int arcmin = Residence.getInstance().getConfigManager().getARCSizeMin();
	int arcmax = Residence.getInstance().getConfigManager().getARCSizeMax();
	int maxV = (int) (max * (Residence.getInstance().getConfigManager().getARCSizePercentage() / 100D));
	maxV = maxV < arcmin && arcmin < max ? arcmin : maxV;
	maxV = maxV > arcmax ? arcmax : maxV;
	return maxV;
    }

    private static int getMin(int min, int max) {
	if (!Residence.getInstance().getConfigManager().isARCSizeEnabled())
	    return min;
	int percent = (int) (max * (Residence.getInstance().getConfigManager().getARCSizePercentage() / 100D));
	int arcmin = Residence.getInstance().getConfigManager().getARCSizeMin();
	int arcmax = Residence.getInstance().getConfigManager().getARCSizeMax();
	int pmin = arcmin < percent ? percent : arcmin;
	int newmin = min < pmin ? pmin : min;
	newmin = newmin > arcmax ? arcmin : newmin;
	newmin = newmin > max ? max : newmin;

	if (newmin >= max) {
	    newmin = (int) (min + ((max - min) * (Residence.getInstance().getConfigManager().getARCSizePercentage() / 100D)));
	}

	return newmin;
    }

    public static boolean resize(Residence plugin, Player player, CuboidArea cuboid, boolean checkBalance, int max) {

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();

	double cost = cuboid.getCost(group);

	double balance = 0;
	if (plugin.getEconomyManager() != null)
	    balance = plugin.getEconomyManager().getBalance(player.getName());

	direction dir = direction.Top;

	List<direction> locked = new ArrayList<direction>();

	boolean checkCollision = plugin.getConfigManager().isARCCheckCollision();
	int skipped = 0;
	int done = 0;

	int maxX = getMax(group.getMaxX());
	int maxY = getMax(group.getMaxY());
	int maxZ = getMax(group.getMaxZ());

	if (maxX > max && max > 0)
	    maxX = max;
	if (maxY > max && max > 0)
	    maxY = max;
	if (maxZ > max && max > 0)
	    maxZ = max;

	if (maxX <= 1)
	    maxX = (group.getMaxX() - group.getMinX()) / 2 + group.getMinX();

	if (maxY <= 1)
	    maxY = (group.getMaxY() - group.getMinY()) / 2 + group.getMinY();

	if (maxZ <= 1)
	    maxZ = (group.getMaxZ() - group.getMinZ()) / 2 + group.getMinZ();

	int minY = CMIWorld.getMinHeight(cuboid.getWorld());

	while (true) {
	    if (Residence.getInstance().getConfigManager().isSelectionIgnoreY() && (dir.equals(direction.Top) || dir.equals(direction.Bottom))) {
		dir = dir.getNext();
		continue;
	    }
	    done++;

	    if (skipped >= 6) {
		break;
	    }

	    // fail safe if loop keeps going on
	    if (done > 100000) {
		break;
	    }

	    if (locked.contains(dir)) {
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    CuboidArea c = new CuboidArea();
	    c.setLowLocation(cuboid.getLowLocation().clone().add(-dir.getLow().getX(), -dir.getLow().getY(), -dir.getLow().getZ()));
	    c.setHighLocation(cuboid.getHighLocation().clone().add(dir.getHigh().getX(), dir.getHigh().getY(), dir.getHigh().getZ()));

	    if ((dir.equals(direction.Top) || dir.equals(direction.Bottom)) && c.getLowVector().getY() < minY) {
		c.getLowVector().setY(minY);
		locked.add(dir);
		dir = dir.getNext();
		if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
		    skipped++;
		}
		continue;
	    }

	    if ((dir.equals(direction.Top) || dir.equals(direction.Bottom)) && c.getHighVector().getY() >= c.getWorld().getMaxHeight()) {
		c.getHighVector().setY(c.getWorld().getMaxHeight() - 1);
		locked.add(dir);
		dir = dir.getNext();
		if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
		    skipped++;
		}
		continue;
	    }

	    if (checkCollision && plugin.getResidenceManager().collidesWithResidence(c) != null) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (maxX > 0 && maxX < c.getXSize() || c.getXSize() > group.getMaxX()) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY() && (maxY > 0 && maxY < c.getYSize() || c.getYSize() > group.getMaxY() + (-group.getMinY()))) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (maxZ > 0 && maxZ < c.getZSize() || c.getZSize() > group.getMaxZ()) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    skipped = 0;

	    if (checkBalance && plugin.getConfigManager().enableEconomy()) {
		cost = c.getCost(group);
		if (cost > balance) {
		    plugin.msg(player, lm.Economy_NotEnoughMoney);
		    break;
		}
	    }

	    cuboid.setLowLocation(c.getLowLocation());
	    cuboid.setHighLocation(c.getHighLocation());

	    dir = dir.getNext();
	}

	plugin.getSelectionManager().placeLoc1(player, cuboid.getLowLocation());
	plugin.getSelectionManager().placeLoc2(player, cuboid.getHighLocation());

	cuboid = plugin.getSelectionManager().getSelectionCuboid(player);

	if (cuboid.getXSize() > group.getMaxX() || cuboid.getYSize() > group.getMaxY() + (-group.getMinY()) || cuboid.getZSize() > group.getMaxZ()) {
	    return false;
	}

	return true;
    }

    public enum direction {
	Top(new Vector(0, 1, 0), new Vector(0, 0, 0)),
	Bottom(new Vector(0, 0, 0), new Vector(0, 1, 0)),
	East(new Vector(1, 0, 0), new Vector(0, 0, 0)),
	West(new Vector(0, 0, 0), new Vector(1, 0, 0)),
	North(new Vector(0, 0, 1), new Vector(0, 0, 0)),
	South(new Vector(0, 0, 0), new Vector(0, 0, 1));

	private Vector low;
	private Vector high;

	direction(Vector low, Vector high) {
	    this.low = low;
	    this.high = high;
	}

	public Vector getLow() {
	    return low;
	}

	public Vector getHigh() {
	    return high;
	}

	public direction getNext() {
	    boolean next = false;
	    direction dir = direction.Top;
	    for (direction one : direction.values()) {
		if (next) {
		    dir = one;
		    next = false;
		    break;
		}
		if (this.equals(one)) {
		    next = true;
		}
	    }
	    return dir;
	}
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	// Main command
	c.get("Description", "Create maximum allowed residence around you");
	c.get("Info", Arrays.asList("&eUsage: &6/res auto (residence name) (radius)"));
	LocaleManager.addTabCompleteMain(this);
    }
}

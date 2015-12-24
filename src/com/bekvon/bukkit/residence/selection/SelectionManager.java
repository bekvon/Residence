/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.utils.ActionBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class SelectionManager {
    protected Map<String, Location> playerLoc1;
    protected Map<String, Location> playerLoc2;
    protected Server server;

    public static Integer id;
    public static HashMap<String, Long> normalPrintMap = new HashMap<String, Long>();
    public static HashMap<String, Long> errorPrintMap = new HashMap<String, Long>();
    public static HashMap<String, Integer> normalIDMap = new HashMap<String, Integer>();
    public static HashMap<String, Integer> errorIDMap = new HashMap<String, Integer>();

    public static final int MIN_HEIGHT = 0;

    public enum Direction {
	UP, DOWN, PLUSX, PLUSZ, MINUSX, MINUSZ
    }

    public SelectionManager(Server server) {
	this.server = server;
	playerLoc1 = Collections.synchronizedMap(new HashMap<String, Location>());
	playerLoc2 = Collections.synchronizedMap(new HashMap<String, Location>());
    }

    public void updateLocations(Player player, Location loc1, Location loc2) {
	if (loc1 != null && loc2 != null) {
	    playerLoc1.put(player.getName(), loc1);
	    playerLoc2.put(player.getName(), loc2);
	    if (Residence.getConfigManager().isSelectionIgnoreY() && hasPlacedBoth(player.getName())) {
		this.qsky(player);
		this.qbedrock(player);
	    }
//	    this.afterSelectionUpdate(player);
	}
    }

    public void placeLoc1(Player player, Location loc) {
	if (loc != null) {
	    playerLoc1.put(player.getName(), loc);
	    if (Residence.getConfigManager().isSelectionIgnoreY() && hasPlacedBoth(player.getName())) {
		this.qsky(player);
		this.qbedrock(player);
	    }
	    this.afterSelectionUpdate(player);
	}
    }

    public void placeLoc2(Player player, Location loc) {
	if (loc != null) {
	    playerLoc2.put(player.getName(), loc);
	    if (Residence.getConfigManager().isSelectionIgnoreY() && hasPlacedBoth(player.getName())) {
		this.qsky(player);
		this.qbedrock(player);
	    }
	    this.afterSelectionUpdate(player);
	}
    }

    public void afterSelectionUpdate(Player player) {
	if (hasPlacedBoth(player.getName())) {
	    NewMakeBorders(player, getPlayerLoc1(player.getName()), getPlayerLoc2(player.getName()), false);
	}
    }

    public Location getPlayerLoc1(String player) {
	return playerLoc1.get(player);
    }

    public Location getPlayerLoc2(String player) {
	return playerLoc2.get(player);
    }

    public boolean hasPlacedBoth(String player) {
	return playerLoc1.containsKey(player) && playerLoc2.containsKey(player);
    }

    public void showSelectionInfoInActionBar(Player player) {

	if (!Residence.getConfigManager().useActionBarOnSelection())
	    return;

	String pname = player.getName();
	CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));

	String Message = ChatColor.YELLOW + Residence.getLanguage().getPhrase("Selection.Total.Size") + ":" + ChatColor.DARK_AQUA + " " + cuboidArea.getSize();

	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (Residence.getConfigManager().enableEconomy())
	    Message += " " + ChatColor.YELLOW + Residence.getLanguage().getPhrase("Land.Cost") + ":" + ChatColor.DARK_AQUA + " " + ((int) Math.ceil(
		(double) cuboidArea.getSize() * group.getCostPerBlock()));

	ActionBar.send(player, Message);

    }

    public void showSelectionInfo(Player player) {
	String pname = player.getName();
	if (hasPlacedBoth(pname)) {
	    CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Selection.Total.Size") + ":" + ChatColor.DARK_AQUA + " " + cuboidArea.getSize());
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    if (Residence.getConfigManager().enableEconomy())
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Land.Cost") + ":" + ChatColor.DARK_AQUA + " " + ((int) Math.ceil(
		    (double) cuboidArea.getSize() * group.getCostPerBlock())));
	    player.sendMessage(ChatColor.YELLOW + "X" + Residence.getLanguage().getPhrase("Size") + ":" + ChatColor.DARK_AQUA + " " + cuboidArea.getXSize());
	    player.sendMessage(ChatColor.YELLOW + "Y" + Residence.getLanguage().getPhrase("Size") + ":" + ChatColor.DARK_AQUA + " " + cuboidArea.getYSize());
	    player.sendMessage(ChatColor.YELLOW + "Z" + Residence.getLanguage().getPhrase("Size") + ":" + ChatColor.DARK_AQUA + " " + cuboidArea.getZSize());
	    NewMakeBorders(player, getPlayerLoc1(pname), getPlayerLoc2(pname), false);
	} else
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
    }

    public static boolean showParticle(Player player, Location Current, boolean error) {
	if (!player.getLocation().getWorld().getName().equalsIgnoreCase(Current.getWorld().getName()))
	    return false;
	if (!error) {
	    Residence.getConfigManager().getSelectedFrame().display(0, 0, 0, 0, 1, Current, player);
	} else
	    Residence.getConfigManager().getOverlapFrame().display(0, 0, 0, 0, 1, Current, player);
	return false;
    }

    public static boolean showParticleWalls(final Player player, final Location Current, final boolean error) {
	if (!player.getLocation().getWorld().getName().equalsIgnoreCase(Current.getWorld().getName()))
	    return false;
	if (!error)
	    Residence.getConfigManager().getSelectedSides().display(0, 0, 0, 0, 1, Current, player);
	else
	    Residence.getConfigManager().getOverlapSides().display(0, 0, 0, 0, 1, Current, player);
	return false;
    }

    public void NewMakeBorders(final Player player, final Location OriginalLow, final Location OriginalHigh, final boolean error) {

	if (!Residence.getConfigManager().useVisualizer())
	    return;

	if (!error)
	    normalPrintMap.put(player.getName(), System.currentTimeMillis());
	else
	    errorPrintMap.put(player.getName(), System.currentTimeMillis());
	Bukkit.getScheduler().runTaskAsynchronously(Residence.instance, new Runnable() {
	    @Override
	    public void run() {
		MakeBorders(player, OriginalLow, OriginalHigh, error);
		return;
	    }
	});
    }

    public boolean showBounces(final Player player, final ArrayList<Location> map) {
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Residence.instance, new Runnable() {
	    public void run() {
		if (!player.isOnline() || map.size() == 0)
		    return;
		for (int i = 0; i < 20; i++) {
		    if (i < map.size()) {
			Residence.getConfigManager().getOverlapSides().display(0, 0, 0, 0, 1, map.get(i), player);
		    }
		}
		for (int i = 0; i < 20; i++) {
		    if (map.size() > 0)
			map.remove(0);
		}
		showBounces(player, map);
		return;
	    }
	}, 1L);

	return false;
    }

    public boolean showBounce(final Player player, Location OriginalLow, Location OriginalHigh) {

	CuboidArea cuboidArea = new CuboidArea(OriginalLow, OriginalHigh);
	cuboidArea.getHighLoc().add(1, 1, 1);

	Boolean NorthSide = true, WestSide = true, EastSide = true, SouthSide = true, TopSide = true, BottomSide = true;

	double Range = 16.0;

	Location loc = player.getLocation();
	loc.add(0, 1.5, 0);
	double PLLX = loc.getX() - Range;
	double PLLZ = loc.getZ() - Range;
	double PLLY = loc.getY() - Range;
	double PLHX = loc.getX() + Range;
	double PLHZ = loc.getZ() + Range;
	double PLHY = loc.getY() + Range;

	if (cuboidArea.getLowLoc().getBlockX() < PLLX) {
	    cuboidArea.getLowLoc().setX(PLLX);
	    WestSide = false;
	}

	if (cuboidArea.getHighLoc().getBlockX() > PLHX) {
	    cuboidArea.getHighLoc().setX(PLHX);
	    EastSide = false;
	}

	if (cuboidArea.getLowLoc().getBlockZ() < PLLZ) {
	    cuboidArea.getLowLoc().setZ(PLLZ);
	    NorthSide = false;
	}

	if (cuboidArea.getHighLoc().getBlockZ() > PLHZ) {
	    cuboidArea.getHighLoc().setZ(PLHZ);
	    SouthSide = false;
	}

	if (cuboidArea.getLowLoc().getBlockY() < PLLY) {
	    cuboidArea.getLowLoc().setY(PLLY);
	    BottomSide = false;
	}

	if (cuboidArea.getHighLoc().getBlockY() > PLHY) {
	    cuboidArea.getHighLoc().setY(PLHY);
	    TopSide = false;
	}

	double TX = cuboidArea.getHighLoc().getBlockX() - cuboidArea.getLowLoc().getBlockX();
	double TY = cuboidArea.getHighLoc().getBlockY() - cuboidArea.getLowLoc().getBlockY();
	double TZ = cuboidArea.getHighLoc().getBlockZ() - cuboidArea.getLowLoc().getBlockZ();

	Map<Double, Location> map = GetLocationsByData(player, loc, TX, TY, TZ, cuboidArea.getLowLoc(), EastSide, SouthSide, WestSide, NorthSide, TopSide,
	    BottomSide);

	map = sortByComparatorASC(map);

	final ArrayList<Location> locations = new ArrayList<Location>();
	for (Entry<Double, Location> one : map.entrySet()) {
	    locations.add(one.getValue());
	}

	Bukkit.getScheduler().runTaskAsynchronously(Residence.instance, new Runnable() {
	    @Override
	    public void run() {
		showBounces(player, locations);
		return;
	    }
	});

	return true;
    }

    public HashMap<Double, Location> GetLocationsByData(Player player, Location loc, Double TX, Double TY, Double TZ, Location lowLoc, Boolean EastSide,
	Boolean SouthSide, Boolean WestSide, Boolean NorthSide, Boolean TopSide, Boolean BottomSide) {
	double Range = 40D;
	HashMap<Double, Location> map = new HashMap<Double, Location>();

	Location Current = lowLoc;

	double OLX = lowLoc.getBlockX();
	double OLY = lowLoc.getBlockY();
	double OLZ = lowLoc.getBlockZ();

	double eachCollumn = Residence.getConfigManager().getVisualizerRowSpacing() / 4.0;
	double eachRow = Residence.getConfigManager().getVisualizerCollumnSpacing() / 4.0;

	// North wall
	if (NorthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (double y = eachCollumn; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (double x = eachRow; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    double dist = loc.distanceSquared(Current);
		    if (dist < Range)
			map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
		}
	    }
	}

	// South wall
	if (SouthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ + TZ);
	    for (double y = eachCollumn; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (double x = eachRow; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    double dist = loc.distanceSquared(Current);
		    if (dist < Range)
			map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
		}
	    }
	}

	// West wall
	if (WestSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (double y = eachCollumn; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (double z = eachRow; z < TZ; z += eachRow) {
		    Current.setZ(OLZ + z);
		    double dist = loc.distanceSquared(Current);
		    if (dist < Range)
			map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
		}
	    }
	}

	// East wall
	if (EastSide) {
	    Current.setX(OLX + TX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (double y = eachCollumn; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (double z = eachRow; z < TZ; z += eachRow) {
		    Current.setZ(OLZ + z);
		    double dist = loc.distanceSquared(Current);
		    if (dist < Range)
			map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
		}
	    }
	}

	// Roof wall
	if (TopSide) {
	    Current.setX(OLX);
	    Current.setY(OLY + TY);
	    Current.setZ(OLZ);
	    for (double z = eachCollumn; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		for (double x = eachRow; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    double dist = loc.distanceSquared(Current);
		    if (dist < Range)
			map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
		}
	    }
	}

	// Ground wall
	if (BottomSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (double z = eachCollumn; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		for (double x = eachRow; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    double dist = loc.distanceSquared(Current);
		    if (dist < Range)
			map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
		}
	    }
	}

	// North bottom line
	if (BottomSide && NorthSide) {
	    Current.setZ(OLZ);
	    Current.setX(OLX);
	    Current.setY(OLY);
	    for (double x = 0; x < TX; x += eachCollumn) {
		Current.setX(OLX + x);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// North top line
	if (TopSide && NorthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY + TY);
	    Current.setZ(OLZ);
	    for (double x = 0; x < TX; x += eachCollumn) {
		Current.setX(OLX + x);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// South bottom line
	if (BottomSide && SouthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ + TZ);
	    for (double x = 0; x < TX; x += eachCollumn) {
		Current.setX(OLX + x);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// South top line
	if (TopSide && SouthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY + TY);
	    Current.setZ(OLZ + TZ);
	    for (double x = 0; x <= TX; x += eachCollumn) {
		Current.setX(OLX + x);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// North - West corner
	if (WestSide && NorthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (double y = 0; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// North - East corner
	if (EastSide && NorthSide) {
	    Current.setY(OLY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ);
	    for (double y = 0; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// South - West corner
	if (SouthSide && WestSide) {
	    Current.setY(OLY);
	    Current.setX(OLX);
	    Current.setZ(OLZ + TZ);
	    for (double y = 0; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// South - East corner
	if (SouthSide && EastSide) {
	    Current.setY(OLY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ + TZ);
	    for (double y = 0; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// West bottom corner
	if (WestSide && BottomSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (double z = 0; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// East bottom corner
	if (EastSide && BottomSide) {
	    Current.setY(OLY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ);
	    for (double z = 0; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// West top corner
	if (WestSide && TopSide) {
	    Current.setY(OLY + TY);
	    Current.setX(OLX);
	    Current.setZ(OLZ + TZ);
	    for (double z = 0; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	// East top corner
	if (EastSide && TopSide) {
	    Current.setY(OLY + TY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ + TZ);
	    for (double z = 0; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		double dist = loc.distanceSquared(Current);
		if (dist < Range)
		    map.put(dist, new Location(Current.getWorld(), Current.getX(), Current.getY(), Current.getZ()));
	    }
	}

	return map;
    }

    private static Map<Double, Location> sortByComparatorASC(Map<Double, Location> unsortMap) {

	// Convert Map to List
	List<Map.Entry<Double, Location>> list = new LinkedList<Map.Entry<Double, Location>>(unsortMap.entrySet());

	// Sort list with comparator, to compare the Map values
	Collections.sort(list, new Comparator<Map.Entry<Double, Location>>() {
	    public int compare(Map.Entry<Double, Location> o1, Map.Entry<Double, Location> o2) {
		return (o1.getKey()).compareTo(o2.getKey());
	    }
	});

	// Convert sorted map back to a Map
	Map<Double, Location> sortedMap = new LinkedHashMap<Double, Location>();
	for (Iterator<Map.Entry<Double, Location>> it = list.iterator(); it.hasNext();) {
	    Map.Entry<Double, Location> entry = it.next();
	    sortedMap.put(entry.getKey(), entry.getValue());
	}
	return sortedMap;
    }

    public boolean MakeBorders(final Player player, final Location OriginalLow, final Location OriginalHigh, final boolean error) {

	CuboidArea cuboidArea = new CuboidArea(OriginalLow, OriginalHigh);
	cuboidArea.getHighLoc().add(1, 1, 1);

	Boolean NorthSide = true, WestSide = true, EastSide = true, SouthSide = true, TopSide = true, BottomSide = true;

	int Range = Residence.getConfigManager().getVisualizerRange();

	Location loc = player.getLocation();
	double PLLX = loc.getX() - Range;
	double PLLZ = loc.getZ() - Range;
	double PLLY = loc.getY() - Range;
	double PLHX = loc.getX() + Range;
	double PLHZ = loc.getZ() + Range;
	double PLHY = loc.getY() + Range;

	if (cuboidArea.getLowLoc().getBlockX() < PLLX) {
	    cuboidArea.getLowLoc().setX(PLLX);
	    WestSide = false;
	}

	if (cuboidArea.getHighLoc().getBlockX() > PLHX) {
	    cuboidArea.getHighLoc().setX(PLHX);
	    EastSide = false;
	}

	if (cuboidArea.getLowLoc().getBlockZ() < PLLZ) {
	    cuboidArea.getLowLoc().setZ(PLLZ);
	    NorthSide = false;
	}

	if (cuboidArea.getHighLoc().getBlockZ() > PLHZ) {
	    cuboidArea.getHighLoc().setZ(PLHZ);
	    SouthSide = false;
	}

	if (cuboidArea.getLowLoc().getBlockY() < PLLY) {
	    cuboidArea.getLowLoc().setY(PLLY);
	    BottomSide = false;
	}

	if (cuboidArea.getHighLoc().getBlockY() > PLHY) {
	    cuboidArea.getHighLoc().setY(PLHY);
	    TopSide = false;
	}

	double TX = cuboidArea.getHighLoc().getBlockX() - cuboidArea.getLowLoc().getBlockX();
	double TY = cuboidArea.getHighLoc().getBlockY() - cuboidArea.getLowLoc().getBlockY();
	double TZ = cuboidArea.getHighLoc().getBlockZ() - cuboidArea.getLowLoc().getBlockZ();

	if (!error && normalIDMap.containsKey(player.getName())) {
	    Bukkit.getScheduler().cancelTask(normalIDMap.get(player.getName()));
	} else if (error && errorIDMap.containsKey(player.getName())) {
	    Bukkit.getScheduler().cancelTask(errorIDMap.get(player.getName()));
	}

	DrawBounds(player, TX, TY, TZ, cuboidArea.getLowLoc(), EastSide, SouthSide, WestSide, NorthSide, TopSide, BottomSide, error);

	String planerName = player.getName();
	if (!error && !normalPrintMap.containsKey(planerName))
	    return false;
	else if (error && !errorPrintMap.containsKey(planerName))
	    return false;

	if (!error && normalPrintMap.get(planerName) + Residence.getConfigManager().getVisualizerShowFor() < System.currentTimeMillis())
	    return false;
	else if (error && errorPrintMap.get(planerName) + Residence.getConfigManager().getVisualizerShowFor() < System.currentTimeMillis())
	    return false;

	int scid = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Residence.instance, new Runnable() {
	    public void run() {

		if (player.isOnline())
		    MakeBorders(player, OriginalLow, OriginalHigh, error);
		return;
	    }
	}, Residence.getConfigManager().getVisualizerUpdateInterval() * 1L);
	if (!error)
	    normalIDMap.put(planerName, scid);
	else
	    errorIDMap.put(planerName, scid);

	return true;
    }

    public void DrawBounds(Player player, Double TX, Double TY, Double TZ, Location lowLoc, Boolean EastSide, Boolean SouthSide, Boolean WestSide, Boolean NorthSide,
	Boolean TopSide, Boolean BottomSide, boolean error) {

	Location Current = lowLoc;

	double OLX = lowLoc.getBlockX();
	double OLY = lowLoc.getBlockY();
	double OLZ = lowLoc.getBlockZ();

	int eachCollumn = Residence.getConfigManager().getVisualizerRowSpacing();
	int eachRow = Residence.getConfigManager().getVisualizerCollumnSpacing();
	// North wall
	if (NorthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (int y = 1; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (int x = 1; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    showParticleWalls(player, Current, error);
		}
	    }
	}

	// South wall
	if (SouthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ + TZ);
	    for (int y = 1; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (int x = 1; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    showParticleWalls(player, Current, error);
		}
	    }
	}

	// West wall
	if (WestSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (int y = 1; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (int z = 1; z < TZ; z += eachRow) {
		    Current.setZ(OLZ + z);
		    showParticleWalls(player, Current, error);
		}
	    }
	}

	// East wall
	if (EastSide) {
	    Current.setX(OLX + TX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (int y = 1; y < TY; y += eachCollumn) {
		Current.setY(OLY + y);
		for (int z = 1; z < TZ; z += eachRow) {
		    Current.setZ(OLZ + z);
		    showParticleWalls(player, Current, error);
		}
	    }
	}

	// Roof wall
	if (TopSide) {
	    Current.setX(OLX);
	    Current.setY(OLY + TY);
	    Current.setZ(OLZ);
	    for (int z = 1; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		for (int x = 1; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    showParticleWalls(player, Current, error);
		}
	    }
	}

	// Ground wall
	if (BottomSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (int z = 1; z < TZ; z += eachCollumn) {
		Current.setZ(OLZ + z);
		for (int x = 1; x < TX; x += eachRow) {
		    Current.setX(OLX + x);
		    showParticleWalls(player, Current, error);
		}
	    }
	}

	// North bottom line
	if (BottomSide && NorthSide) {
	    Current.setZ(OLZ);
	    Current.setX(OLX);
	    Current.setY(OLY);
	    for (int x = 0; x < TX; x++) {
		Current.setX(OLX + x);
		showParticle(player, Current, error);
	    }
	}

	// North top line
	if (TopSide && NorthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY + TY);
	    Current.setZ(OLZ);
	    for (int x = 0; x < TX; x++) {
		Current.setX(OLX + x);
		showParticle(player, Current, error);
	    }
	}

	// South bottom line
	if (BottomSide && SouthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ + TZ);
	    for (int x = 0; x < TX; x++) {
		Current.setX(OLX + x);
		showParticle(player, Current, error);
	    }
	}

	// South top line
	if (TopSide && SouthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY + TY);
	    Current.setZ(OLZ + TZ);
	    for (int x = 0; x <= TX; x++) {
		Current.setX(OLX + x);
		showParticle(player, Current, error);
	    }
	}

	// North - West corner
	if (WestSide && NorthSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (int y = 0; y < TY; y++) {
		Current.setY(OLY + y);
		showParticle(player, Current, error);
	    }
	}

	// North - East corner
	if (EastSide && NorthSide) {
	    Current.setY(OLY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ);
	    for (int y = 0; y < TY; y++) {
		Current.setY(OLY + y);
		showParticle(player, Current, error);
	    }
	}

	// South - West corner
	if (SouthSide && WestSide) {
	    Current.setY(OLY);
	    Current.setX(OLX);
	    Current.setZ(OLZ + TZ);
	    for (int y = 0; y < TY; y++) {
		Current.setY(OLY + y);
		showParticle(player, Current, error);
	    }
	}

	// South - East corner
	if (SouthSide && EastSide) {
	    Current.setY(OLY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ + TZ);
	    for (int y = 0; y < TY; y++) {
		Current.setY(OLY + y);
		showParticle(player, Current, error);
	    }
	}

	// West bottom corner
	if (WestSide && BottomSide) {
	    Current.setX(OLX);
	    Current.setY(OLY);
	    Current.setZ(OLZ);
	    for (int z = 0; z < TZ; z++) {
		Current.setZ(OLZ + z);
		showParticle(player, Current, error);
	    }
	}

	// East bottom corner
	if (EastSide && BottomSide) {
	    Current.setY(OLY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ);
	    for (int z = 0; z < TZ; z++) {
		Current.setZ(OLZ + z);
		showParticle(player, Current, error);
	    }
	}

	// West top corner
	if (WestSide && TopSide) {
	    Current.setY(OLY + TY);
	    Current.setX(OLX);
	    Current.setZ(OLZ + TZ);
	    for (int z = 0; z < TZ; z++) {
		Current.setZ(OLZ + z);
		showParticle(player, Current, error);
	    }
	}

	// East top corner
	if (EastSide && TopSide) {
	    Current.setY(OLY + TY);
	    Current.setX(OLX + TX);
	    Current.setZ(OLZ + TZ);
	    for (int z = 0; z < TZ; z++) {
		Current.setZ(OLZ + z);
		showParticle(player, Current, error);
	    }
	}
    }

    public void vert(Player player, boolean resadmin) {
	if (hasPlacedBoth(player.getName())) {
	    this.sky(player, resadmin);
	    this.bedrock(player, resadmin);
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	}
    }

    public void qsky(Player player) {
	int y1 = playerLoc1.get(player.getName()).getBlockY();
	int y2 = playerLoc2.get(player.getName()).getBlockY();
	int newy = player.getLocation().getWorld().getMaxHeight() - 1;
	if (y1 > y2)
	    playerLoc1.get(player.getName()).setY(newy);
	else
	    playerLoc2.get(player.getName()).setY(newy);
    }

    public void qbedrock(Player player) {
	int y1 = playerLoc1.get(player.getName()).getBlockY();
	int y2 = playerLoc2.get(player.getName()).getBlockY();
	if (y1 < y2) {
	    int newy = MIN_HEIGHT;
	    playerLoc1.get(player.getName()).setY(newy);
	} else {
	    int newy = MIN_HEIGHT;
	    playerLoc2.get(player.getName()).setY(newy);
	}
    }

    public void sky(Player player, boolean resadmin) {
	if (hasPlacedBoth(player.getName())) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    int y1 = playerLoc1.get(player.getName()).getBlockY();
	    int y2 = playerLoc2.get(player.getName()).getBlockY();
	    int newy = player.getLocation().getWorld().getMaxHeight() - 1;
	    if (y1 > y2) {
		if (!resadmin) {
		    if (group.getMaxHeight() < newy)
			newy = group.getMaxHeight();
		    if (newy - y2 > (group.getMaxY() - 1))
			newy = y2 + (group.getMaxY() - 1);
		}
		playerLoc1.get(player.getName()).setY(newy);
	    } else {
		if (!resadmin) {
		    if (group.getMaxHeight() < newy)
			newy = group.getMaxHeight();
		    if (newy - y1 > (group.getMaxY() - 1))
			newy = y1 + (group.getMaxY() - 1);
		}
		playerLoc2.get(player.getName()).setY(newy);
	    }
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionSky"));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	}
    }

    public void bedrock(Player player, boolean resadmin) {
	if (hasPlacedBoth(player.getName())) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    int y1 = playerLoc1.get(player.getName()).getBlockY();
	    int y2 = playerLoc2.get(player.getName()).getBlockY();
	    if (y1 < y2) {
		int newy = MIN_HEIGHT;
		if (!resadmin) {
		    if (newy < group.getMinHeight())
			newy = group.getMinHeight();
		    if (y2 - newy > (group.getMaxY() - 1))
			newy = y2 - (group.getMaxY() - 1);
		}
		playerLoc1.get(player.getName()).setY(newy);
	    } else {
		int newy = MIN_HEIGHT;
		if (!resadmin) {
		    if (newy < group.getMinHeight())
			newy = group.getMinHeight();
		    if (y1 - newy > (group.getMaxY() - 1))
			newy = y1 - (group.getMaxY() - 1);
		}
		playerLoc2.get(player.getName()).setY(newy);
	    }
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionBedrock"));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	}
    }

    public void clearSelection(Player player) {
	playerLoc1.remove(player.getName());
	playerLoc2.remove(player.getName());
    }

    public void selectChunk(Player player) {
	Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
	int xcoord = chunk.getX() * 16;
	int zcoord = chunk.getZ() * 16;
	int ycoord = MIN_HEIGHT;
	int xmax = xcoord + 15;
	int zmax = zcoord + 15;
	int ymax = player.getLocation().getWorld().getMaxHeight() - 1;
	playerLoc1.put(player.getName(), new Location(player.getWorld(), xcoord, ycoord, zcoord));
	playerLoc2.put(player.getName(), new Location(player.getWorld(), xmax, ymax, zmax));
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionSuccess"));
    }

    public boolean worldEdit(Player player) {
	player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("WorldEditNotFound"));
	return false;
    }

    public boolean worldEditUpdate(Player player) {
	player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("WorldEditNotFound"));
	return false;
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
	Location myloc = player.getLocation();
	Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
	Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);
	placeLoc1(player, loc1);
	placeLoc2(player, loc2);
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionSuccess"));
	showSelectionInfo(player);
    }

    public void modify(Player player, boolean shift, double amount) {
	if (!hasPlacedBoth(player.getName())) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	    return;
	}
	Direction d = this.getDirection(player);
	if (d == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidDirection"));
	}
	CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
	switch (d) {
	case DOWN:
	    double oldy = area.getLowLoc().getBlockY();
	    oldy = oldy - amount;
	    if (oldy < MIN_HEIGHT) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooLow"));
		oldy = MIN_HEIGHT;
	    }
	    area.getLowLoc().setY(oldy);
	    if (shift) {
		double oldy2 = area.getHighLoc().getBlockY();
		oldy2 = oldy2 - amount;
		area.getHighLoc().setY(oldy2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.Down") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.Down") + " (" + amount + ")");
	    break;
	case MINUSX:
	    double oldx = area.getLowLoc().getBlockX();
	    oldx = oldx - amount;
	    area.getLowLoc().setX(oldx);
	    if (shift) {
		double oldx2 = area.getHighLoc().getBlockX();
		oldx2 = oldx2 - amount;
		area.getHighLoc().setX(oldx2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.West") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.West") + " (" + amount + ")");
	    break;
	case MINUSZ:
	    double oldz = area.getLowLoc().getBlockZ();
	    oldz = oldz - amount;
	    area.getLowLoc().setZ(oldz);
	    if (shift) {
		double oldz2 = area.getHighLoc().getBlockZ();
		oldz2 = oldz2 - amount;
		area.getHighLoc().setZ(oldz2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.North") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.North") + " (" + amount + ")");
	    break;
	case PLUSX:
	    oldx = area.getHighLoc().getBlockX();
	    oldx = oldx + amount;
	    area.getHighLoc().setX(oldx);
	    if (shift) {
		double oldx2 = area.getLowLoc().getBlockX();
		oldx2 = oldx2 + amount;
		area.getLowLoc().setX(oldx2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.East") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.East") + " (" + amount + ")");
	    break;
	case PLUSZ:
	    oldz = area.getHighLoc().getBlockZ();
	    oldz = oldz + amount;
	    area.getHighLoc().setZ(oldz);
	    if (shift) {
		double oldz2 = area.getLowLoc().getBlockZ();
		oldz2 = oldz2 + amount;
		area.getLowLoc().setZ(oldz2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.South") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.South") + " (" + amount + ")");
	    break;
	case UP:
	    oldy = area.getHighLoc().getBlockY();
	    oldy = oldy + amount;
	    if (oldy > player.getLocation().getWorld().getMaxHeight() - 1) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooHigh"));
		oldy = player.getLocation().getWorld().getMaxHeight() - 1;
	    }
	    area.getHighLoc().setY(oldy);
	    if (shift) {
		double oldy2 = area.getLowLoc().getBlockY();
		oldy2 = oldy2 + amount;
		area.getLowLoc().setY(oldy2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.Up") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.Up") + " (" + amount + ")");
	    break;
	default:
	    break;
	}
	updateLocations(player, area.getHighLoc(), area.getLowLoc());
    }

    public boolean contract(Player player, double amount, boolean resadmin) {
	if (!hasPlacedBoth(player.getName())) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	    return false;
	}
	Direction d = this.getDirection(player);
	if (d == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidDirection"));
	}
	CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
	switch (d) {
	case DOWN:
	    double oldy = area.getHighLoc().getBlockY();
	    oldy = oldy - amount;
	    if (oldy > player.getLocation().getWorld().getMaxHeight() - 1) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooHigh"));
		oldy = player.getLocation().getWorld().getMaxHeight() - 1;
	    }
	    area.getHighLoc().setY(oldy);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.Down") + " (" + amount + ")");
	    break;
	case MINUSX:
	    double oldx = area.getHighLoc().getBlockX();
	    oldx = oldx - amount;
	    area.getHighLoc().setX(oldx);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.West") + " (" + amount + ")");
	    break;
	case MINUSZ:
	    double oldz = area.getHighLoc().getBlockZ();
	    oldz = oldz - amount;
	    area.getHighLoc().setZ(oldz);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.North") + " (" + amount + ")");
	    break;
	case PLUSX:
	    oldx = area.getLowLoc().getBlockX();
	    oldx = oldx + amount;
	    area.getLowLoc().setX(oldx);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.East") + " (" + amount + ")");
	    break;
	case PLUSZ:
	    oldz = area.getLowLoc().getBlockZ();
	    oldz = oldz + amount;
	    area.getLowLoc().setZ(oldz);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.South") + " (" + amount + ")");
	    break;
	case UP:
	    oldy = area.getLowLoc().getBlockY();
	    oldy = oldy + amount;
	    if (oldy < MIN_HEIGHT) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooLow"));
		oldy = MIN_HEIGHT;
	    }
	    area.getLowLoc().setY(oldy);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.Up") + " (" + amount + ")");
	    break;
	default:
	    break;
	}

	if (!ClaimedResidence.CheckAreaSize(player, area, resadmin))
	    return false;

	playerLoc1.put(player.getName(), area.getHighLoc());
	playerLoc2.put(player.getName(), area.getLowLoc());
	return true;
    }

    private Direction getDirection(Player player) {

	int yaw = (int) player.getLocation().getYaw();

	if (yaw < 0)
	    yaw += 360;

	yaw += 45;
	yaw %= 360;

	int facing = yaw / 90;

	float pitch = player.getLocation().getPitch();
	if (pitch < -50)
	    return Direction.UP;
	if (pitch > 50)
	    return Direction.DOWN;
	if (facing == 1) // east
	    return Direction.MINUSX;
	if (facing == 3) // west
	    return Direction.PLUSX;
	if (facing == 2) // north
	    return Direction.MINUSZ;
	if (facing == 0) // south
	    return Direction.PLUSZ;
	return null;
    }

}

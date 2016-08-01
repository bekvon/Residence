package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.SelectionSides;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class SelectionManager {
    protected Map<String, Location> playerLoc1;
    protected Map<String, Location> playerLoc2;
    protected Server server;
    private Residence plugin;

    public static HashMap<String, Long> normalPrintMap = new HashMap<String, Long>();
    public static HashMap<String, Long> errorPrintMap = new HashMap<String, Long>();
    public static HashMap<String, Integer> normalIDMap = new HashMap<String, Integer>();
    public static HashMap<String, Integer> errorIDMap = new HashMap<String, Integer>();

    public static final int MIN_HEIGHT = 0;

    Permission p = new Permission("residence.bypass.ignorey", PermissionDefault.FALSE);

    public enum Direction {
	UP, DOWN, PLUSX, PLUSZ, MINUSX, MINUSZ
    }

    public SelectionManager(Server server, Residence plugin) {
	this.plugin = plugin;
	this.server = server;
	playerLoc1 = Collections.synchronizedMap(new HashMap<String, Location>());
	playerLoc2 = Collections.synchronizedMap(new HashMap<String, Location>());
    }

    public void updateLocations(Player player, Location loc1, Location loc2) {
	if (loc1 != null && loc2 != null) {
	    playerLoc1.put(player.getName(), loc1);
	    playerLoc2.put(player.getName(), loc2);
	    updateForY(player);
	    this.afterSelectionUpdate(player);
	}
    }

    public void placeLoc1(Player player, Location loc, boolean show) {
	if (loc != null) {
	    playerLoc1.put(player.getName(), loc);
	    updateForY(player);
	    if (show)
		this.afterSelectionUpdate(player);
	}
    }

    public void placeLoc2(Player player, Location loc, boolean show) {
	if (loc != null) {
	    playerLoc2.put(player.getName(), loc);
	    updateForY(player);
	    if (show)
		this.afterSelectionUpdate(player);
	}
    }

    private void updateForY(Player player) {
	if (Residence.getConfigManager().isSelectionIgnoreY() && hasPlacedBoth(player.getName()) && !player.hasPermission(p)) {
	    this.qsky(player);
	    this.qbedrock(player);
	}
    }

    public void afterSelectionUpdate(Player player) {
	if (hasPlacedBoth(player.getName())) {
	    NewMakeBorders(player, getPlayerLoc1(player.getName()), getPlayerLoc2(player.getName()), false);
	}
    }

    public Location getPlayerLoc1(Player player) {
	return getPlayerLoc1(player.getName());
    }

    public Location getPlayerLoc1(String player) {
	return playerLoc1.get(player);
    }

    public Location getPlayerLoc2(Player player) {
	return getPlayerLoc2(player.getName());
    }

    public Location getPlayerLoc2(String player) {
	return playerLoc2.get(player);
    }

    public CuboidArea getSelectionCuboid(Player player) {
	return getSelectionCuboid(player.getName());
    }

    public CuboidArea getSelectionCuboid(String player) {
	return new CuboidArea(getPlayerLoc1(player), getPlayerLoc2(player));
    }

    public boolean hasPlacedBoth(String player) {
	return playerLoc1.containsKey(player) && playerLoc2.containsKey(player);
    }

    public void showSelectionInfoInActionBar(Player player) {

	if (!Residence.getConfigManager().useActionBarOnSelection())
	    return;

	String pname = player.getName();
	CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));

	String Message = Residence.msg(lm.Select_TotalSize, cuboidArea.getSize());

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (Residence.getConfigManager().enableEconomy())
	    Message += " " + Residence.msg(lm.General_LandCost, ((int) Math.ceil(cuboidArea.getSize() * group.getCostPerBlock())));

	Residence.getAB().send(player, Message);

    }

    public void showSelectionInfo(Player player) {
	String pname = player.getName();
	if (hasPlacedBoth(pname)) {
	    Residence.msg(player, lm.General_Separator);
	    CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));
	    Residence.msg(player, lm.Select_TotalSize, cuboidArea.getSize());

	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    PermissionGroup group = rPlayer.getGroup();

	    if (Residence.getConfigManager().enableEconomy())
		Residence.msg(player, lm.General_LandCost, ((int) Math.ceil(cuboidArea.getSize() * group.getCostPerBlock())));
	    player.sendMessage(ChatColor.YELLOW + "X" + Residence.msg(lm.General_Size, cuboidArea.getXSize()));
	    player.sendMessage(ChatColor.YELLOW + "Y" + Residence.msg(lm.General_Size, cuboidArea.getYSize()));
	    player.sendMessage(ChatColor.YELLOW + "Z" + Residence.msg(lm.General_Size, cuboidArea.getZSize()));
	    Residence.msg(player, lm.General_Separator);
	    NewMakeBorders(player, getPlayerLoc1(pname), getPlayerLoc2(pname), false);
	} else
	    Residence.msg(player, lm.Select_Points);
    }

    public static boolean showParticle(Player player, Location Current, boolean error) {
	if (!player.getLocation().getWorld().getName().equalsIgnoreCase(Current.getWorld().getName()))
	    return false;

	if (Residence.isSpigot()) {
	    if (!error) {
		player.spigot().playEffect(Current, Residence.getConfigManager().getSelectedSpigotFrame(), 0, 0, 0, 0, 0, 0, 1, 128);
	    } else
		player.spigot().playEffect(Current, Residence.getConfigManager().getOverlapSpigotFrame(), 0, 0, 0, 0, 0, 0, 1, 128);
	} else {
	    if (!error) {
		Residence.getConfigManager().getSelectedFrame().display(0, 0, 0, 0, 1, Current, player);
	    } else
		Residence.getConfigManager().getOverlapFrame().display(0, 0, 0, 0, 1, Current, player);
	}
	return false;
    }

    public static boolean showParticleWalls(final Player player, final Location Current, final boolean error) {
	if (!player.getLocation().getWorld().getName().equalsIgnoreCase(Current.getWorld().getName()))
	    return false;
	if (Residence.isSpigot()) {
	    if (!error) {
		player.spigot().playEffect(Current, Residence.getConfigManager().getSelectedSpigotSides(), 0, 0, 0, 0, 0, 0, 1, 128);
	    } else
		player.spigot().playEffect(Current, Residence.getConfigManager().getOverlapSpigotSides(), 0, 0, 0, 0, 0, 0, 1, 128);
	} else {
	    if (!error)
		Residence.getConfigManager().getSelectedSides().display(0, 0, 0, 0, 1, Current, player);
	    else
		Residence.getConfigManager().getOverlapSides().display(0, 0, 0, 0, 1, Current, player);
	}
	return false;
    }

    public void NewMakeBorders(final Player player, final Location OriginalLow, final Location OriginalHigh, final boolean error) {

	if (!Residence.getConfigManager().useVisualizer())
	    return;

	if (!error)
	    normalPrintMap.put(player.getName(), System.currentTimeMillis());
	else
	    errorPrintMap.put(player.getName(), System.currentTimeMillis());
	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {
		MakeBorders(player, OriginalLow, OriginalHigh, error);
		return;
	    }
	});
    }

    public List<Location> getLocations(Location lowLoc, Location loc, Double TX, Double TY, Double TZ, Double Range, boolean StartFromZero) {

	double eachCollumn = Residence.getConfigManager().getVisualizerRowSpacing();
	double eachRow = Residence.getConfigManager().getVisualizerCollumnSpacing();

	if (TX == 0D)
	    TX = eachCollumn + eachCollumn * 0.1;
	if (TY == 0D)
	    TY = eachRow + eachRow * 0.1;
	if (TZ == 0D)
	    TZ = eachCollumn + eachCollumn * 0.1;

	double CollumnStart = eachCollumn;
	double RowStart = eachRow;

	if (StartFromZero) {
	    CollumnStart = 0;
	    RowStart = 0;
	}

	List<Location> locList = new ArrayList<Location>();

	if (lowLoc.getWorld() != loc.getWorld())
	    return locList;

	for (double x = CollumnStart; x < TX; x += eachCollumn) {
	    Location CurrentX = lowLoc.clone();
	    if (TX > eachCollumn + eachCollumn * 0.1)
		CurrentX.add(x, 0, 0);
	    for (double y = RowStart; y < TY; y += eachRow) {
		Location CurrentY = CurrentX.clone();
		if (TY > eachRow + eachRow * 0.1)
		    CurrentY.add(0, y, 0);
		for (double z = CollumnStart; z < TZ; z += eachCollumn) {
		    Location CurrentZ = CurrentY.clone();
		    if (TZ > eachCollumn + eachCollumn * 0.1)
			CurrentZ.add(0, 0, z);
		    double dist = loc.distance(CurrentZ);
		    if (dist < Range)
			locList.add(CurrentZ.clone());
		}
	    }
	}

	return locList;
    }

    public List<Location> GetLocationsWallsByData(Location loc, Double TX, Double TY, Double TZ, Location lowLoc, SelectionSides Sides,
	double Range) {
	List<Location> locList = new ArrayList<Location>();

	// North wall
	if (Sides.ShowNorthSide())
	    locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, TY, 0D, Range, false));

	// South wall
	if (Sides.ShowSouthSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), TX, TY, 0D, Range, false));

	// West wall
	if (Sides.ShowWestSide())
	    locList.addAll(getLocations(lowLoc.clone(), loc.clone(), 0D, TY, TZ, Range, false));

	// East wall
	if (Sides.ShowEastSide())
	    locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, TY, TZ, Range, false));

	// Roof wall
	if (Sides.ShowTopSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), TX, 0D, TZ, Range, false));

	// Ground wall
	if (Sides.ShowBottomSide())
	    locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, 0D, TZ, Range, false));

	return locList;
    }

    public List<Location> GetLocationsCornersByData(Location loc, Double TX, Double TY, Double TZ, Location lowLoc, SelectionSides Sides,
	double Range) {
	List<Location> locList = new ArrayList<Location>();

	// North bottom line
	if (Sides.ShowBottomSide() && Sides.ShowNorthSide())
	    locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, 0D, 0D, Range, true));

	// North top line
	if (Sides.ShowTopSide() && Sides.ShowNorthSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), TX, 0D, 0D, Range, true));

	// South bottom line
	if (Sides.ShowBottomSide() && Sides.ShowSouthSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), TX, 0D, 0D, Range, true));

	// South top line
	if (Sides.ShowTopSide() && Sides.ShowSouthSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, TY, TZ), loc.clone(), TX, 0D, 0D, Range, true));

	// North - West corner
	if (Sides.ShowWestSide() && Sides.ShowNorthSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, 0, 0), loc.clone(), 0D, TY, 0D, Range, true));

	// North - East corner
	if (Sides.ShowEastSide() && Sides.ShowNorthSide())
	    locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, TY, 0D, Range, true));

	// South - West corner
	if (Sides.ShowSouthSide() && Sides.ShowWestSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), 0D, TY, 0D, Range, true));

	// South - East corner
	if (Sides.ShowSouthSide() && Sides.ShowEastSide())
	    locList.addAll(getLocations(lowLoc.clone().add(TX, 0, TZ), loc.clone(), 0D, TY + 1, 0D, Range, true));

	// West bottom corner
	if (Sides.ShowWestSide() && Sides.ShowBottomSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, 0, 0), loc.clone(), 0D, 0D, TZ, Range, true));

	// East bottom corner
	if (Sides.ShowEastSide() && Sides.ShowBottomSide())
	    locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, 0D, TZ, Range, true));

	// West top corner
	if (Sides.ShowWestSide() && Sides.ShowTopSide())
	    locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), 0D, 0D, TZ, Range, true));

	// East top corner
	if (Sides.ShowEastSide() && Sides.ShowTopSide())
	    locList.addAll(getLocations(lowLoc.clone().add(TX, TY, 0), loc.clone(), 0D, 0D, TZ, Range, true));

	return locList;
    }

    public boolean MakeBorders(final Player player, final Location OriginalLow, final Location OriginalHigh, final boolean error) {

	CuboidArea cuboidArea = new CuboidArea(OriginalLow, OriginalHigh);
	cuboidArea.getHighLoc().add(1, 1, 1);

	SelectionSides Sides = new SelectionSides();

	int Range = Residence.getConfigManager().getVisualizerRange();

	Location loc = player.getLocation();
//	loc = loc.add(0, 0.5, 0);
	double PLLX = loc.getBlockX() - Range;
	double PLLZ = loc.getBlockZ() - Range;
	double PLLY = loc.getBlockY() - Range;
	double PLHX = loc.getBlockX() + Range;
	double PLHZ = loc.getBlockZ() + Range;
	double PLHY = loc.getBlockY() + Range;

	if (cuboidArea.getLowLoc().getBlockX() < PLLX) {
	    cuboidArea.getLowLoc().setX(PLLX);
	    Sides.setWestSide(false);
	}

	if (cuboidArea.getHighLoc().getBlockX() > PLHX) {
	    cuboidArea.getHighLoc().setX(PLHX);
	    Sides.setEastSide(false);
	}

	if (cuboidArea.getLowLoc().getBlockZ() < PLLZ) {
	    cuboidArea.getLowLoc().setZ(PLLZ);
	    Sides.setNorthSide(false);
	}

	if (cuboidArea.getHighLoc().getBlockZ() > PLHZ) {
	    cuboidArea.getHighLoc().setZ(PLHZ);
	    Sides.setSouthSide(false);
	}

	if (cuboidArea.getLowLoc().getBlockY() < PLLY) {
	    cuboidArea.getLowLoc().setY(PLLY);
	    Sides.setBottomSide(false);
	}

	if (cuboidArea.getHighLoc().getBlockY() > PLHY) {
	    cuboidArea.getHighLoc().setY(PLHY);
	    Sides.setTopSide(false);
	}

	double TX = cuboidArea.getXSize() - 1;
	double TY = cuboidArea.getYSize() - 1;
	double TZ = cuboidArea.getZSize() - 1;

	if (!error && normalIDMap.containsKey(player.getName())) {
	    Bukkit.getScheduler().cancelTask(normalIDMap.get(player.getName()));
	} else if (error && errorIDMap.containsKey(player.getName())) {
	    Bukkit.getScheduler().cancelTask(errorIDMap.get(player.getName()));
	}

	final List<Location> locList = GetLocationsWallsByData(loc, TX, TY, TZ, cuboidArea.getLowLoc().clone(), Sides, Range);

	final List<Location> locList2 = GetLocationsCornersByData(loc, TX, TY, TZ, cuboidArea.getLowLoc().clone(), Sides, Range);

	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {

		if (Residence.isSpigot()) {
		    if (!error)
			for (Location one : locList)
			    player.spigot().playEffect(one, Residence.getConfigManager().getSelectedSpigotSides(), 0, 0, 0, 0, 0, 0, 1, 128);
		    else
			for (Location one : locList)
			    player.spigot().playEffect(one, Residence.getConfigManager().getOverlapSpigotSides(), 0, 0, 0, 0, 0, 0, 1, 128);

		    if (!error)
			for (Location one : locList2)
			    player.spigot().playEffect(one, Residence.getConfigManager().getSelectedSpigotFrame(), 0, 0, 0, 0, 0, 0, 1, 128);
		    else
			for (Location one : locList2)
			    player.spigot().playEffect(one, Residence.getConfigManager().getOverlapSpigotFrame(), 0, 0, 0, 0, 0, 0, 1, 128);
		} else {
		    if (!error)
			for (Location one : locList)
			    Residence.getConfigManager().getSelectedSides().display(0, 0, 0, 0, 1, one, player);
		    else
			for (Location one : locList)
			    Residence.getConfigManager().getOverlapSides().display(0, 0, 0, 0, 1, one, player);
		    if (!error)
			for (Location one : locList2)
			    Residence.getConfigManager().getSelectedFrame().display(0, 0, 0, 0, 1, one, player);
		    else
			for (Location one : locList2)
			    Residence.getConfigManager().getOverlapFrame().display(0, 0, 0, 0, 1, one, player);
		}

		return;
	    }
	});

	String planerName = player.getName();
	if (!error && !normalPrintMap.containsKey(planerName))
	    return false;
	else if (error && !errorPrintMap.containsKey(planerName))
	    return false;

	if (!error && normalPrintMap.get(planerName) + Residence.getConfigManager().getVisualizerShowFor() < System.currentTimeMillis())
	    return false;
	else if (error && errorPrintMap.get(planerName) + Residence.getConfigManager().getVisualizerShowFor() < System.currentTimeMillis())
	    return false;

	int scid = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
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

    public void vert(Player player, boolean resadmin) {
	if (hasPlacedBoth(player.getName())) {
	    this.sky(player, resadmin);
	    this.bedrock(player, resadmin);
	} else {
	    Residence.msg(player, lm.Select_Points);
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
	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    PermissionGroup group = rPlayer.getGroup();
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
	    Residence.msg(player, lm.Select_Sky);
	} else {
	    Residence.msg(player, lm.Select_Points);
	}
    }

    public void bedrock(Player player, boolean resadmin) {
	if (hasPlacedBoth(player.getName())) {
	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    PermissionGroup group = rPlayer.getGroup();
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
	    Residence.msg(player, lm.Select_Bedrock);
	} else {
	    Residence.msg(player, lm.Select_Points);
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
	Residence.msg(player, lm.Select_Success);
    }

    public boolean worldEdit(Player player) {
	Residence.msg(player, lm.General_WorldEditNotFound);
	return false;
    }

    public boolean worldEditUpdate(Player player) {
	Residence.msg(player, lm.General_WorldEditNotFound);
	return false;
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
	Location myloc = player.getLocation();
	Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
	Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);
	placeLoc1(player, loc1, false);
	placeLoc2(player, loc2, true);
	Residence.msg(player, lm.Select_Success);
	showSelectionInfo(player);
    }

    public void modify(Player player, boolean shift, double amount) {
	if (!hasPlacedBoth(player.getName())) {
	    Residence.msg(player, lm.Select_Points);
	    return;
	}
	Direction d = getDirection(player);
	if (d == null) {
	    Residence.msg(player, lm.Invalid_Direction);
	    return;
	}
	CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
	switch (d) {
	case DOWN:
	    double oldy = area.getLowLoc().getBlockY();
	    oldy = oldy - amount;
	    if (oldy < MIN_HEIGHT) {
		Residence.msg(player, lm.Select_TooLow);
		oldy = MIN_HEIGHT;
	    }
	    area.getLowLoc().setY(oldy);
	    if (shift) {
		double oldy2 = area.getHighLoc().getBlockY();
		oldy2 = oldy2 - amount;
		area.getHighLoc().setY(oldy2);
		Residence.msg(player, lm.Shifting_Down, amount);
	    } else
		Residence.msg(player, lm.Expanding_Down, amount);
	    break;
	case MINUSX:
	    double oldx = area.getLowLoc().getBlockX();
	    oldx = oldx - amount;
	    area.getLowLoc().setX(oldx);
	    if (shift) {
		double oldx2 = area.getHighLoc().getBlockX();
		oldx2 = oldx2 - amount;
		area.getHighLoc().setX(oldx2);
		Residence.msg(player, lm.Shifting_West, amount);
	    } else
		Residence.msg(player, lm.Expanding_West, amount);
	    break;
	case MINUSZ:
	    double oldz = area.getLowLoc().getBlockZ();
	    oldz = oldz - amount;
	    area.getLowLoc().setZ(oldz);
	    if (shift) {
		double oldz2 = area.getHighLoc().getBlockZ();
		oldz2 = oldz2 - amount;
		area.getHighLoc().setZ(oldz2);
		Residence.msg(player, lm.Shifting_North, amount);
	    } else
		Residence.msg(player, lm.Expanding_North, amount);
	    break;
	case PLUSX:
	    oldx = area.getHighLoc().getBlockX();
	    oldx = oldx + amount;
	    area.getHighLoc().setX(oldx);
	    if (shift) {
		double oldx2 = area.getLowLoc().getBlockX();
		oldx2 = oldx2 + amount;
		area.getLowLoc().setX(oldx2);
		Residence.msg(player, lm.Shifting_East, amount);
	    } else
		Residence.msg(player, lm.Expanding_East, amount);
	    break;
	case PLUSZ:
	    oldz = area.getHighLoc().getBlockZ();
	    oldz = oldz + amount;
	    area.getHighLoc().setZ(oldz);
	    if (shift) {
		double oldz2 = area.getLowLoc().getBlockZ();
		oldz2 = oldz2 + amount;
		area.getLowLoc().setZ(oldz2);
		Residence.msg(player, lm.Shifting_South, amount);
	    } else
		Residence.msg(player, lm.Expanding_South, amount);
	    break;
	case UP:
	    oldy = area.getHighLoc().getBlockY();
	    oldy = oldy + amount;
	    if (oldy > player.getLocation().getWorld().getMaxHeight() - 1) {
		Residence.msg(player, lm.Select_TooHigh);
		oldy = player.getLocation().getWorld().getMaxHeight() - 1;
	    }
	    area.getHighLoc().setY(oldy);
	    if (shift) {
		double oldy2 = area.getLowLoc().getBlockY();
		oldy2 = oldy2 + amount;
		area.getLowLoc().setY(oldy2);
		Residence.msg(player, lm.Shifting_Up, amount);
	    } else
		Residence.msg(player, lm.Expanding_Up, amount);
	    break;
	default:
	    break;
	}
	updateLocations(player, area.getHighLoc(), area.getLowLoc());
    }

    public boolean contract(Player player, double amount) {
	return contract(player, amount, false);
    }

    public boolean contract(Player player, double amount, @SuppressWarnings("unused") boolean resadmin) {
	if (!hasPlacedBoth(player.getName())) {
	    Residence.msg(player, lm.Select_Points);
	    return false;
	}
	Direction d = getDirection(player);
	if (d == null) {
	    Residence.msg(player, lm.Invalid_Direction);
	    return false;
	}
	CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
	switch (d) {
	case DOWN:
	    double oldy = area.getHighLoc().getBlockY();
	    oldy = oldy - amount;
	    if (oldy > player.getLocation().getWorld().getMaxHeight() - 1) {
		Residence.msg(player, lm.Select_TooHigh);
		oldy = player.getLocation().getWorld().getMaxHeight() - 1;
	    }
	    area.getHighLoc().setY(oldy);
	    Residence.msg(player, lm.Contracting_Down, amount);
	    break;
	case MINUSX:
	    double oldx = area.getHighLoc().getBlockX();
	    oldx = oldx - amount;
	    area.getHighLoc().setX(oldx);
	    Residence.msg(player, lm.Contracting_West, amount);
	    break;
	case MINUSZ:
	    double oldz = area.getHighLoc().getBlockZ();
	    oldz = oldz - amount;
	    area.getHighLoc().setZ(oldz);
	    Residence.msg(player, lm.Contracting_North, amount);
	    break;
	case PLUSX:
	    oldx = area.getLowLoc().getBlockX();
	    oldx = oldx + amount;
	    area.getLowLoc().setX(oldx);
	    Residence.msg(player, lm.Contracting_East, amount);
	    break;
	case PLUSZ:
	    oldz = area.getLowLoc().getBlockZ();
	    oldz = oldz + amount;
	    area.getLowLoc().setZ(oldz);
	    Residence.msg(player, lm.Contracting_South, amount);
	    break;
	case UP:
	    oldy = area.getLowLoc().getBlockY();
	    oldy = oldy + amount;
	    if (oldy < MIN_HEIGHT) {
		Residence.msg(player, lm.Select_TooLow);
		oldy = MIN_HEIGHT;
	    }
	    area.getLowLoc().setY(oldy);
	    Residence.msg(player, lm.Contracting_Up, amount);
	    break;
	default:
	    break;
	}

//	if (!ClaimedResidence.isBiggerThanMinSubzone(player, area, resadmin))
//	    return false;

	updateLocations(player, area.getHighLoc(), area.getLowLoc());
	return true;
    }

    private static Direction getDirection(Player player) {

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

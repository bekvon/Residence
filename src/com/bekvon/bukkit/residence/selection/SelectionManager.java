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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    public static final int MAX_HEIGHT = 255, MIN_HEIGHT = 0;

    public enum Direction {
	UP, DOWN, PLUSX, PLUSZ, MINUSX, MINUSZ
    }

    public SelectionManager(Server server) {
	this.server = server;
	playerLoc1 = Collections.synchronizedMap(new HashMap<String, Location>());
	playerLoc2 = Collections.synchronizedMap(new HashMap<String, Location>());
    }

    public void placeLoc1(Player player, Location loc) {
	if (loc != null) {
	    playerLoc1.put(player.getName(), loc);
	}
    }

    public void placeLoc2(Player player, Location loc) {
	if (loc != null) {
	    playerLoc2.put(player.getName(), loc);
	}
    }

    public Location getPlayerLoc1(String player) {
	return playerLoc1.get(player);
    }

    public Location getPlayerLoc2(String player) {
	return playerLoc2.get(player);
    }

    public boolean hasPlacedBoth(String player) {
	return (playerLoc1.containsKey(player) && playerLoc2.containsKey(player));
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
	if (!error) {
	    Residence.getConfigManager().getSelectedFrame().display(0, 0, 0, 0, 1, Current, player);
	} else
	    Residence.getConfigManager().getOverlapFrame().display(0, 0, 0, 0, 1, Current, player);
	return false;
    }

    public static boolean showParticleWalls(final Player player, final Location Current, final boolean error) {
	if (!error)
	    Residence.getConfigManager().getSelectedSides().display(0, 0, 0, 0, 1, Current, player);
	else
	    Residence.getConfigManager().getOverlapSides().display(0, 0, 0, 0, 1, Current, player);
	return false;
    }

    public void NewMakeBorders(final Player player, Location OriginalLow, Location OriginalHigh, boolean error) {

	if (!Residence.getConfigManager().useVisualizer())
	    return;

	if (!error)
	    normalPrintMap.put(player.getName(), System.currentTimeMillis());
	else
	    errorPrintMap.put(player.getName(), System.currentTimeMillis());
	MakeBorders(player, OriginalLow, OriginalHigh, error);
    }

    public boolean MakeBorders(final Player player, final Location OriginalLow, final Location OriginalHigh, final boolean error) {

	Boolean NorthSide = true, WestSide = true, EastSide = true, SouthSide = true, TopSide = true, BottomSide = true;

	Double OLX = OriginalLow.getX();
	if (OriginalLow.getX() > OriginalHigh.getX())
	    OLX = OriginalHigh.getX();

	Double OLY = OriginalLow.getY();
	if (OriginalLow.getY() > OriginalHigh.getY())
	    OLY = OriginalHigh.getY();

	Double OLZ = OriginalLow.getZ();
	if (OriginalLow.getZ() > OriginalHigh.getZ())
	    OLZ = OriginalHigh.getZ();

	Location Current = new Location(OriginalHigh.getWorld(), OLX, OLY, OLZ);

	Double OHX = OriginalHigh.getX() + 1;
	if (OriginalHigh.getX() < OriginalLow.getX())
	    OHX = OriginalLow.getX() + 1;

	Double OHY = OriginalHigh.getY() + 1;
	if (OriginalHigh.getY() < OriginalLow.getY())
	    OHY = OriginalLow.getY() + 1;

	Double OHZ = OriginalHigh.getZ() + 1;
	if (OriginalHigh.getZ() < OriginalLow.getZ())
	    OHZ = OriginalLow.getZ() + 1;

	int Range = Residence.getConfigManager().getVisualizerRange();

	double PLLX = player.getLocation().getX() - Range;
	double PLLZ = player.getLocation().getZ() - Range;
	double PLLY = player.getLocation().getY() - Range;
	double PLHX = player.getLocation().getX() + Range;
	double PLHZ = player.getLocation().getZ() + Range;
	double PLHY = player.getLocation().getY() + Range;

	if (OLX < PLLX) {
	    OLX = PLLX;
	    WestSide = false;
	}

	if (OHX > PLHX) {
	    OHX = PLHX;
	    EastSide = false;
	}

	if (OLZ < PLLZ) {
	    OLZ = PLLZ;
	    NorthSide = false;
	}

	if (OHZ > PLHZ) {
	    OHZ = PLHZ;
	    SouthSide = false;
	}

	if (OLY < PLLY) {
	    OLY = PLLY;
	    BottomSide = false;
	}

	if (OHY > PLHY) {
	    OHY = PLHY;
	    TopSide = false;
	}

	double TX = OLX - OHX;
	double TY = OLY - OHY;
	double TZ = OLZ - OHZ;

	if (TX < 0)
	    TX = TX * -1;
	if (TY < 0)
	    TY = TY * -1;
	if (TZ < 0)
	    TZ = TZ * -1;

	if (!error && normalIDMap.containsKey(player.getName())) {
	    Bukkit.getScheduler().cancelTask(normalIDMap.get(player.getName()));
	} else if (error && errorIDMap.containsKey(player.getName())) {
	    Bukkit.getScheduler().cancelTask(errorIDMap.get(player.getName()));
	}

	DrawBounds(player, TX, TY, TZ, OLX, OLY, OLZ, EastSide, SouthSide, WestSide, NorthSide, TopSide, BottomSide, Current, error);

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
		Bukkit.getScheduler().runTaskAsynchronously(Residence.instance, new Runnable() {
		    @Override
		    public void run() {
			if (player.isOnline())
			    MakeBorders(player, OriginalLow, OriginalHigh, error);
			return;
		    }
		});
		return;
	    }
	}, Residence.getConfigManager().getVisualizerUpdateInterval() * 1L);
	if (!error)
	    normalIDMap.put(planerName, scid);
	else
	    errorIDMap.put(planerName, scid);

	return true;
    }

    public void DrawBounds(final Player player, final Double TX, final Double TY, final Double TZ, final Double OLX, final Double OLY, final Double OLZ,
	final Boolean EastSide, final Boolean SouthSide, final Boolean WestSide, final Boolean NorthSide, final Boolean TopSide, final Boolean BottomSide,
	final Location Current, boolean error) {

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

    public void sky(Player player, boolean resadmin) {
	if (hasPlacedBoth(player.getName())) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    int y1 = playerLoc1.get(player.getName()).getBlockY();
	    int y2 = playerLoc2.get(player.getName()).getBlockY();
	    if (y1 > y2) {
		int newy = MAX_HEIGHT;
		if (!resadmin) {
		    if (group.getMaxHeight() < newy)
			newy = group.getMaxHeight();
		    if (newy - y2 > (group.getMaxY() - 1))
			newy = y2 + (group.getMaxY() - 1);
		}
		playerLoc1.get(player.getName()).setY(newy);
	    } else {
		int newy = MAX_HEIGHT;
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
	int ymax = MAX_HEIGHT;
	playerLoc1.put(player.getName(), new Location(player.getWorld(), xcoord, ycoord, zcoord));
	playerLoc2.put(player.getName(), new Location(player.getWorld(), xmax, ymax, zmax));
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionSuccess"));
    }

    public boolean worldEdit(Player player) {
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

    public void modify(Player player, boolean shift, int amount) {
	if (!hasPlacedBoth(player.getName())) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	    return;
	}
	Direction d = this.getDirection(player);
	if (d == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidDirection"));
	}
	CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
	if (d == Direction.UP) {
	    int oldy = area.getHighLoc().getBlockY();
	    oldy = oldy + amount;
	    if (oldy > MAX_HEIGHT) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooHigh"));
		oldy = MAX_HEIGHT;
	    }
	    area.getHighLoc().setY(oldy);
	    if (shift) {
		int oldy2 = area.getLowLoc().getBlockY();
		oldy2 = oldy2 + amount;
		area.getLowLoc().setY(oldy2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.Up") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.Up") + " (" + amount + ")");
	}
	if (d == Direction.DOWN) {
	    int oldy = area.getLowLoc().getBlockY();
	    oldy = oldy - amount;
	    if (oldy < MIN_HEIGHT) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooLow"));
		oldy = MIN_HEIGHT;
	    }
	    area.getLowLoc().setY(oldy);
	    if (shift) {
		int oldy2 = area.getHighLoc().getBlockY();
		oldy2 = oldy2 - amount;
		area.getHighLoc().setY(oldy2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.Down") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.Down") + " (" + amount + ")");
	}
	if (d == Direction.MINUSX) {
	    int oldx = area.getLowLoc().getBlockX();
	    oldx = oldx - amount;
	    area.getLowLoc().setX(oldx);
	    if (shift) {
		int oldx2 = area.getHighLoc().getBlockX();
		oldx2 = oldx2 - amount;
		area.getHighLoc().setX(oldx2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.West") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.West") + " (" + amount + ")");
	}
	if (d == Direction.PLUSX) {
	    int oldx = area.getHighLoc().getBlockX();
	    oldx = oldx + amount;
	    area.getHighLoc().setX(oldx);
	    if (shift) {
		int oldx2 = area.getLowLoc().getBlockX();
		oldx2 = oldx2 + amount;
		area.getLowLoc().setX(oldx2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.East") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.East") + " (" + amount + ")");
	}
	if (d == Direction.MINUSZ) {
	    int oldz = area.getLowLoc().getBlockZ();
	    oldz = oldz - amount;
	    area.getLowLoc().setZ(oldz);
	    if (shift) {
		int oldz2 = area.getHighLoc().getBlockZ();
		oldz2 = oldz2 - amount;
		area.getHighLoc().setZ(oldz2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.North") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.North") + " (" + amount + ")");
	}
	if (d == Direction.PLUSZ) {
	    int oldz = area.getHighLoc().getBlockZ();
	    oldz = oldz + amount;
	    area.getHighLoc().setZ(oldz);
	    if (shift) {
		int oldz2 = area.getLowLoc().getBlockZ();
		oldz2 = oldz2 + amount;
		area.getLowLoc().setZ(oldz2);
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Shifting.South") + " (" + amount + ")");
	    } else
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Expanding.South") + " (" + amount + ")");
	}
	playerLoc1.put(player.getName(), area.getHighLoc());
	playerLoc2.put(player.getName(), area.getLowLoc());
    }

    public boolean contract(Player player, int amount, boolean resadmin) {
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
	    int oldy = area.getHighLoc().getBlockY();
	    oldy = oldy - amount;
	    if (oldy > MAX_HEIGHT) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooHigh"));
		oldy = MAX_HEIGHT;
	    }
	    area.getHighLoc().setY(oldy);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.Down") + " (" + amount + ")");
	    break;
	case MINUSX:
	    int oldx = area.getHighLoc().getBlockX();
	    oldx = oldx - amount;
	    area.getHighLoc().setX(oldx);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Contracting.West") + " (" + amount + ")");
	    break;
	case MINUSZ:
	    int oldz = area.getHighLoc().getBlockZ();
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

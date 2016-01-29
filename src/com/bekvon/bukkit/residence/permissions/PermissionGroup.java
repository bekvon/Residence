package com.bekvon.bukkit.residence.permissions;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * 
 * @author Administrator
 * 
 *         changed by inori 03/17/2012 line 91:limits MaxHeight changed to 255
 */
public class PermissionGroup {
    protected int xmax;
    protected int ymax;
    protected int zmax;
    protected int resmax;
    protected double costperarea;
    protected double sellperarea = 0;
    protected boolean tpaccess;
    protected int subzonedepth;
    protected FlagPermissions flagPerms;
    protected Map<String, Boolean> creatorDefaultFlags;
    protected Map<String, Map<String, Boolean>> groupDefaultFlags;
    protected Map<String, Boolean> residenceDefaultFlags;
    protected boolean messageperms;
    protected String defaultEnterMessage;
    protected String defaultLeaveMessage;
    protected int maxLeaseTime;
    protected int leaseGiveTime;
    protected double renewcostperarea;
    protected boolean canBuy;
    protected boolean canSell;
    protected boolean buyIgnoreLimits;
    protected boolean cancreate;
    protected String groupname;
    protected int maxPhysical;
    protected boolean unstuck;
    protected boolean kick;
    protected int minHeight;
    protected int maxHeight;
    protected int maxRents;
    protected int maxRentables;
    protected boolean selectCommandAccess;
    protected boolean itemListAccess;

    public PermissionGroup(String name) {
	flagPerms = new FlagPermissions();
	creatorDefaultFlags = new HashMap<String, Boolean>();
	residenceDefaultFlags = new HashMap<String, Boolean>();
	groupDefaultFlags = new HashMap<String, Map<String, Boolean>>();
	groupname = name;
    }

    public PermissionGroup(String name, ConfigurationSection node) {
	this(name);
	this.parseGroup(node);
    }

    public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms) {
	this(name, node);
	flagPerms.setParent(parentFlagPerms);
    }

    private void parseGroup(ConfigurationSection limits) {
	if (limits == null) {
	    return;
	}
	cancreate = limits.getBoolean("Residence.CanCreate", false);
	resmax = limits.getInt("Residence.MaxResidences", 0);
	maxPhysical = limits.getInt("Residence.MaxAreasPerResidence", 2);
	xmax = limits.getInt("Residence.MaxEastWest", 0);
	ymax = limits.getInt("Residence.MaxUpDown", 0);
	zmax = limits.getInt("Residence.MaxNorthSouth", 0);
	minHeight = limits.getInt("Residence.MinHeight", 0);
	maxHeight = limits.getInt("Residence.MaxHeight", 255);
	tpaccess = limits.getBoolean("Residence.CanTeleport", false);
	subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);
	messageperms = limits.getBoolean("Messaging.CanChange", false);
	defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
	defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
	maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
	leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
	maxRents = limits.getInt("Rent.MaxRents", 0);
	maxRentables = limits.getInt("Rent.MaxRentables", 0);
	renewcostperarea = limits.getDouble("Economy.RenewCost", 0.02D);
	canBuy = limits.getBoolean("Economy.CanBuy", false);
	canSell = limits.getBoolean("Economy.CanSell", false);
	buyIgnoreLimits = limits.getBoolean("Economy.IgnoreLimits", false);
	costperarea = limits.getDouble("Economy.BuyCost", 0);

	if (limits.contains("Economy.SellCost"))
	    sellperarea = limits.getDouble("Economy.SellCost", 0);

	unstuck = limits.getBoolean("Residence.Unstuck", false);
	kick = limits.getBoolean("Residence.Kick", false);
	selectCommandAccess = limits.getBoolean("Residence.SelectCommandAccess", true);
	itemListAccess = limits.getBoolean("Residence.ItemListAccess", true);
	ConfigurationSection node = limits.getConfigurationSection("Flags.Permission");
	Set<String> flags = null;
	if (node != null) {
	    flags = node.getKeys(false);
	}
	if (flags != null) {
	    Iterator<String> flagit = flags.iterator();
	    while (flagit.hasNext()) {
		String flagname = flagit.next();
		boolean access = limits.getBoolean("Flags.Permission." + flagname, false);
		flagPerms.setFlag(flagname, access ? FlagState.TRUE : FlagState.FALSE);
	    }
	}
	node = limits.getConfigurationSection("Flags.CreatorDefault");
	if (node != null) {
	    flags = node.getKeys(false);
	}
	if (flags != null) {
	    Iterator<String> flagit = flags.iterator();
	    while (flagit.hasNext()) {
		String flagname = flagit.next();
		boolean access = limits.getBoolean("Flags.CreatorDefault." + flagname, false);
		creatorDefaultFlags.put(flagname, access);
	    }

	}
	node = limits.getConfigurationSection("Flags.Default");
	if (node != null) {
	    flags = node.getKeys(false);
	}
	if (flags != null) {
	    Iterator<String> flagit = flags.iterator();
	    while (flagit.hasNext()) {
		String flagname = flagit.next();
		boolean access = limits.getBoolean("Flags.Default." + flagname, false);
		residenceDefaultFlags.put(flagname, access);
	    }
	}
	node = limits.getConfigurationSection("Flags.GroupDefault");
	Set<String> groupDef = null;
	if (node != null) {
	    groupDef = node.getKeys(false);
	}
	if (groupDef != null) {
	    Iterator<String> groupit = groupDef.iterator();
	    while (groupit.hasNext()) {
		String name = groupit.next();
		Map<String, Boolean> gflags = new HashMap<String, Boolean>();
		flags = limits.getConfigurationSection("Flags.GroupDefault." + name).getKeys(false);
		Iterator<String> flagit = flags.iterator();
		while (flagit.hasNext()) {
		    String flagname = flagit.next();
		    boolean access = limits.getBoolean("Flags.GroupDefault." + name + "." + flagname, false);
		    gflags.put(flagname, access);
		}
		groupDefaultFlags.put(name, gflags);
	    }
	}
    }

    public String getGroupName() {
	return groupname;
    }

    public int getMaxX() {
	return xmax;
    }

    public int getMaxY() {
	return ymax;
    }

    public int getMaxZ() {
	return zmax;
    }

    public int getMinHeight() {
	return minHeight;
    }

    public int getMaxHeight() {
	return maxHeight;
    }

    public int getMaxZones() {
	return resmax;
    }

    public int getMaxZones(String player) {
	int max = Residence.getPlayerManager().getMaxResidences(player);
	if (max != -1)
	    return max;
	return resmax;
    }

    public double getCostPerBlock() {
	return costperarea;
    }

    public double getSellPerBlock() {
	return sellperarea;
    }

    public boolean hasTpAccess() {
	return tpaccess;
    }

    public int getMaxSubzoneDepth(String player) {
	int max = Residence.getPlayerManager().getMaxSubzones(player);
	if (max != -1)
	    return max;
	return subzonedepth;
    }

    public boolean canSetEnterLeaveMessages() {
	return messageperms;
    }

    public String getDefaultEnterMessage() {
	return defaultEnterMessage;
    }

    public String getDefaultLeaveMessage() {
	return defaultLeaveMessage;
    }

    public int getMaxLeaseTime() {
	return maxLeaseTime;
    }

    public int getLeaseGiveTime() {
	return leaseGiveTime;
    }

    public double getLeaseRenewCost() {
	return renewcostperarea;
    }

    public boolean canBuyLand() {
	return canBuy;
    }

    public boolean canSellLand() {
	return canSell;
    }

    public int getMaxRents(String player) {
	int max = Residence.getPlayerManager().getMaxRents(player);
	if (max != -1)
	    return max;
	return maxRents;
    }

    public int getMaxRentables() {
	return maxRentables;
    }

    public boolean buyLandIgnoreLimits() {
	return buyIgnoreLimits;
    }

    public boolean hasUnstuckAccess() {
	return unstuck;
    }

    public boolean hasKickAccess() {
	return kick;
    }

    public int getMaxPhysicalPerResidence() {
	return maxPhysical;
    }

    public Set<Entry<String, Boolean>> getDefaultResidenceFlags() {
	return residenceDefaultFlags.entrySet();
    }

    public Set<Entry<String, Boolean>> getDefaultCreatorFlags() {
	return creatorDefaultFlags.entrySet();
    }

    public Set<Entry<String, Map<String, Boolean>>> getDefaultGroupFlags() {
	return groupDefaultFlags.entrySet();
    }

    public boolean canCreateResidences() {
	return cancreate;
    }

    public boolean hasFlagAccess(String flag) {
	return flagPerms.has(flag, false);
    }

    public boolean inLimits(CuboidArea area) {
	if (area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax) {
	    return false;
	}
	return true;
    }

    public boolean selectCommandAccess() {
	return selectCommandAccess;
    }

    public boolean itemListAccess() {
	return itemListAccess;
    }

    public void printLimits(Player player, OfflinePlayer target, boolean resadmin) {
	player.sendMessage(ChatColor.GRAY + Residence.getLanguage().getPhrase("LimitsTop"));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsPGroup", Residence.getPermissionManager().getPermissionsGroup(target.getName(),
	    Residence.getConfigManager().getDefaultWorld())));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsRGroup", groupname));
	if (target.isOnline() && resadmin)
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsAdmin", String.valueOf(Residence.getPermissionManager().isResidenceAdmin(
		player))));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsCanCreate", String.valueOf(cancreate)));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMaxRes", String.valueOf(getMaxZones(target.getName()))));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMaxEW", String.valueOf(xmax)));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMaxNS", String.valueOf(zmax)));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMaxUD", String.valueOf(ymax)));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMinMax", String.valueOf(minHeight) + "|" + String.valueOf(maxHeight)));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMaxSub", String.valueOf(getMaxSubzoneDepth(target.getName()))));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsMaxRents", String.valueOf(getMaxRents(target.getName()))));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsEnterLeave", String.valueOf(messageperms)));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsNumberOwn", String.valueOf(Residence.getResidenceManager().getOwnedZoneCount(target
	    .getName()))));
	if (Residence.getEconomyManager() != null) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsCost", String.valueOf(costperarea)));
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsSell", String.valueOf(sellperarea)));
	}
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsFlag", flagPerms.listFlags()));
	if (Residence.getConfigManager().useLeases()) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsFlag", String.valueOf(maxLeaseTime)));
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsFlag", String.valueOf(leaseGiveTime)));
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LimitsFlag", String.valueOf(renewcostperarea)));
	}
	player.sendMessage(ChatColor.GRAY + Residence.getLanguage().getPhrase("LimitsBottom"));
    }

}

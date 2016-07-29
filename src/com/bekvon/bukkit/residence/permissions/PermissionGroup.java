package com.bekvon.bukkit.residence.permissions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public class PermissionGroup {
    protected int xmax;
    protected int ymax;
    protected int zmax;

    protected int xmin;
    protected int ymin;
    protected int zmin;

    protected int Subzonexmax;
    protected int Subzoneymax;
    protected int Subzonezmax;

    protected int Subzonexmin;
    protected int Subzoneymin;
    protected int Subzonezmin;

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
    protected int MaxRentDays = -1;
    protected int maxRentables;
    protected boolean selectCommandAccess;
    protected boolean itemListAccess;
    protected int priority = 0;

    public PermissionGroup(String name) {
	flagPerms = new FlagPermissions();
	creatorDefaultFlags = new HashMap<String, Boolean>();
	residenceDefaultFlags = new HashMap<String, Boolean>();
	groupDefaultFlags = new HashMap<String, Map<String, Boolean>>();
	groupname = name;
    }

    public void setPriority(int number) {
	this.priority = number;
    }

    public int getPriority() {
	return this.priority;
    }

    public PermissionGroup(String name, ConfigurationSection node) {
	this(name);
	this.parseGroup(node);
    }

    public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms) {
	this(name, node);
	flagPerms.setParent(parentFlagPerms);
    }

    public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms, int priority) {
	this(name, node);
	flagPerms.setParent(parentFlagPerms);
	this.priority = priority;
    }

    private void parseGroup(ConfigurationSection limits) {
	if (limits == null) {
	    return;
	}
	cancreate = limits.getBoolean("Residence.CanCreate", false);
	resmax = limits.getInt("Residence.MaxResidences", 0);
	maxPhysical = limits.getInt("Residence.MaxAreasPerResidence", 2);

	xmax = limits.getInt("Residence.MaxEastWest", 0);
	xmin = limits.getInt("Residence.MinEastWest", 0);
	xmin = xmin > xmax ? xmax : xmin;

	ymax = limits.getInt("Residence.MaxUpDown", 0);
	ymin = limits.getInt("Residence.MinUpDown", 0);
	ymin = ymin > ymax ? ymax : ymin;

	zmax = limits.getInt("Residence.MaxNorthSouth", 0);
	zmin = limits.getInt("Residence.MinNorthSouth", 0);
	zmin = zmin > zmax ? zmax : zmin;

	minHeight = limits.getInt("Residence.MinHeight", 0);
	maxHeight = limits.getInt("Residence.MaxHeight", 255);
	tpaccess = limits.getBoolean("Residence.CanTeleport", false);
	subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);

	Subzonexmax = limits.getInt("Residence.SubzoneMaxEastWest", xmax);
	Subzonexmax = xmax < Subzonexmax ? xmax : Subzonexmax;
	Subzonexmin = limits.getInt("Residence.SubzoneMinEastWest", 0);
	Subzonexmin = Subzonexmin > Subzonexmax ? Subzonexmax : Subzonexmin;

	Subzoneymax = limits.getInt("Residence.SubzoneMaxUpDown", ymax);
	Subzoneymax = ymax < Subzoneymax ? ymax : Subzoneymax;
	Subzoneymin = limits.getInt("Residence.SubzoneMinUpDown", 0);
	Subzoneymin = Subzoneymin > Subzoneymax ? Subzoneymax : Subzoneymin;

	Subzonezmax = limits.getInt("Residence.SubzoneMaxNorthSouth", zmax);
	Subzonezmax = zmax < Subzonezmax ? zmax : Subzonezmax;
	Subzonezmin = limits.getInt("Residence.SubzoneMinNorthSouth", 0);
	Subzonezmin = Subzonezmin > Subzonezmax ? Subzonezmax : Subzonezmin;

	messageperms = limits.getBoolean("Messaging.CanChange", false);
	defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
	defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
	maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
	leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
	maxRents = limits.getInt("Rent.MaxRents", 0);

	if (limits.contains("Rent.MaxRentDays"))
	    MaxRentDays = limits.getInt("Rent.MaxRentDays", -1);

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

    public int getMinX() {
	return xmin;
    }

    public int getMinY() {
	return ymin;
    }

    public int getMinZ() {
	return zmin;
    }

    public int getSubzoneMaxX() {
	return Subzonexmax;
    }

    public int getSubzoneMaxY() {
	return Subzoneymax;
    }

    public int getSubzoneMaxZ() {
	return Subzonezmax;
    }

    public int getSubzoneMinX() {
	return Subzonexmin;
    }

    public int getSubzoneMinY() {
	return Subzoneymin;
    }

    public int getSubzoneMinZ() {
	return Subzonezmin;
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

    public double getCostPerBlock() {
	return costperarea;
    }

    public double getSellPerBlock() {
	return sellperarea;
    }

    public boolean hasTpAccess() {
	return tpaccess;
    }

    public int getMaxSubzoneDepth() {
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

    public int getMaxRents() {
	return maxRents;
    }

    public int getMaxRentDays() {
	return MaxRentDays;
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

//    public boolean inLimits(CuboidArea area) {
//	if (area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax) {
//	    return false;
//	}
//	return true;
//    }
//
//    public boolean inLimitsSubzone(CuboidArea area) {
//	if (area.getXSize() > Subzonexmax || area.getYSize() > Subzoneymax || area.getZSize() > Subzonezmax) {
//	    return false;
//	}
//	return true;
//    }

    public boolean selectCommandAccess() {
	return selectCommandAccess;
    }

    public boolean itemListAccess() {
	return itemListAccess;
    }

    public void printLimits(CommandSender player, OfflinePlayer target, boolean resadmin) {

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(target.getName());
	PermissionGroup group = rPlayer.getGroup();

	Residence.msg(player, lm.General_Separator);
	Residence.msg(player, lm.Limits_PGroup, Residence.getPermissionManager().getPermissionsGroup(target.getName(),
	    target.isOnline() ? Bukkit.getPlayer(target.getName()).getWorld().getName() : Residence.getConfigManager().getDefaultWorld()));
	Residence.msg(player, lm.Limits_RGroup, group.getGroupName());
	if (target.isOnline() && resadmin)
	    Residence.msg(player, lm.Limits_Admin, Residence.getPermissionManager().isResidenceAdmin(player));
	Residence.msg(player, lm.Limits_CanCreate, group.canCreateResidences());
	Residence.msg(player, lm.Limits_MaxRes, rPlayer.getMaxRes());
	Residence.msg(player, lm.Limits_NumberOwn, rPlayer.getResAmount());
	Residence.msg(player, lm.Limits_MaxEW, group.xmin + "-" + group.xmax);
	Residence.msg(player, lm.Limits_MaxNS, group.zmin + "-" + group.zmax);
	Residence.msg(player, lm.Limits_MaxUD, group.ymin + "-" + group.ymax);
	Residence.msg(player, lm.Limits_MinMax, group.minHeight, group.maxHeight);
	Residence.msg(player, lm.Limits_MaxSub, rPlayer.getMaxSubzones());
	Residence.msg(player, lm.Limits_MaxRents, rPlayer.getMaxRents() + (getMaxRentDays() != -1 ? Residence.msg(lm.Limits_MaxRentDays, getMaxRentDays())
	    : ""));
	Residence.msg(player, lm.Limits_EnterLeave, group.messageperms);
	if (Residence.getEconomyManager() != null) {
	    Residence.msg(player, lm.Limits_Cost, group.costperarea);
	    Residence.msg(player, lm.Limits_Sell, group.sellperarea);
	}
	Residence.msg(player, lm.Limits_Flag, group.flagPerms.listFlags());
	if (Residence.getConfigManager().useLeases()) {
	    Residence.msg(player, lm.Limits_MaxDays, group.maxLeaseTime);
	    Residence.msg(player, lm.Limits_LeaseTime, group.leaseGiveTime);
	    Residence.msg(player, lm.Limits_RenewCost, group.renewcostperarea);
	}
	Residence.msg(player, lm.General_Separator);
    }

}

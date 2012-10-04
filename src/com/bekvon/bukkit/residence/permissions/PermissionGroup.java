/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

/**
 *
 * @author Administrator
 * 
 * changed by inori 03/17/2012
 * line 91:limits MaxHeight changed to 255
 */
public class PermissionGroup {
	protected int xmax;
	protected int ymax;
	protected int zmax;
	protected int resmax;
	protected double costperarea;
	protected int subzonedepth;
	protected Map<String,Boolean> creatorDefaultFlags;
	protected Map<String,Map<String,Boolean>> groupDefaultFlags;
	protected Map<String,Boolean> residenceDefaultFlags;
	protected String defaultEnterMessage;
	protected String defaultLeaveMessage;
	protected int maxLeaseTime;
	protected int leaseGiveTime;
	protected double renewcostperarea;
	protected String groupname;
	protected int maxPhysical;
	protected int minHeight;
	protected int maxHeight;
	protected int maxRents;
	protected int maxRentables;

	public PermissionGroup(String name) {
		creatorDefaultFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
		residenceDefaultFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
		groupDefaultFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
		groupname = name;
	}

	public PermissionGroup(String name, ConfigurationSection node) {
		this(name);
		this.parseGroup(node);
	}

	public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms) {
		this(name,node);
		flagPerms.setParent(parentFlagPerms);
	}

	private void parseGroup(ConfigurationSection limits) {
		if(limits == null) {
			return;
		}
		resmax = limits.getInt("Residence.MaxResidences", 0);
		maxPhysical = limits.getInt("Residence.MaxAreasPerResidence",2);
		xmax = limits.getInt("Residence.MaxEastWest", 0);
		ymax = limits.getInt("Residence.MaxUpDown", 0);
		zmax = limits.getInt("Residence.MaxNorthSouth", 0);
		minHeight = limits.getInt("Residence.MinHeight", 0);
		maxHeight = limits.getInt("Residence.MaxHeight", 255);
		subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);
		defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
		defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
		maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
		leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
		maxRents = limits.getInt("Rent.MaxRents", 0);
		maxRentables = limits.getInt("Rent.MaxRentables", 0);
		renewcostperarea = limits.getDouble("Economy.RenewCost", 0.02D);
		costperarea = limits.getDouble("Economy.BuyCost", 0);
		ConfigurationSection node = limits.getConfigurationSection("Flags.CreatorDefault");
		Set<String> flags = null;
		if(node != null) {
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
		if(node != null) {
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
		if(node != null) {
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
	public double getCostPerBlock() {
		return costperarea;
	}

	public int getMaxSubzoneDepth() {
		return subzonedepth;
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
	public int getMaxRents() {
		return maxRents;
	}
	public int getMaxRentables() {
		return maxRentables;
	}
	public int getMaxPhysicalPerResidence() {
		return maxPhysical;
	}
	public Set<Entry<String,Boolean>> getDefaultResidenceFlags() {
		return residenceDefaultFlags.entrySet();
	}
	public Set<Entry<String,Boolean>> getDefaultCreatorFlags() {
		return creatorDefaultFlags.entrySet();
	}
	public Set<Entry<String,Map<String,Boolean>>> getDefaultGroupFlags() {
		return groupDefaultFlags.entrySet();
	}
	public boolean inLimits(CuboidArea area) {
		if(area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax)
		{
			return false;
		}
		return true;
	}
	public void printLimits(Player player) {
		PermissionManager pm = Residence.getPermissionManager();
		for(String flag : FlagPermissions.getValidFlags()){
			flagPerms.put(flag, pm.hasAuthority(player, "residence."+flag));
		}
		player.sendMessage(ChatColor.GRAY+"---------------------------");
		player.sendMessage(ChatColor.YELLOW+"Permissions Group:"+ChatColor.DARK_AQUA+" "+pm.getPermissionsGroup(player));
		player.sendMessage(ChatColor.YELLOW+"Residence Group:"+ChatColor.DARK_AQUA+" "+groupname);
		player.sendMessage(ChatColor.YELLOW+"Residence Admin:"+ChatColor.DARK_AQUA+" " + pm.isResidenceAdmin(player));
		player.sendMessage(ChatColor.YELLOW+"Can Create Residences:"+ChatColor.DARK_AQUA+" "+pm.hasAuthority(player, "residence.create"));
		player.sendMessage(ChatColor.YELLOW+"Max Residences:"+ChatColor.DARK_AQUA+" "+resmax);
		player.sendMessage(ChatColor.YELLOW+"Max East/West Size:"+ChatColor.DARK_AQUA+" "+xmax);
		player.sendMessage(ChatColor.YELLOW+"Max North/South Size:"+ChatColor.DARK_AQUA+" "+zmax);
		player.sendMessage(ChatColor.YELLOW+"Max Up/Down Size:"+ChatColor.DARK_AQUA+" "+ymax);
		player.sendMessage(ChatColor.YELLOW+"Min/Max Protection Height:"+ChatColor.DARK_AQUA+" "+minHeight+ " to " + maxHeight);
		player.sendMessage(ChatColor.YELLOW+"Max Subzone Depth:"+ChatColor.DARK_AQUA+" "+subzonedepth);
		player.sendMessage(ChatColor.YELLOW+"Can Set Enter/Leave Messages:"+ChatColor.DARK_AQUA+" "+pm.hasAuthority(player, "residence.messages"));
		player.sendMessage(ChatColor.YELLOW+"Number of Residences you own:"+ChatColor.DARK_AQUA+" " + Residence.getResidenceManager().getOwnedZoneCount(player.getName()));
		if(Residence.getEconomyManager()!=null) {
			player.sendMessage(ChatColor.YELLOW+"Residence Cost Per Block:"+ChatColor.DARK_AQUA+" " + costperarea);
		}
		player.sendMessage(ChatColor.YELLOW+"Flag Permissions:"+ChatColor.DARK_AQUA+" " + flagPerms.listFlags());
		if(Residence.getConfigManager().useLeases()) {
			player.sendMessage(ChatColor.YELLOW+"Max Lease Days:"+ChatColor.DARK_AQUA+" " + maxLeaseTime);
			player.sendMessage(ChatColor.YELLOW+"Lease Time Given on Renew:"+ChatColor.DARK_AQUA+" " + leaseGiveTime);
			player.sendMessage(ChatColor.YELLOW+"Renew Cost Per Block:"+ChatColor.DARK_AQUA+" " + renewcostperarea);
		}
		player.sendMessage(ChatColor.GRAY+"---------------------------");
	}

}

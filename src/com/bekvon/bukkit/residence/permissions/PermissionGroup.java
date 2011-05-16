/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.PermissionList;
import com.bekvon.bukkit.residence.protection.PermissionList.FlagState;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author Administrator
 */
public class PermissionGroup {
        protected int xmax;
        protected int ymax;
        protected int zmax;
        protected int resmax;
        protected double costperarea;
        protected boolean tpaccess;
        protected int subzonedepth;
        protected PermissionList flagPerms;
        protected Map<String,Boolean> creatorDefaultFlags;
        protected Map<String,Map<String,Boolean>> groupDefaultFlags;
        protected Map<String,Boolean> residenceDefaultFlags;
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
        protected int minHeight;
        protected int maxHeight;
        protected boolean canRent;
        protected boolean canMakeForRent;

        public PermissionGroup(String name)
        {
            flagPerms = new PermissionList();
            creatorDefaultFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
            residenceDefaultFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
            groupDefaultFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
            groupname = name;
        }
        
        public PermissionGroup(String name, ConfigurationNode node)
        {
            this(name);
            this.parseGroup(node);
        }

        public PermissionGroup(String name, ConfigurationNode node, PermissionList parentFlagPerms)
        {
            this(name,node);
            flagPerms.setParent(parentFlagPerms);
        }

    private void parseGroup(ConfigurationNode limits) {
        if(limits == null)
            return;
        cancreate = limits.getBoolean("Residence.CanCreate", false);
        resmax = limits.getInt("Residence.MaxResidences", 0);
        maxPhysical = limits.getInt("Residence.MaxAreasPerResidence",2);
        xmax = limits.getInt("Residence.MaxEastWest", 0);
        ymax = limits.getInt("Residence.MaxUpDown", 0);
        zmax = limits.getInt("Residence.MaxNorthSouth", 0);
        minHeight = limits.getInt("Residence.MinHeight", 0);
        maxHeight = limits.getInt("Residence.MaxHeight", 127);
        tpaccess = limits.getBoolean("Residence.CanTeleport", false);
        subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);
        messageperms = limits.getBoolean("Messaging.CanChange", false);
        defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
        defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
        maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
        leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
        canRent = limits.getBoolean("Rent.CanRent", false);
        canMakeForRent = limits.getBoolean("Rent.CanSetForRent", false);
        renewcostperarea = limits.getDouble("Economy.RenewCost", 0.02D);
        canBuy = limits.getBoolean("Economy.CanBuy", false);
        canSell = limits.getBoolean("Economy.CanSell", false);
        buyIgnoreLimits = limits.getBoolean("Economy.IgnoreLimits", false);
        costperarea = limits.getDouble("Economy.BuyCost", 0);
        unstuck = limits.getBoolean("Residence.Unstuck", false);
        List<String> flags = limits.getKeys("Flags.Permission");
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.Permission." + flagname, false);
                flagPerms.set(flagname, access ? FlagState.TRUE : FlagState.FALSE);
            }
        }
        flags = limits.getKeys("Flags.CreatorDefault");
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.CreatorDefault." + flagname, false);
                creatorDefaultFlags.put(flagname, access);
            }

        }
        flags = limits.getKeys("Flags.Default");
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.Default." + flagname, false);
                residenceDefaultFlags.put(flagname, access);
            }
        }
        List<String> groupDef = limits.getKeys("Flags.GroupDefault");
        if (groupDef != null) {
            Iterator<String> groupit = groupDef.iterator();
            while (groupit.hasNext()) {
                String name = groupit.next();
                Map<String, Boolean> gflags = new HashMap<String, Boolean>();
                flags = limits.getKeys("Flags.GroupDefault." + name);
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

    public int getMinHeight()
    {
        return minHeight;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public int getMaxZones() {
        return resmax;
    }
    public double getCostPerBlock()
    {
        return costperarea;
    }
    public boolean hasTpAccess()
    {
        return tpaccess;
    }
    public int getMaxSubzoneDepth()
    {
        return subzonedepth;
    }
    public boolean canSetEnterLeaveMessages()
    {
        return messageperms;
    }
    public String getDefaultEnterMessage()
    {
        return defaultEnterMessage;
    }
    public String getDefaultLeaveMessage()
    {
        return defaultLeaveMessage;
    }
    public int getMaxLeaseTime()
    {
        return maxLeaseTime;
    }
    public int getLeaseGiveTime()
    {
        return leaseGiveTime;
    }
    public double getLeaseRenewCost()
    {
        return renewcostperarea;
    }
    public boolean canBuyLand()
    {
        return canBuy;
    }
    public boolean canSellLand()
    {
        return canSell;
    }
    public boolean canRentLand()
    {
        return canRent;
    }
    public boolean canMakeLandForRent()
    {
        return canMakeForRent;
    }
    public boolean buyLandIgnoreLimits()
    {
        return buyIgnoreLimits;
    }
    public boolean hasUnstuckAccess()
    {
        return unstuck;
    }
    public int getMaxPhysicalPerResidence()
    {
        return maxPhysical;
    }
    public Set<Entry<String,Boolean>> getDefaultResidenceFlags()
    {
        return residenceDefaultFlags.entrySet();
    }
    public Set<Entry<String,Boolean>> getDefaultCreatorFlags()
    {
        return creatorDefaultFlags.entrySet();
    }
    public Set<Entry<String,Map<String,Boolean>>> getDefaultGroupFlags()
    {
        return groupDefaultFlags.entrySet();
    }

    public boolean canCreateResidences()
    {
        return cancreate;
    }
    public boolean hasFlagAccess(String flag)
    {
        return flagPerms.has(flag, false);
    }

    public boolean inLimits(CuboidArea area)
    {
        if(area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax)
        {
            return false;
        }
        return true;
    }

    public void printLimits(Player player)
    {
        player.sendMessage("§7---------------------------");
        player.sendMessage("§eGroup:§3 "+groupname);
        player.sendMessage("§eResidence Admin:§3 " + Residence.getPermissionManager().isResidenceAdmin(player));
        player.sendMessage("§eCan Create Residences:§3 "+cancreate);
        player.sendMessage("§eMax Residences:§3 "+resmax);
        player.sendMessage("§eMax East/West Size:§3 "+xmax);
        player.sendMessage("§eMax North/South Size:§3 "+zmax);
        player.sendMessage("§eMax Up/Down Size:§3 "+ymax);
        player.sendMessage("§eMin/Max Protection Height:§3 "+minHeight+ " to " + maxHeight);
        player.sendMessage("§eMax Subzone Depth:§3 "+subzonedepth);
        player.sendMessage("§eCan Set Enter/Leave Messages:§3 "+messageperms);
        player.sendMessage("§eNumber of Residences you own:§3 " + Residence.getResidenceManger().getOwnedZoneCount(player.getName()));
        if(Residence.getEconomyManager()!=null)
            player.sendMessage("§eResidence Cost Per Block:§3 " + costperarea);
        player.sendMessage("§eFlag Permissions:§3 " + flagPerms.listFlags());
        if(Residence.getConfig().useLeases())
        {
            player.sendMessage("§eMax Lease Days:§3 " + maxLeaseTime);
            player.sendMessage("§eLease Time Given on Renew:§3 " + leaseGiveTime);
            player.sendMessage("§eRenew Cost Per Block:§3 " + renewcostperarea);
        }
        player.sendMessage("§7---------------------------");
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
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
        protected int areamax;
        protected double costperarea;
        protected boolean tpaccess;
        protected int subzonedepth;
        protected Map<String,Boolean> flagPerms;
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

        public PermissionGroup(String name)
        {
            flagPerms = Collections.synchronizedMap(new HashMap<String,Boolean>());
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

    private void parseGroup(ConfigurationNode limits) {
        if(limits == null)
            return;
        cancreate = limits.getBoolean("CanCreate", true);
        xmax = limits.getInt("MaxX", 0);
        ymax = limits.getInt("MaxY", 0);
        zmax = limits.getInt("MaxZ", 0);
        costperarea = limits.getDouble("MoneyCost", 0);
        areamax = limits.getInt("MaxRes", 0);
        tpaccess = limits.getBoolean("AllowTeleport", false);
        subzonedepth = limits.getInt("SubzoneDepth", 0);
        messageperms = limits.getBoolean("CanSetMessages", false);
        defaultEnterMessage = limits.getString("DefaultEnterMessage", null);
        defaultLeaveMessage = limits.getString("DefaultLeaveMessage", null);
        maxPhysical = limits.getInt("MaxResidenceAreas",2);
        ConfigurationNode lease = limits.getNode("Lease");
        if (lease != null) {
            maxLeaseTime = lease.getInt("MaxDays", 16);
            leaseGiveTime = lease.getInt("RenewTime", 14);
            renewcostperarea = lease.getDouble("RenewCost", 0.02D);
        }
        ConfigurationNode transaction = limits.getNode("Economy");
        if (transaction != null) {
            canBuy = transaction.getBoolean("CanBuy", false);
            canSell = transaction.getBoolean("CanSell", false);
            buyIgnoreLimits = transaction.getBoolean("BuyIgnoreLimits", false);
        }
        List<String> flags = limits.getKeys("FlagPermissions");
        if (limits != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("FlagPermissions." + flagname, false);
                flagPerms.put(flagname, access);
            }
        }
        flags = limits.getKeys("CreatorDefaultFlags");
        if (limits != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("CreatorDefaultFlags." + flagname, false);
                creatorDefaultFlags.put(flagname, access);
            }
        }
        flags = limits.getKeys("ResidenceDefaultFlags");
        if (limits != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("ResidenceDefaultFlags." + flagname, false);
                residenceDefaultFlags.put(flagname, access);
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
    public int getMaxZones() {
        return areamax;
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
    public boolean buyLandIgnoreLimits()
    {
        return buyIgnoreLimits;
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
    public Set<Entry<String,Boolean>> getFlagPermissions()
    {
        return flagPerms.entrySet();
    }

    public boolean canCreateResidences()
    {
        return cancreate;
    }
    public boolean hasFlagAccess(String flag)
    {
        if(flagPerms.containsKey(flag))
            return flagPerms.get(flag);
        return false;
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
        player.sendMessage("§eResidenceAdmin:§3 " + Residence.getPermissionManager().isResidenceAdmin(player));
        player.sendMessage("§eCanCreateResidences:§3 "+cancreate);
        player.sendMessage("§eMaxX:§3 "+xmax);
        player.sendMessage("§eMaxY:§3 "+ymax);
        player.sendMessage("§eMaxZ:§3 "+zmax);
        player.sendMessage("§eMax residences per player:§3 "+areamax);
        player.sendMessage("§eMaxSubZoneDepth:§3 "+subzonedepth);
        player.sendMessage("§eEnterLeaveMessagePermission:§3 "+messageperms);
        player.sendMessage("§eResidences you own:§3 " + Residence.getResidenceManger().getOwnedZoneCount(player.getName()));
        if(Residence.getIConManager()!=null)
            player.sendMessage("§eMoney cost per block:§3 " + costperarea);
        StringBuilder flags = new StringBuilder();
        synchronized (flagPerms) {
            Iterator<Entry<String, Boolean>> it = flagPerms.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Boolean> next = it.next();
                if (next.getValue()) {
                    flags.append(" +");
                    flags.append(next.getKey());
                }
            }
            player.sendMessage("§eResidenceFlagAccess:§3 " + flags.toString());
        }
        if(Residence.getConfig().useLeases())
        {
            player.sendMessage("§eMaxLeaseDays: " + maxLeaseTime);
        }
        player.sendMessage("§7---------------------------");
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidenceManager {
    protected Map<String,ClaimedResidence> residences;

    public ResidenceManager()
    {
        residences = Collections.synchronizedMap(new HashMap<String,ClaimedResidence>());
    }

    public ClaimedResidence getByLoc(Location loc) {
        ClaimedResidence res = null;
        boolean found = false;
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized (residences) {
            for (Entry<String, ClaimedResidence> key : set) {
                res = key.getValue();
                if (res.containsLoc(loc)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            return null;
        }
        ClaimedResidence subres = res.getSubzoneByLoc(loc);
        if (subres == null) {
            return res;
        }
        return subres;
    }

    public ClaimedResidence getByName(String name) {
        String[] split = name.split("\\.");
        if (split.length == 1) {
            return residences.get(name);
        }
        ClaimedResidence res = residences.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (res != null) {
                res = res.getSubzone(split[i]);
            } else {
                return null;
            }
        }
        return res;
    }


    public String getNameByLoc(Location loc) {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        ClaimedResidence res = null;
        String name = null;
        synchronized (residences) {
            for (Entry<String, ClaimedResidence> key : set) {
                res = key.getValue();
                if (res.containsLoc(loc)) {
                    name = key.getKey();
                    break;
                }
            }
        }
        if(name==null)
            return null;
        String szname = res.getSubzoneNameByLoc(loc);
        if (szname != null) {
            return name + "." + szname;
        }
        return name;
    }

    public void addResidence(Player player, String name, Location loc1, Location loc2)
    {
        name = name.replace(".", "_");
        name = name.replace(":", "_");
        if(player == null)
            return;
        if(loc1==null || loc2==null || !loc1.getWorld().getName().equals(loc2.getWorld().getName()))
        {
            player.sendMessage("§cInvalid selection points.");
            return;
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(player);
        if (!group.canCreateResidences() && !Residence.getPermissionManager().isResidenceAdmin(player)) {
            player.sendMessage("§cYou dont have permission to create residences.");
            return;
        }
        if (residences.containsKey(name)) {
            player.sendMessage("§cA residence by this name already exists.");
            return;
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        Collection<ClaimedResidence> set = residences.values();
        synchronized (set) {
            for (ClaimedResidence res : set) {
                if (res.checkCollision(newArea)) {
                    player.sendMessage("§cArea collides with another residence.");
                    return;
                }
            }
        }
        ClaimedResidence newRes = new ClaimedResidence(player.getName(), loc1.getWorld().getName());
        newRes.getPermissions().applyDefaultFlags();;
        newRes.setEnterMessage(group.getDefaultEnterMessage());
        newRes.setLeaveMessage(group.getDefaultLeaveMessage());
        newRes.addArea(player, newArea, "main");
        if(newRes.getAreaCount()!=0)
        {
            residences.put(name, newRes);
            player.sendMessage("§aYou have created residence: §e" + name + "§a!");
            if(Residence.getConfig().useLeases())
                Residence.getLeaseManager().setExpireTime(player, name, group.getLeaseGiveTime());
        }
        else
            player.sendMessage("§cError creating residence...");
    }

    public void listResidences(Player player)
    {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("§eResidences:§3 ");
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized(residences)
        {
            Iterator<Entry<String, ClaimedResidence>> it = set.iterator();
            while(it.hasNext())
            {
                Entry<String, ClaimedResidence> next = it.next();
                if(next.getValue().getPermissions().getOwner().equalsIgnoreCase(player.getName()))
                {
                    sbuilder.append(next.getKey());
                    if(it.hasNext())
                        sbuilder.append(", ");
                }
            }
        }
        player.sendMessage(sbuilder.toString());
    }

    public void addPhysicalArea(Player player, String residenceName,String areaID, Location loc1, Location loc2)
    {
        residenceName = residenceName.toLowerCase();
        ClaimedResidence res = getByName(residenceName);
        if(!res.getPermissions().hasResidencePermission(player, true))
        {
            player.sendMessage("§cYou dont have permission to do this.");
            return;
        }
        CuboidArea newarea = new CuboidArea(loc1,loc2);
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized(residences)
        {
            for(Entry<String, ClaimedResidence> entry : set)
            {
                String name = entry.getKey();
                if(!name.equals(residenceName))
                {
                    ClaimedResidence check = entry.getValue();
                    if(check.checkCollision(newarea))
                    {
                        player.sendMessage("§cArea collides with another residence.");
                        return;
                    }
                }
            }
        }
        res.addArea(player, newarea, areaID);
    }

    public void removeResidence(Player player, String name) {
        ClaimedResidence res = this.getByName(name);
        if (res != null) {
            if (player!=null && !Residence.getPermissionManager().isResidenceAdmin(player)) {
                if (!res.getPermissions().hasResidencePermission(player, true)) {
                    player.sendMessage("§cYou dont have permission to modify this residence.");
                    return;
                }
            }
            ClaimedResidence parent = res.getParent();
            if (parent != null) {
                String[] split = name.split("\\.");
                parent.removeSubzone(split[split.length - 1]);
            } else {
                residences.remove(name);
            }
            if(player!=null)
                player.sendMessage("§aResidence§e " + name + " §aremoved...");
        } else {
            if(player!=null)
                player.sendMessage("§cInvalid Residence.");
        }
    }

    public int getOwnedZoneCount(String player)
    {
        player = player;
        Collection<ClaimedResidence> set = residences.values();
        int count=0;
        synchronized(residences)
        {
            for(ClaimedResidence res : set)
            {
                if(res.getPermissions().getOwner().equals(player))
                {
                    count++;
                }
            }
        }
        return count;
    }

    public String[] getResidenceList()
    {
        return (String[]) residences.keySet().toArray();
    }

    public void listAllResidences(Player player)
    {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("§a");
        Set<String> set = residences.keySet();
        synchronized(residences)
        {
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String next = it.next();
                sbuilder.append(next);
                if(it.hasNext())
                    sbuilder.append(", ");
            }
        }
        player.sendMessage("§eResidences (Count="+residences.size()+"): " + sbuilder.toString());

    }

    public void printAreaInfo(String areaname, Player player) {
        ClaimedResidence res = this.getByName(areaname);
        if(res==null)
        {
            player.sendMessage("§cInvalid Residence.");
            return;
        }
        ResidencePermissions perms = res.getPermissions();
        player.sendMessage("§7---------------------------");
        player.sendMessage("§eResidence:§2 " + areaname);
        player.sendMessage("§eOwner:§c " + perms.getOwner());
        player.sendMessage("§eFlags:§9 " + perms.listFlags());
        player.sendMessage("§eYour Flags: §a" + perms.listPlayerFlags(player.getName()));
        player.sendMessage("§eGroup Flags:§c " + perms.listGroupFlags());
        player.sendMessage("§eOthers Flags:§c " + perms.listOtherPlayersFlags(player.getName()));
        player.sendMessage("§ePhysical Areas: " + res.getFormattedAreaList());
        String aid = res.getAreaIDbyLoc(player.getLocation());
        if(aid !=null)
            player.sendMessage("§eCurrent Area ID: §6" + aid);
        player.sendMessage("§eTotal Size:§d " + res.getTotalSize());
        player.sendMessage("§eSubZones:§6 " + res.CSVSubzoneList());
        if (Residence.getConfig().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
            player.sendMessage("§eLeaseExpiration:§a " + Residence.getLeaseManager().getExpireTime(areaname));
        }
        player.sendMessage("§7---------------------------");
    }

    public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea) {
        ClaimedResidence reciever = this.getByName(targetArea);
        ClaimedResidence source = this.getByName(sourceArea);
        if (source == null || reciever == null) {
            reqPlayer.sendMessage("§cEither the target or source area was invalid.");
            return;
        }
        if (!Residence.getPermissionManager().isResidenceAdmin(reqPlayer)) {

            if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
                reqPlayer.sendMessage("§cYou must be the owner of both residences to mirror permissions.");
                return;
            }
        }
        reciever.getPermissions().applyTemplate(reqPlayer, source.getPermissions());
    }

    public Map<String,Object> save()
    {
        Map<String,Object> resmap = new LinkedHashMap<String,Object>();
        for(Entry<String, ClaimedResidence> res : residences.entrySet())
        {
            try
            {
                resmap.put(res.getKey(), res.getValue().save());
            }
            catch (Exception ex)
            {
                System.out.println("[Residence] Failed to save residence (" + res.getKey() + ")!");
                Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resmap;
    }

    public static ResidenceManager load(Map<String,Object> root)
    {
        ResidenceManager resm = new ResidenceManager();
        if(root != null)
        {
            for(Entry<String, Object> res : root.entrySet())
            {
                try
                {
                    resm.residences.put(res.getKey(), ClaimedResidence.load((Map<String, Object>) res.getValue(), null));
                }
                catch (Exception ex)
                {
                    System.out.print("[Residence] Failed to load residence (" + res.getKey() + ")! Reason:" + ex.getMessage());
                    //Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return resm;
    }

    public void renameResidence(Player player, String oldName, String newName)
    {
        newName = newName.replace(".", "_");
        newName = newName.replace(":", "_");
        ClaimedResidence res = this.getByName(oldName);
        if(res==null)
        {
            player.sendMessage("Invalid Residence...");
            return;
        }
        if(res.getPermissions().hasResidencePermission(player, true))
        {
            if(res.getParent()==null)
            {
                if(residences.containsKey(newName))
                {
                    player.sendMessage("Another residence already has that name...");
                    return;
                }
                residences.put(newName, res);
                residences.remove(oldName);
                player.sendMessage("Renamed " + oldName + " to " + newName + "...");
            }
            else
            {
                String[] oldname = oldName.split("\\.");
                ClaimedResidence parent = res.getParent();
                parent.renameSubzone(player, oldname[oldname.length-1], newName);
            }
        }
        else
        {
            player.sendMessage("You dont have permission...");
        }
    }
}

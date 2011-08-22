/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import java.util.ArrayList;
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
        if(loc==null)
            return null;
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
        if(name==null)
            return null;
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
        if(loc==null)
            return null;
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

    public String getNameByRes(ClaimedResidence res)
    {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized(residences)
        {
            for(Entry<String, ClaimedResidence> check : set)
            {
                if(check.getValue()==res)
                    return check.getKey();
                String n = check.getValue().getSubzoneNameByRes(res);
                if(n!=null)
                    return check.getKey() + "." + n;
            }
        }
        return null;
    }

    public void addResidence(String name, String owner, Location loc1, Location loc2)
    {
        name = ResidenceManager.nameFilter(name);
        if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
            return;
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(owner, loc1.getWorld().getName());
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        ClaimedResidence newRes = new ClaimedResidence(owner, loc1.getWorld().getName());
        newRes.getPermissions().applyDefaultFlags();
        newRes.setEnterMessage(group.getDefaultEnterMessage());
        newRes.setLeaveMessage(group.getDefaultLeaveMessage());
        ResidenceCreationEvent resevent = new ResidenceCreationEvent(null, name, newRes, newArea);
        Residence.getServ().getPluginManager().callEvent(resevent);
        if (resevent.isCancelled()) {
            return;
        }
        newArea = resevent.getPhysicalArea();
        name = resevent.getResidenceName();
        if (residences.containsKey(name)) {
            return;
        }
        newRes.addArea(newArea, "main");
        if (newRes.getAreaCount() != 0) {
            residences.put(name, newRes);
        }
    }

    public void addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin)
    {
        name = ResidenceManager.nameFilter(name);
        if(player == null)
            return;
        if(loc1==null || loc2==null || !loc1.getWorld().getName().equals(loc2.getWorld().getName()))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("SelectPoints"));
            return;
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(player);
        boolean createpermission = group.canCreateResidences() || Residence.getPermissionManager().hasAuthority(player, "residence.create", false);
        if (!createpermission && !resadmin) {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if (getOwnedZoneCount(player.getName()) >= group.getMaxZones() && !resadmin)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceTooMany"));
            return;
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        ClaimedResidence newRes = new ClaimedResidence(player.getName(), loc1.getWorld().getName());
        newRes.getPermissions().applyDefaultFlags();
        newRes.setEnterMessage(group.getDefaultEnterMessage());
        newRes.setLeaveMessage(group.getDefaultLeaveMessage());

        ResidenceCreationEvent resevent = new ResidenceCreationEvent(player,name, newRes, newArea);
        Residence.getServ().getPluginManager().callEvent(resevent);
        if(resevent.isCancelled())
            return;
        newArea = resevent.getPhysicalArea();
        name = resevent.getResidenceName();
        if (residences.containsKey(name)) {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceAlreadyExists","§e"+name+"§c"));
            return;
        }
        newRes.addArea(player, newArea, "main", resadmin);
        if(newRes.getAreaCount()!=0)
        {
            residences.put(name, newRes);
            Residence.getLeaseManager().removeExpireTime(name);
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceCreate","§e" + name + "§a"));
            if(Residence.getConfig().useLeases())
                Residence.getLeaseManager().setExpireTime(player, name, group.getLeaseGiveTime());
        }
    }

    public void listResidences(Player player)
    {
        this.listResidences(player, player.getName(), 1);
    }
    
    public void listResidences(Player player, int page)
    {
        this.listResidences(player, player.getName(), page);
    }
    
    public void listResidences(Player player, String targetplayer)
    {
        this.listResidences(player, targetplayer, 1);
    }

    public void listResidences(Player player, String targetplayer, int page)
    {
        ArrayList<String> temp = new ArrayList<String>();
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized(residences)
        {
            Iterator<Entry<String, ClaimedResidence>> it = set.iterator();
            while(it.hasNext())
            {
                Entry<String, ClaimedResidence> next = it.next();
                if(next.getValue().getPermissions().getOwner().equalsIgnoreCase(targetplayer))
                {
                    temp.add("§a"+next.getKey()+"§e - "+Residence.getLanguage().getPhrase("World") + ": " + next.getValue().getWorld());
                }
            }
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences") + " - " + targetplayer, temp, page);
    }

    public String checkAreaCollision(CuboidArea newarea, ClaimedResidence parentResidence) {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        for (Entry<String, ClaimedResidence> entry : set) {
            ClaimedResidence check = entry.getValue();
            if (check!=parentResidence && check.checkCollision(newarea)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void removeResidence(String name)
    {
        this.removeResidence(null, name, true);
    }

    public void removeResidence(Player player, String name, boolean resadmin) {
        ClaimedResidence res = this.getByName(name);
        if (res != null) {
            if (player!=null && !resadmin) {
                if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                    return;
                }
            }
            ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, player==null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
            Residence.getServ().getPluginManager().callEvent(resevent);
            if(resevent.isCancelled())
                return;
            ClaimedResidence parent = res.getParent();
            if (parent != null) {
                String[] split = name.split("\\.");
                parent.removeSubzone(split[split.length - 1]);
            } else {
                residences.remove(name);
            }
            //Residence.getLeaseManager().removeExpireTime(name); - causing concurrent modification exception in lease manager... worked around for now
            Residence.getRentManager().removeRentable(name);
            if(player!=null)
                player.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceRemove","§e" + name + "§a"));
        } else {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
        }
    }

    public void removeAllByOwner(String owner)
    {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized(residences)
        {
            Iterator<ClaimedResidence> it = residences.values().iterator();
            while(it.hasNext())
            {
                ClaimedResidence res = it.next();
                if(res.getOwner().equalsIgnoreCase(owner))
                {
                    it.remove();
                }
            }
        }
    }

    public int getOwnedZoneCount(String player)
    {
        Collection<ClaimedResidence> set = residences.values();
        int count=0;
        synchronized(residences)
        {
            for(ClaimedResidence res : set)
            {
                if(res.getPermissions().getOwner().equalsIgnoreCase(player))
                {
                    count++;
                }
            }
        }
        return count;
    }

    public String[] getResidenceList()
    {
        String[] reslist = new String[residences.size()];
        int i = 0;
        synchronized(residences)
        {
            for(String res : residences.keySet())
            {
                reslist[i] = res;
                i++;
            }
        }
        return reslist;
    }

    public void listAllResidences(Player player, int page)
    {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        ArrayList<String> temp = new ArrayList<String>();
        synchronized(residences)
        {
            Iterator<Entry<String, ClaimedResidence>> it = set.iterator();
            while(it.hasNext())
            {
                Entry<String, ClaimedResidence> next = it.next();
                temp.add("§a" +next.getKey() + "§e - "+Residence.getLanguage().getPhrase("Owner") + ": " + next.getValue().getOwner() + " - " + Residence.getLanguage().getPhrase("World")+": " + next.getValue().getWorld());
            }
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences"), temp, page);
    }

    public void printAreaInfo(String areaname, Player player) {
        ClaimedResidence res = this.getByName(areaname);
        if(res==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        ResidencePermissions perms = res.getPermissions();
        if(Residence.getConfig().enableEconomy())
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("Residence")+":§2 " + areaname + " §eBank: §6" + res.getBank().getStoredMoney());
        else
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("Residence")+":§2 " + areaname);
        if(Residence.getConfig().enabledRentSystem() && Residence.getRentManager().isRented(areaname))
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("Owner")+":§c " + perms.getOwner() + "§e Rented by: §c" + Residence.getRentManager().getRentingPlayer(areaname));
        else
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("Owner")+":§c " + perms.getOwner() + "§e - " + Residence.getLanguage().getPhrase("World")+": §c"+ perms.getWorld());
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Flags")+":§9 " + perms.listFlags());
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Your.Flags")+": §a" + perms.listPlayerFlags(player.getName()));
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Group.Flags")+":§c " + perms.listGroupFlags());
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Others.Flags")+":§c " + perms.listOtherPlayersFlags(player.getName()));
        String aid = res.getAreaIDbyLoc(player.getLocation());
        if(aid !=null)
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("CurrentArea")+": §6" + aid);
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Total.Size")+":§d " + res.getTotalSize());
        if (Residence.getConfig().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("LeaseExpire")+":§a " + Residence.getLeaseManager().getExpireTime(areaname));
        }
    }

    public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea, boolean resadmin) {
        ClaimedResidence reciever = this.getByName(targetArea);
        ClaimedResidence source = this.getByName(sourceArea);
        if (source == null || reciever == null) {
            reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidArea"));
            return;
        }
        if (!resadmin) {
            if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
                reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
        }
        reciever.getPermissions().applyTemplate(reqPlayer, source.getPermissions(), resadmin);
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

    public static ResidenceManager load(Map<String,Object> root) throws Exception
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
                    System.out.print("[Residence] Failed to load residence (" + res.getKey() + ")! Reason:" + ex.getMessage() + " Error Log:");
                    Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
                    if(Residence.getConfig().stopOnSaveError())
                    {
                        throw(ex);
                    }
                }
            }
        }
        return resm;
    }

    public boolean renameResidence(String oldName, String newName)
    {
        return this.renameResidence(null, oldName, newName, true);
    }

    public boolean renameResidence(Player player, String oldName, String newName, boolean resadmin)
    {
        newName = this.nameFilter(newName);
        ClaimedResidence res = this.getByName(oldName);
        if(res==null)
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
            return false;
        }
        if(res.getPermissions().hasResidencePermission(player, true) || resadmin)
        {
            if(res.getParent()==null)
            {
                if(residences.containsKey(newName))
                {
                    if(player!=null)
                        player.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceAlreadyExists","§e"+newName+"§c"));
                    return false;
                }
                residences.put(newName, res);
                residences.remove(oldName);
                if(Residence.getConfig().useLeases())
                    Residence.getLeaseManager().updateLeaseName(oldName, newName);
                if(Residence.getConfig().enabledRentSystem())
                {
                    Residence.getRentManager().updateRentableName(oldName, newName);
                }
                if(player!=null)
                    player.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceRename","§e" + oldName + "§a.§e" + newName + "§a"));
                return true;
            }
            else
            {
                String[] oldname = oldName.split("\\.");
                ClaimedResidence parent = res.getParent();
                return parent.renameSubzone(player, oldname[oldname.length-1], newName, resadmin);
            }
        }
        else
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin)
    {
        ClaimedResidence res = getByName(residence);
        if(res==null)
        {
            reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        if(!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin)
        {
            reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        Player giveplayer = Residence.getServ().getPlayer(targPlayer);
        if (giveplayer == null || !giveplayer.isOnline()) {
            reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("NotOnline"));
            return;
        }
        CuboidArea[] areas = res.getAreaArray();
        PermissionGroup g = Residence.getPermissionManager().getGroup(giveplayer);
        if (areas.length > g.getMaxPhysicalPerResidence() && !resadmin) {
            reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
            return;
        }
        if (getOwnedZoneCount(giveplayer.getName()) >= g.getMaxZones() && !resadmin) {
            reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
            return;
        }
        if(!resadmin)
        {
            for (CuboidArea area : areas) {
                if (!g.inLimits(area)) {
                    reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
                    return;
                }
            }
        }
        res.getPermissions().setOwner(giveplayer.getName(), true);
        reqPlayer.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceGive","§e" + residence + "§a.§e" + giveplayer.getName() + "§a"));
        giveplayer.sendMessage(Residence.getLanguage().getPhrase("ResidenceRecieve","§a" + reqPlayer.getName() + "§e.§a" + residence + "§e"));
    }

    public int getResidenceCount()
    {
        return residences.size();
    }

    public static String nameFilter(String name)
    {
        name = name.replace(".", "_");
        name = name.replace(":", "_");
        String regex = Residence.getConfig().getResidenceNameRegex();
        name = name.replaceAll(regex, "");
        if(name.equals(""))
            return "_";
        else
            return name;
    }
}

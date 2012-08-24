/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;
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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;

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

    public boolean addResidence(String name, String owner, Location loc1, Location loc2)
    {
        if(!Residence.validName(name))
            return false;
        if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
            return false;
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
            return false;
        }
        newArea = resevent.getPhysicalArea();
        name = resevent.getResidenceName();
        if (residences.containsKey(name)) {
            return false;
        }
        newRes.addArea(newArea, "main");
        if (newRes.getAreaCount() != 0) {
            residences.put(name, newRes);
        }
        return true;
    }

    public void addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin)
    {
        if(!Residence.validName(name))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return;
        }
        if(player == null)
            return;
        if(loc1==null || loc2==null || !loc1.getWorld().getName().equals(loc2.getWorld().getName()))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectPoints"));
            return;
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(player);
        boolean createpermission = group.canCreateResidences() || Residence.getPermissionManager().hasAuthority(player, "residence.create");
        if (!createpermission && !resadmin) {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if (getOwnedZoneCount(player.getName()) >= group.getMaxZones() && !resadmin)
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceTooMany"));
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
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceAlreadyExists",ChatColor.YELLOW+name+ChatColor.RED));
            return;
        }
        newRes.addArea(player, newArea, "main", resadmin);
        if(newRes.getAreaCount()!=0)
        {
            residences.put(name, newRes);
            Residence.getLeaseManager().removeExpireTime(name);
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceCreate",ChatColor.YELLOW + name + ChatColor.GREEN));
            if(Residence.getConfigManager().useLeases())
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
        this.listResidences(player, targetplayer, page, false);
    }

    public void listResidences(Player player, int page, boolean showhidden)
    {
        this.listResidences(player, player.getName(), page, showhidden);
    }

    public void listResidences(Player player, String targetplayer, int page, boolean showhidden)
    {
        ArrayList<String> temp = new ArrayList<String>();
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        synchronized(residences)
        {
            Iterator<Entry<String, ClaimedResidence>> it = set.iterator();
            while(it.hasNext())
            {
                Entry<String, ClaimedResidence> next = it.next();
                ClaimedResidence res = next.getValue();
                boolean hidden = res.getPermissions().has("hidden", false);
                if( (showhidden && hidden) || (!showhidden && !hidden) || (res.getPermissions().getOwner().equals(player.getName()) && targetplayer.equals(player.getName()) && (!showhidden && hidden)))
                {
                    if(res.getPermissions().getOwner().equalsIgnoreCase(targetplayer))
                    {
                        temp.add(ChatColor.GREEN+next.getKey()+ChatColor.YELLOW+" - "+Residence.getLanguage().getPhrase("World") + ": " + res.getWorld());
                    }
                }
            }
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences") + " - " + targetplayer, temp, page);
    }
    
    public void listResidences(Player player, String targetplayer, int page, boolean showhidden, boolean showsubzones)
    {
        if(showhidden && !Residence.isResAdminOn(player) && !player.getName().equals(targetplayer))
        {
            showhidden = false;
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences") + " - " + targetplayer, this.getAllOwnedZones(targetplayer, showhidden, showsubzones), page);
    }
    
    public ArrayList<String> getAllOwnedZones(String player, boolean showhidden, boolean showsubzones)
    {
        ArrayList<String> list = new ArrayList<String>();
        for(Entry<String, ClaimedResidence> res : residences.entrySet())
        {
            this.getAllOwnedZones(player,showhidden,showsubzones,"",res.getKey(),res.getValue(),list);
        }
        return list;
    }
    
    private void getAllOwnedZones(String targetplayer, boolean showhidden, boolean showsubzones, String parentzone, String resname, ClaimedResidence res, ArrayList<String> list)
    {
        boolean hidden = res.getPermissions().has("hidden", false);
        if((showhidden && hidden) || (!showhidden && !hidden))
        {
            if(res.getPermissions().getOwner().equalsIgnoreCase(targetplayer))
            {
                list.add(ChatColor.GREEN+parentzone+resname+ChatColor.YELLOW+" - "+Residence.getLanguage().getPhrase("World") + ": " + res.getWorld());
            }
            if(showsubzones)
            {
                for(Entry<String, ClaimedResidence> sz : res.subzones.entrySet())
                {
                    this.getAllOwnedZones(targetplayer, showhidden, showsubzones, parentzone+resname+".", sz.getKey(), sz.getValue(), list);
                }
            }
        }
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
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
                    return;
                }
            }
            ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, player==null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
            Residence.getServ().getPluginManager().callEvent(resevent);
            if(resevent.isCancelled())
                return;
            ClaimedResidence parent = res.getParent();
            if (parent == null) {
                residences.remove(name);
                if(player!=null){
                    player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceRemove",ChatColor.YELLOW + name + ChatColor.GREEN));
                }
            } else {
                String[] split = name.split("\\.");
                if(player!=null){
                    parent.removeSubzone(player, split[split.length - 1], true);
                } else{
                    parent.removeSubzone(split[split.length - 1]);
                }     
            }
            //Residence.getLeaseManager().removeExpireTime(name); - causing concurrent modification exception in lease manager... worked around for now
            Residence.getRentManager().removeRentable(name);

        } else {
            if(player!=null)
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
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
        this.listAllResidences(player, page, false);
    }

    public void listAllResidences(Player player, int page, boolean showhidden)
    {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        ArrayList<String> temp = new ArrayList<String>();
        synchronized(residences)
        {
            Iterator<Entry<String, ClaimedResidence>> it = set.iterator();
            while(it.hasNext())
            {
                Entry<String, ClaimedResidence> next = it.next();
                ClaimedResidence res = next.getValue();
                boolean hidden = res.getPermissions().has("hidden", false);
                if( (showhidden && hidden) || (!showhidden && !hidden) || player.getName().equals(res.getOwner()))
                    temp.add(ChatColor.GREEN +next.getKey() + ChatColor.YELLOW+" - "+Residence.getLanguage().getPhrase("Owner") + ": " + res.getOwner() + " - " + Residence.getLanguage().getPhrase("World")+": " + res.getWorld());
            }
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences"), temp, page);
    }

    public void printAreaInfo(String areaname, Player player) {
        ClaimedResidence res = this.getByName(areaname);
        if(res==null)
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        ResidencePermissions perms = res.getPermissions();
        if(Residence.getConfigManager().enableEconomy())
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Residence")+":"+ChatColor.DARK_GREEN+" " + areaname + " "+ChatColor.YELLOW+"Bank: "+ChatColor.GOLD + res.getBank().getStoredMoney());
        else
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Residence")+":"+ChatColor.DARK_GREEN+" " + areaname);
        if(Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isRented(areaname))
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Owner")+":"+ChatColor.RED+" " + perms.getOwner() + ChatColor.YELLOW+" Rented by: "+ChatColor.RED + Residence.getRentManager().getRentingPlayer(areaname));
        else
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Owner")+":"+ChatColor.RED+" " + perms.getOwner() + ChatColor.YELLOW+" - " + Residence.getLanguage().getPhrase("World")+": "+ChatColor.RED+ perms.getWorld());
        player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Flags")+":"+ChatColor.BLUE+" " + perms.listFlags());
        player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Your.Flags")+": "+ChatColor.GREEN + perms.listPlayerFlags(player.getName()));
        player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Group.Flags")+":"+ChatColor.RED+" " + perms.listGroupFlags());
        player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Others.Flags")+":"+ChatColor.RED+" " + perms.listOtherPlayersFlags(player.getName()));
        String aid = res.getAreaIDbyLoc(player.getLocation());
        if(aid !=null)
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("CurrentArea")+": "+ChatColor.GOLD + aid);
        player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Total.Size")+":"+ChatColor.LIGHT_PURPLE+" " + res.getTotalSize());
        if(aid !=null){
           player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("CoordsT")+": "+ChatColor.LIGHT_PURPLE+Residence.getLanguage().getPhrase("CoordsTop",res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockX() + "." + res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockY() + "." + res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockZ()));
           player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("CoordsB")+": "+ChatColor.LIGHT_PURPLE+Residence.getLanguage().getPhrase("CoordsBottom",res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockX() + "." + res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockY() + "." + res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockZ()));
        }
        if (Residence.getConfigManager().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("LeaseExpire")+":"+ChatColor.GREEN+" " + Residence.getLeaseManager().getExpireTime(areaname));
        }
    }

    public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea, boolean resadmin) {
        ClaimedResidence reciever = this.getByName(targetArea);
        ClaimedResidence source = this.getByName(sourceArea);
        if (source == null || reciever == null) {
            reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        if (!resadmin) {
            if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
                reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
        }
        reciever.getPermissions().applyTemplate(reqPlayer, source.getPermissions(), resadmin);
    }

    public Map<String,Object> save()
    {
        Map<String,Object> worldmap = new LinkedHashMap<String,Object>();
        for(World world : Residence.getServ().getWorlds())
        {
            Map<String,Object> resmap = new LinkedHashMap<String,Object>();
            for(Entry<String, ClaimedResidence> res : residences.entrySet())
            {
                if(res.getValue().getWorld().equals(world.getName()))
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
            }
            worldmap.put(world.getName(), resmap);
        }
        return worldmap;
    }

    public static ResidenceManager load(Map<String,Object> root) throws Exception
    {
        ResidenceManager resm = new ResidenceManager();
        if(root==null)
        {
            return resm;
        }
        for(World world : Residence.getServ().getWorlds())
        {
            Map<String,Object> reslist = (Map<String, Object>) root.get(world.getName());
            if(reslist!=null)
            {
                try
                {
                    loadMap(reslist,resm);
                }
                catch (Exception ex)
                {
                    System.out.println("Error in loading save file for world: " + world.getName());
                    if(Residence.getConfigManager().stopOnSaveError())
                        throw(ex);
                }
            }
        }
        return resm;
    }

    public static ResidenceManager loadMap(Map<String,Object> root, ResidenceManager resm) throws Exception
    {
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
                    if(Residence.getConfigManager().stopOnSaveError())
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
        if(!Residence.validName(newName))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        ClaimedResidence res = this.getByName(oldName);
        if(res==null)
        {
            if(player!=null)
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
            return false;
        }
        if(res.getPermissions().hasResidencePermission(player, true) || resadmin)
        {
            if(res.getParent()==null)
            {
                if(residences.containsKey(newName))
                {
                    if(player!=null)
                        player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceAlreadyExists",ChatColor.YELLOW+newName+ChatColor.RED));
                    return false;
                }
    			ResidenceRenameEvent resevent = new ResidenceRenameEvent(res, newName, oldName);
				Residence.getServ().getPluginManager().callEvent(resevent);
                residences.put(newName, res);
                residences.remove(oldName);
                if(Residence.getConfigManager().useLeases())
                    Residence.getLeaseManager().updateLeaseName(oldName, newName);
                if(Residence.getConfigManager().enabledRentSystem())
                {
                    Residence.getRentManager().updateRentableName(oldName, newName);
                }
                if(player!=null)
                    player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceRename",ChatColor.YELLOW + oldName + ChatColor.GREEN+"."+ChatColor.YELLOW + newName + ChatColor.GREEN));
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
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin)
    {
        ClaimedResidence res = getByName(residence);
        if(res==null)
        {
            reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        if(!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin)
        {
            reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        Player giveplayer = Residence.getServ().getPlayer(targPlayer);
        if (giveplayer == null || !giveplayer.isOnline()) {
            reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NotOnline"));
            return;
        }
        CuboidArea[] areas = res.getAreaArray();
        PermissionGroup g = Residence.getPermissionManager().getGroup(giveplayer);
        if (areas.length > g.getMaxPhysicalPerResidence() && !resadmin) {
            reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
            return;
        }
        if (getOwnedZoneCount(giveplayer.getName()) >= g.getMaxZones() && !resadmin) {
            reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
            return;
        }
        if(!resadmin)
        {
            for (CuboidArea area : areas) {
                if (!g.inLimits(area)) {
                    reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
                    return;
                }
            }
        }
        res.getPermissions().setOwner(giveplayer.getName(), true);
        reqPlayer.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceGive",ChatColor.YELLOW + residence + ChatColor.GREEN+"."+ChatColor.YELLOW + giveplayer.getName() + ChatColor.GREEN));
        giveplayer.sendMessage(Residence.getLanguage().getPhrase("ResidenceRecieve",ChatColor.GREEN + residence + ChatColor.YELLOW+"."+ChatColor.GREEN + reqPlayer.getName() + ChatColor.YELLOW));
    }

    public void removeAllFromWorld(CommandSender sender, String world)
    {
        int count=0;
        Iterator<ClaimedResidence> it = residences.values().iterator();
        while(it.hasNext())
        {
            ClaimedResidence next = it.next();
            if(next.getWorld().equals(world))
            {
                it.remove();
                count++;
            }
        }
        if(count==0)
            sender.sendMessage(ChatColor.RED+"No residences found in world: "+ChatColor.YELLOW + world);
        else
            sender.sendMessage(ChatColor.RED+"Removed "+ChatColor.YELLOW+count+ChatColor.RED+" residences in world: "+ChatColor.YELLOW + world);
    }

    public int getResidenceCount()
    {
        return residences.size();
    }
}

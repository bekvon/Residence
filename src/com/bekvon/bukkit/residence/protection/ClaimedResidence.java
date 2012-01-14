/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.itemlist.ItemList.ListType;
import com.bekvon.bukkit.residence.itemlist.ResidenceItemList;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ClaimedResidence {

    protected ClaimedResidence parent;
    protected Map<String, CuboidArea> areas;
    protected Map<String, ClaimedResidence> subzones;
    protected ResidencePermissions perms;
    protected ResidenceBank bank;
    protected Location tpLoc;
    protected String enterMessage;
    protected String leaveMessage;
    protected ResidenceItemList ignorelist;
    protected ResidenceItemList blacklist;

    private ClaimedResidence()
    {
        subzones = Collections.synchronizedMap(new HashMap<String, ClaimedResidence>());
        areas = Collections.synchronizedMap(new HashMap<String, CuboidArea>());
        bank = new ResidenceBank(this);
        blacklist = new ResidenceItemList(this, ListType.BLACKLIST);
        ignorelist = new ResidenceItemList(this, ListType.IGNORELIST);
    }

    public ClaimedResidence(String creator, String creationWorld) {
        this();
        perms = new ResidencePermissions(this,creator, creationWorld);
    }

    public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence) {
        this(creator, creationWorld);
        parent = parentResidence;
    }

    public boolean addArea(CuboidArea area, String name)
    {
        return addArea(null,area,name,true);
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin) {
        if(!Residence.validName(name))
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        if(areas.containsKey(name))
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaExists"));
            return false;
        }
        if (!area.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaDiffWorld"));
            return false;
        }
        if(parent==null)
        {
            String collideResidence = Residence.getResidenceManager().checkAreaCollision(area, this);
            if(collideResidence!=null)
            {
                if(player!=null)
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaCollision","§e" + collideResidence));
                return false;
            }
        }
        else
        {
            String[] szs = parent.listSubzones();
            for(String sz : szs)
            {
                ClaimedResidence res = parent.getSubzone(sz);
                if(res!=null && res != this)
                {
                    if(res.checkCollision(area))
                    {
                        if(player!=null)
                            player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaSubzoneCollision","§e" + sz));
                        return false;
                    }
                }
            }
        }
        if(!resadmin && player!=null)
        {
            if (!this.perms.hasResidencePermission(player, true)) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (parent != null) {
                if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaNotWithinParent"));
                    return false;
                }
                if(!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(),"subzone", true))
                {
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("ParentNoPermission"));
                    return false;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if(!group.canCreateResidences() && !Residence.getPermissionManager().hasAuthority(player, "residence.create"))
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if(areas.size()>=group.getMaxPhysicalPerResidence())
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaMaxPhysical"));
                return false;
            }
            if(!group.inLimits(area))
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaSizeLimit"));
                return false;
            }
            if(group.getMinHeight()>area.getLowLoc().getBlockY())
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaLowLimit","§e" + group.getMinHeight()));
                return false;
            }
            if(group.getMaxHeight()<area.getHighLoc().getBlockY())
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaHighLimit","§e" + group.getMaxHeight()));
                return false;
            }
            if(parent==null && Residence.getConfigManager().enableEconomy())
            {
                int chargeamount = (int) Math.ceil((double)area.getSize() * group.getCostPerBlock());
                if(!TransactionManager.chargeEconomyMoney(player, chargeamount))
                {
                    return false;
                }
            }
        }
        areas.put(name, area);
        if(player!=null)
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("AreaCreate","§e" + name));
        return true;
    }

    public boolean replaceArea(CuboidArea neware, String name)
    {
        return this.replaceArea(null, neware, name, true);
    }

    public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {
        if (!areas.containsKey(name)) {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaNonExist"));
            return false;
        }
        CuboidArea oldarea = areas.get(name);
        if (!newarea.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaDiffWorld"));
            return false;
        }
        if (parent == null) {
            String collideResidence = Residence.getResidenceManager().checkAreaCollision(newarea, this);
            if (collideResidence != null) {
                if(player!=null)
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaCollision","§e" + collideResidence));
                return false;
            }
        } else {
            String[] szs = parent.listSubzones();
            for (String sz : szs) {
                ClaimedResidence res = parent.getSubzone(sz);
                if (res != null && res != this) {
                    if (res.checkCollision(newarea)) {
                        if(player!=null)
                            player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaSubzoneCollision","§e" + sz));
                        return false;
                    }
                }
            }
        }
        if (!resadmin && player!=null) {
            if (!this.perms.hasResidencePermission(player, true)) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (parent != null) {
                if (!parent.containsLoc(newarea.getHighLoc()) || !parent.containsLoc(newarea.getLowLoc())) {
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaNotWithinParent"));
                    return false;
                }
                if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("ParentNoPermission"));
                    return false;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if (!group.canCreateResidences() && !Residence.getPermissionManager().hasAuthority(player, "residence.create")) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (!group.inLimits(newarea)) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaSizeLimit"));
                return false;
            }
            if (group.getMinHeight() > newarea.getLowLoc().getBlockY()) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaLowLimit","§e" + group.getMinHeight()));
                return false;
            }
            if (group.getMaxHeight() < newarea.getHighLoc().getBlockY()) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaHighLimit","§e" + group.getMaxHeight()));
                return false;
            }
            if (parent == null && Residence.getConfigManager().enableEconomy()) {
                int chargeamount = (int) Math.ceil((double) (newarea.getSize()-oldarea.getSize()) * group.getCostPerBlock());
                if(chargeamount>0)
                {
                    if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
                        return false;
                    }
                }
            }

        }
        areas.remove(name);
        areas.put(name, newarea);
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("AreaUpdate"));
        return true;
    }

    public boolean addSubzone(String name, Location loc1, Location loc2)
    {
        return this.addSubzone(null, loc1, loc2, name, true);
    }

    public boolean addSubzone(Player player, Location loc1, Location loc2, String name, boolean resadmin) {
        if(!Residence.validName(name))
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("SubzoneSelectInside"));
            return false;
        }
        if (subzones.containsKey(name)) {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("SubzoneExists","§e" + name));
            return false;
        }
        if(!resadmin && player!=null)
        {
            if (!this.perms.hasResidencePermission(player, true)) {
                if(!this.perms.playerHas(player.getName(), "subzone", false))
                {
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                    return false;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if(this.getZoneDepth()>=group.getMaxSubzoneDepth())
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("SubzoneMaxDepth"));
                return false;
            }
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        synchronized (subzones) {
            for (Entry<String, ClaimedResidence> resEntry : set) {
                ClaimedResidence res = resEntry.getValue();
                if (res.checkCollision(newArea)) {
                    if(player!=null)
                        player.sendMessage("§c"+Residence.getLanguage().getPhrase("SubzoneCollide","§e" + name));
                    return false;
                }
            }
        }
        ClaimedResidence newres = new ClaimedResidence(player.getName(), perms.getWorld(), this);
        newres.addArea(player, newArea, name, resadmin);
        if(newres.getAreaCount()!=0)
        {
            newres.getPermissions().applyDefaultFlags();
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            newres.setEnterMessage(group.getDefaultEnterMessage());
            newres.setLeaveMessage(group.getDefaultLeaveMessage());
            if(Residence.getConfigManager().flagsInherit())
                newres.getPermissions().setParent(perms);
            subzones.put(name, newres);
            if(player!=null)
                player.sendMessage("§a"+Residence.getLanguage().getPhrase("SubzoneCreate","§e" + name));
            return true;
        }
        else
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("SubzoneCreateFail","§e" + name));
            return false;
        }
    }

    public String getSubzoneNameByLoc(Location loc) {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        ClaimedResidence res = null;
        String key = null;
        for (Entry<String, ClaimedResidence> entry : set) {
            res = entry.getValue();
            if (res.containsLoc(loc)) {
                key = entry.getKey();
                break;
            }
        }
        if (key == null) {
            return null;
        }
        String subname = res.getSubzoneNameByLoc(loc);
        if (subname != null) {
            return key + "." + subname;
        }
        return key;
    }

    public ClaimedResidence getSubzoneByLoc(Location loc) {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        boolean found = false;
        ClaimedResidence res = null;
        synchronized (subzones) {
            for (Entry<String, ClaimedResidence> entry : set) {
                res = entry.getValue();
                if (res.containsLoc(loc)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            return null;
        }
        ClaimedResidence subrez = res.getSubzoneByLoc(loc);
        if (subrez == null) {
            return res;
        }
        return subrez;
    }

    public ClaimedResidence getSubzone(String subzonename) {
        if (!subzonename.contains(".")) {
            return subzones.get(subzonename);
        }
        String split[] = subzonename.split("\\.");
        ClaimedResidence get = subzones.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (get == null) {
                return null;
            }
            get = get.getSubzone(split[i]);
        }
        return get;
    }

    public String getSubzoneNameByRes(ClaimedResidence res)
    {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        for(Entry<String, ClaimedResidence> entry : set)
        {
            if(entry.getValue() == res)
                return entry.getKey();
            String n = entry.getValue().getSubzoneNameByRes(res);
            if(n!=null)
                return entry.getKey() + "." + n;
        }
        return null;
    }

    public String[] getSubzoneList() {
        ArrayList zones = new ArrayList<String>();
        Set<String> set = subzones.keySet();
        synchronized (subzones) {
            for (String key : set) {
                if (key != null) {
                    zones.add(key);
                }
            }
        }
        return (String[]) zones.toArray();
    }

    public boolean checkCollision(CuboidArea area) {
        Set<String> set = areas.keySet();
        for (String key : set) {
            CuboidArea checkarea = areas.get(key);
            if (checkarea != null) {
                if (checkarea.checkCollision(area)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsLoc(Location loc) {
        Collection<CuboidArea> keys = areas.values();
        for (CuboidArea key : keys) {
            if (key.containsLoc(loc))
            {
                if(parent!=null)
                    return parent.containsLoc(loc);
                return true;
            }
        }
        return false;
    }

    public ClaimedResidence getParent() {
        return parent;
    }

    public ClaimedResidence getTopParent()
    {
        if(parent==null)
            return this;
        return parent.getTopParent();
    }

    public boolean removeSubzone(String name)
    {
        return this.removeSubzone(null, name, true);
    }

    public boolean removeSubzone(Player player, String name, boolean resadmin) {
        ClaimedResidence res = subzones.get(name);
        if (player!=null && !res.perms.hasResidencePermission(player, true) && !resadmin) {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
        subzones.remove(name);
        if(player!=null)
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("SubzoneRemove","§e"+name+"§a"));
        return true;
    }

    public long getTotalSize() {
        Collection<CuboidArea> set = areas.values();
        long size = 0;
        synchronized (areas) {
            for (CuboidArea entry : set) {
                size = size + entry.getSize();
            }
        }
        return size;
    }

    public CuboidArea[] getAreaArray() {
        CuboidArea[] temp = new CuboidArea[areas.size()];
        int i=0;
        for(CuboidArea area : areas.values())
        {
            temp[i] = area;
            i++;
        }
        return temp;
    }

    public ResidencePermissions getPermissions() {
        return perms;
    }

    public String getEnterMessage() {
        return enterMessage;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setEnterMessage(String message) {
        enterMessage = message;
    }

    public void setLeaveMessage(String message) {
        leaveMessage = message;
    }

    public void setEnterLeaveMessage(Player player, String message, boolean enter, boolean resadmin)
    {
        if(message!=null && !Residence.validString(message))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidCharacters"));
            return;
        }
        if(message!=null)
            if(message.equals(""))
                message = null;
        PermissionGroup group = Residence.getPermissionManager().getGroup(perms.getOwner(), perms.getWorld());
        if(!group.canSetEnterLeaveMessages() && !resadmin)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("OwnerNoPermission"));
            return;
        }
        if(!perms.hasResidencePermission(player, false) && !resadmin)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if(enter)
            this.setEnterMessage(message);
        else
            this.setLeaveMessage(message);
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("MessageChange"));
    }

    public Location getOutsideFreeLoc(Location insideLoc)
    {
        int maxIt = 100;
        CuboidArea area = this.getAreaByLoc(insideLoc);
        if(area==null)
            return insideLoc;
        Location highLoc = area.getHighLoc();
        Location newLoc = new Location(highLoc.getWorld(), highLoc.getBlockX(), highLoc.getBlockY(), highLoc.getBlockZ());
        boolean found = false;
        int it = 0;
        while (!found && it < maxIt) {
            it++;
            Location lowLoc;
            newLoc.setX(newLoc.getBlockX() + 1);
            newLoc.setZ(newLoc.getBlockZ() + 1);
            lowLoc = new Location(newLoc.getWorld(), newLoc.getBlockX(), 126, newLoc.getBlockZ());
            newLoc.setY(127);
            while ((newLoc.getBlock().getTypeId() != 0 || lowLoc.getBlock().getTypeId() == 0) && lowLoc.getBlockY() > -126) {
                newLoc.setY(newLoc.getY() - 1);
                lowLoc.setY(lowLoc.getY() - 1);
            }
            if (newLoc.getBlock().getTypeId() == 0 && lowLoc.getBlock().getTypeId() != 0) {
                found = true;
            }
        }
        if(found)
            return newLoc;
        else
        {
            World world = Residence.getServ().getWorld(perms.getWorld());
            if(world!=null)
                return world.getSpawnLocation();
            return insideLoc;
        }
    }

    protected CuboidArea getAreaByLoc(Location loc)
    {
        synchronized(areas)
        {
            for(CuboidArea thisarea : areas.values())
            {
                if(thisarea.containsLoc(loc))
                    return thisarea;
            }
        }
        return null;
    }

    public String[] listSubzones()
    {
        String list[] = new String[subzones.size()];
        int i = 0;
        synchronized(subzones)
        {
            for(String res : subzones.keySet())
            {
                list[i] = res;
                i++;
            }
        }
        return list;
    }

    public void printSubzoneList(Player player, int page)
    {
        ArrayList<String> temp = new ArrayList<String>();
        for(Entry<String, ClaimedResidence> sz : subzones.entrySet())
        {
            temp.add("§a"+sz.getKey() + "§e - " +Residence.getLanguage().getPhrase("Owner")+": " + sz.getValue().getOwner());
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Subzones"), temp, page);
    }

    public void printAreaList(Player player, int page)
    {
        ArrayList<String> temp = new ArrayList<String>();
        for(String area : areas.keySet())
        {
            temp.add(area);
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
    }

    public void printAdvancedAreaList(Player player, int page)
    {
        ArrayList<String> temp = new ArrayList<String>();
        for(Entry<String, CuboidArea> entry : areas.entrySet())
        {
            CuboidArea a = entry.getValue();
            Location h = a.getHighLoc();
            Location l = a.getLowLoc();
            temp.add("§a{§eID:§c"+entry.getKey()+" §eP1:§c("+h.getBlockX()+","+h.getBlockY()+","+h.getBlockZ()+") §eP2:§c("+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ()+") §e(Size:§c" + a.getSize() + "§e)§a} ");
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
    }

    public String[] getAreaList()
    {
        String arealist[] = new String[areas.size()];
        int i = 0;
        for(Entry<String, CuboidArea> entry : areas.entrySet())
        {
            arealist[i] = entry.getKey();
            i++;
        }
        return arealist;
    }

    public int getZoneDepth()
    {
        int count = 0;
        ClaimedResidence res = parent;
        while(res!=null)
        {
            count++;
            res = res.getParent();
        }
        return count;
    }

    public void setTpLoc(Player player, boolean resadmin)
    {
        if(!this.perms.hasResidencePermission(player, false) && !resadmin)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if(!this.containsLoc(player.getLocation()))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NotInResidence"));
            return;
        }
        tpLoc = player.getLocation();
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("SetTeleportLocation"));
    }

    public void tpToResidence(Player reqPlayer, Player targetPlayer, boolean resadmin) {
        if (!resadmin) {
            PermissionGroup group = Residence.getPermissionManager().getGroup(reqPlayer);
            if (!group.hasTpAccess()) {
                reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("TeleportDeny"));
                return;
            }
            if (!reqPlayer.equals(targetPlayer)) {
                reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
            if (!this.perms.playerHas(reqPlayer.getName(), "tp", true)) {
                reqPlayer.sendMessage("§c"+Residence.getLanguage().getPhrase("TeleportNoFlag"));
                return;
            }
        }
        if (tpLoc != null) {
            ResidenceTPEvent tpevent = new ResidenceTPEvent(this,tpLoc, targetPlayer, reqPlayer);
            Residence.getServ().getPluginManager().callEvent(tpevent);
            if(!tpevent.isCancelled())
            {
                targetPlayer.teleport(tpLoc);
                targetPlayer.sendMessage("§a"+Residence.getLanguage().getPhrase("TeleportSuccess"));
            }
        } else {
            CuboidArea area = areas.values().iterator().next();
            if (area == null) {
                reqPlayer.sendMessage("§cCould not find area to teleport to...");
                return;
            }
            Location targloc = this.getOutsideFreeLoc(area.getHighLoc());
            ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
            Residence.getServ().getPluginManager().callEvent(tpevent);
            if(!tpevent.isCancelled())
            {
                targetPlayer.teleport(targloc);
                targetPlayer.sendMessage("§e"+Residence.getLanguage().getPhrase("TeleportNear"));
            }
        }
    }

    public String getAreaIDbyLoc(Location loc)
    {
        for(Entry<String, CuboidArea> area : areas.entrySet())
        {
            if(area.getValue().containsLoc(loc))
                return area.getKey();
        }
        return null;
    }

    public void removeArea(String id)
    {
        areas.remove(id);
    }

    public void removeArea(Player player, String id, boolean resadmin)
    {

        if(this.getPermissions().hasResidencePermission(player, true) || resadmin)
        {
            if(!areas.containsKey(id))
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaNonExist"));
                return;
            }
            if(areas.size()==1 && !Residence.getConfigManager().allowEmptyResidences())
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaRemoveLast"));
                return;
            }
            removeArea(id);
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("AreaRemove"));
        }
        else
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new HashMap<String,Object>();
        Map<String,Object> areamap = new HashMap<String,Object>();
        root.put("EnterMessage", enterMessage);
        root.put("LeaveMessage", leaveMessage);
        root.put("StoredMoney", bank.getStoredMoney());
        root.put("BlackList", blacklist.save());
        root.put("IgnoreList", ignorelist.save());
        for(Entry<String, CuboidArea> entry : areas.entrySet())
        {
            areamap.put(entry.getKey(), entry.getValue().save());
        }
        root.put("Areas", areamap);
        Map<String,Object> subzonemap = new HashMap<String,Object>();
        for(Entry<String, ClaimedResidence> sz : subzones.entrySet())
        {
            subzonemap.put(sz.getKey(), sz.getValue().save());
        }
        root.put("Subzones", subzonemap);
        root.put("Permissions", perms.save());
        if(tpLoc!=null)
        {
            Map<String,Object> tpmap = new HashMap<String,Object>();
            tpmap.put("X", tpLoc.getBlockX());
            tpmap.put("Y", tpLoc.getBlockY());
            tpmap.put("Z", tpLoc.getBlockZ());
            root.put("TPLoc", tpmap);
        }
        return root;
    }

    public static ClaimedResidence load(Map<String,Object> root, ClaimedResidence parent) throws Exception {
        ClaimedResidence res = new ClaimedResidence();
        if(root == null)
            throw new Exception("Null residence!");
        res.enterMessage = (String) root.get("EnterMessage");
        if(res.enterMessage!=null && !Residence.validString(res.enterMessage))
            res.enterMessage = null;
        res.leaveMessage = (String) root.get("LeaveMessage");
        if(res.leaveMessage!=null && !Residence.validString(res.leaveMessage))
            res.leaveMessage = null;
        if(root.containsKey("StoredMoney"))
            res.bank.setStoredMoney((Integer)root.get("StoredMoney"));
        if(root.containsKey("BlackList"))
            res.blacklist = ResidenceItemList.load(res,(Map<String, Object>) root.get("BlackList"));
        if(root.containsKey("IgnoreList"))
            res.ignorelist = ResidenceItemList.load(res,(Map<String, Object>) root.get("IgnoreList"));
        Map<String,Object> areamap = (Map<String, Object>) root.get("Areas");
        res.perms = ResidencePermissions.load(res,(Map<String, Object>) root.get("Permissions"));
        World world = Residence.getServ().getWorld(res.perms.getWorld());
        if(world==null)
            throw new Exception("Cant Find World: " + res.perms.getWorld());
        for(Entry<String, Object> map : areamap.entrySet())
        {
            res.areas.put(map.getKey(), CuboidArea.load((Map<String, Object>) map.getValue(),world));
        }
        Map<String,Object> subzonemap = (Map<String, Object>) root.get("Subzones");
        for(Entry<String, Object> map : subzonemap.entrySet())
        {
            ClaimedResidence subres = ClaimedResidence.load((Map<String, Object>) map.getValue(), res);
            if(Residence.getConfigManager().flagsInherit())
                subres.getPermissions().setParent(res.getPermissions());
            res.subzones.put(map.getKey(), subres);
        }
        res.parent = parent;
        Map<String,Object> tploc = (Map<String, Object>) root.get("TPLoc");
        if(tploc != null)
        {
            res.tpLoc = new Location(world,(Integer)tploc.get("X"),(Integer)tploc.get("Y"),(Integer)tploc.get("Z"));
        }
        return res;
    }

    public int getAreaCount()
    {
        return areas.size();
    }

    public boolean renameSubzone(String oldName, String newName)
    {
        return this.renameSubzone(null, oldName, newName, true);
    }

    public boolean renameSubzone(Player player, String oldName, String newName, boolean resadmin)
    {
        if(!Residence.validName(newName))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        ClaimedResidence res = subzones.get(oldName);
        if(res==null)
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidSubzone"));
            return false;
        }
        if(player!=null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin)
        {
            if(player!=null)
                player.sendMessage("§c" + Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
        if(subzones.containsKey(newName))
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("SubzoneExists","§e"+newName));
            return false;
        }
        subzones.put(newName, res);
        subzones.remove(oldName);
        if(player!=null)
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("SubzoneRename",oldName+"."+newName));
        return true;
    }

    public boolean renameArea(String oldName, String newName)
    {
        return this.renameArea(null, oldName, newName, true);
    }

    public boolean renameArea(Player player, String oldName, String newName, boolean resadmin)
    {
        if(!Residence.validName(newName))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        if(player==null || perms.hasResidencePermission(player, true) || resadmin)
        {
            if(areas.containsKey(newName))
            {
                if(player!=null)
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaExists"));
                return false;
            }
            CuboidArea area = areas.get(oldName);
            if(area == null)
            {
                if(player!=null)
                    player.sendMessage("§c"+Residence.getLanguage().getPhrase("AreaInvalidName"));
                return false;
            }
            areas.put(newName, area);
            areas.remove(oldName);
            if(player!=null)
                player.sendMessage("§a"+Residence.getLanguage().getPhrase("AreaRename", oldName+"."+newName));
            return true;
        }
        else
        {
            if(player!=null)
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
    }

    public CuboidArea getArea(String name)
    {
        return areas.get(name);
    }

    public String getName()
    {
        return Residence.getResidenceManager().getNameByRes(this);
    }

    public void remove()
    {
        String name = getName();
        if(name!=null)
        {
            Residence.getResidenceManager().removeResidence(name);
        }
    }

    public ResidenceBank getBank()
    {
        return bank;
    }

    public String getWorld()
    {
        return perms.getWorld();
    }

    public String getOwner()
    {
        return perms.getOwner();
    }

    public ResidenceItemList getItemBlacklist()
    {
        return blacklist;
    }

    public ResidenceItemList getItemIgnoreList()
    {
        return ignorelist;
    }

    public ArrayList<Player> getPlayersInResidence()
    {
        ArrayList<Player> within = new ArrayList<Player>();
        Player[] players = Residence.getServ().getOnlinePlayers();
        for(Player player : players)
        {
            if(this.containsLoc(player.getLocation()))
            {
                within.add(player);
            }
        }
        return within;
    }
}

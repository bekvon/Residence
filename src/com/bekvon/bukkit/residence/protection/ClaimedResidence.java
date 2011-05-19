/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
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

    private ClaimedResidence()
    {
        subzones = Collections.synchronizedMap(new HashMap<String, ClaimedResidence>());
        areas = Collections.synchronizedMap(new HashMap<String, CuboidArea>());
        bank = new ResidenceBank(this);
    }

    public ClaimedResidence(String creator, String creationWorld) {
        this();
        perms = new ResidencePermissions(this,creator, creationWorld);
    }

    public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence) {
        this(creator, creationWorld);
        parent = parentResidence;
    }

    public void addArea(Player player, CuboidArea area, String name) {
        name = name.replace(".", "_");
        name = name.replace(":", "_");
        if(areas.containsKey(name))
        {
            player.sendMessage("§cArea name already exists.");
            return;
        }
        if (!area.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
            player.sendMessage("§cArea is in a different world from residence.");
            return;
        }
        if(area.getSize()==0)
        {
            player.sendMessage("§cError, 0 sized area.");
            return;
        }
        if(!Residence.getPermissionManager().isResidenceAdmin(player))
        {
            if (!this.perms.hasResidencePermission(player, true)) {
                player.sendMessage("§cYou dont have permission to do this.");
                return;
            }
            if (parent != null) {
                if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
                    player.sendMessage("§cArea is not within parent area.");
                    return;
                }
                if(!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(),"subzone", true))
                {
                    player.sendMessage("§cYou dont have permission to make changes to the parent area.");
                    return;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if(!group.canCreateResidences() && !Residence.getPermissionManager().hasAuthority(player, "residence.create", false))
            {
                player.sendMessage("You dont have permission to create residences.");
                return;
            }
            if(areas.size()>=group.getMaxPhysicalPerResidence())
            {
                player.sendMessage("§cYou've reached the max physical areas allowed for your residence.");
                return;
            }
            if(!group.inLimits(area))
            {
                player.sendMessage("§cArea size is not within your allowed limits.");
                return;
            }
            if(group.getMinHeight()>area.getLowLoc().getBlockY())
            {
                player.sendMessage("You are not allowed to protect this deep, minimum height:" + group.getMinHeight());
                return;
            }
            if(group.getMaxHeight()<area.getHighLoc().getBlockY())
            {
                player.sendMessage("You are not allowed to protect this high up, maximum height:" + group.getMaxHeight());
                return;
            }
            if(parent==null && Residence.getConfig().enableEconomy())
            {
                int chargeamount = (int) Math.ceil((double)area.getSize() * group.getCostPerBlock());
                if(!TransactionManager.chargeEconomyMoney(player, chargeamount, "new residence area"))
                {
                    return;
                }
            }
        }
        if(parent==null)
        {
            String collideResidence = Residence.getResidenceManger().checkAreaCollision(area, this);
            if(collideResidence!=null)
            {
                player.sendMessage("§cArea collides with residence: §e" + collideResidence);
                return;
            }
        }
        else
        {
            String[] szs = parent.listSubzones();
            for(String sz : szs)
            {
                ClaimedResidence res = parent.getSubzone(sz);
                if(res!=null)
                {
                    if(res.checkCollision(area))
                    {
                        player.sendMessage("§cArea collides with subzone: §e" + sz);
                        return;
                    }
                }
            }
        }
        areas.put(name, area);
        player.sendMessage("§aArea created. ID:§e " + name);
    }

    public void addSubzone(Player player, Location loc1, Location loc2, String name) {
        name = name.replace(".", "_");
        name = name.replace(":", "_");
        if(!Residence.getPermissionManager().isResidenceAdmin(player))
        {
            if (!this.perms.hasResidencePermission(player, true)) {
                if(!this.perms.playerHas(player.getName(), "subzone", false))
                {
                    player.sendMessage("§cYou dont have permission to do this.");
                    return;
                }
            }
            if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
                player.sendMessage("§cBoth selection points must be inside the residence.");
                return;
            }
            if (subzones.containsKey(name)) {
                player.sendMessage("§cSubzone name already exists.");
                return;
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if(this.getZoneDepth()>=group.getMaxSubzoneDepth())
            {
                player.sendMessage("§cYou've reached the max allowed subzone depth.");
                return;
            }
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        synchronized (subzones) {
            for (Entry<String, ClaimedResidence> resEntry : set) {
                ClaimedResidence res = resEntry.getValue();
                if (res.checkCollision(newArea)) {
                    player.sendMessage("§cSubzone collides with subzone: §e" + resEntry.getKey());
                    return;
                }
            }
        }
        ClaimedResidence newres = new ClaimedResidence(player.getName(), perms.getWorld(), this);
        newres.addArea(player, newArea, name);
        if(newres.getAreaCount()!=0)
        {
            newres.getPermissions().applyDefaultFlags();
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            newres.setEnterMessage(group.getDefaultEnterMessage());
            newres.setLeaveMessage(group.getDefaultLeaveMessage());
            if(Residence.getConfig().flagsInherit())
                newres.getPermissions().setParent(perms);
            subzones.put(name, newres);
            player.sendMessage("§aCreated subzone: §e" + name);
        }
        else
        {
            player.sendMessage("§cUnable to create subzone...");
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

    public void removeSubzone(Player player, String name) {
        ClaimedResidence res = subzones.get(name);
        if (!res.perms.hasResidencePermission(player, true)) {
            player.sendMessage("§cYou dont have permission to do this.");
            return;
        }
        subzones.remove(name);
        player.sendMessage("§aSubzone removed.");
    }

    public void removeSubzone(String name) {
        subzones.remove(name);
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

    public void setEnterLeaveMessage(Player player, String message, boolean enter)
    {
        if(message!=null)
            if(message.equals(""))
                message = null;
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        PermissionGroup group = Residence.getPermissionManager().getGroup(perms.getOwner(), perms.getWorld());
        if(!group.canSetEnterLeaveMessages() && !resadmin)
        {
            player.sendMessage("§cOwner is not allowed to change enter / leave messages.");
            return;
        }
        if(!perms.hasResidencePermission(player, false) && !resadmin)
        {
            player.sendMessage("§cYou dont have permission to do this.");
            return;
        }
        if(enter)
            this.setEnterMessage(message);
        else
            this.setLeaveMessage(message);
        player.sendMessage("§aMessage set...");
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

    public String CSVSubzoneList()
    {
        String list = "";
        synchronized(subzones)
        {
            for(String res : subzones.keySet())
            {
                if(list.equals(""))
                    list = res;
                else
                    list = list + ", " + res;
            }
        }
        return list;
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

    public String getFormattedAreaList()
    {
        StringBuilder s = new StringBuilder();
        for(Entry<String, CuboidArea> entry : areas.entrySet())
        {
            CuboidArea a = entry.getValue();
            Location h = a.getHighLoc();
            Location l = a.getLowLoc();
            s.append("§a{§eID:§c").append(entry.getKey()).append(" §eP1:§c(").append(h.getBlockX()).append(",").append(h.getBlockY()).append(",").append(h.getBlockZ()).append(") §eP2:§c(").append(l.getBlockX()).append(",").append(l.getBlockY()).append(",").append(l.getBlockZ()+") §e(Size:§c" + a.getSize() + "§e)§a} ");
        }
        return s.toString();
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

    public void setTpLoc(Player player)
    {
        if(!this.perms.hasResidencePermission(player, false))
        {
            player.sendMessage("§cYou dont have permission to set the teleport location.");
            return;
        }
        if(!this.containsLoc(player.getLocation()))
        {
            player.sendMessage("§cYou are not in the residence.");
            return;
        }
        tpLoc = player.getLocation();
        player.sendMessage("§aTeleport Location Set...");
    }

    public void tpToResidence(Player reqPlayer, Player targetPlayer) {
        if (!Residence.getPermissionManager().isResidenceAdmin(reqPlayer)) {
            PermissionGroup group = Residence.getPermissionManager().getGroup(reqPlayer);
            if (!group.hasTpAccess()) {
                reqPlayer.sendMessage("§cYou dont have teleport access.");
                return;
            }
            if (!reqPlayer.equals(targetPlayer)) {
                reqPlayer.sendMessage("§cOnly admins can teleport other players.");
                return;
            }
            if (!this.perms.playerHas(reqPlayer.getName(), "tp", true)) {
                reqPlayer.sendMessage("§cThe owner has not allowed you to teleport to this residence.");
                return;
            }
        }
        if(tpLoc!=null)
        {
            targetPlayer.teleport(tpLoc);
            targetPlayer.sendMessage("§aTeleported!");
        }
        else
        {
            CuboidArea area = areas.values().iterator().next();
            if(area==null)
            {
                reqPlayer.sendMessage("Could not find area to teleport to...");
                return;
            }
            targetPlayer.teleport(this.getOutsideFreeLoc(area.getHighLoc()));
            targetPlayer.sendMessage("§eTeleported to near residence.");
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

    public void removeArea(Player player, String id)
    {

        if(this.getPermissions().hasResidencePermission(player, true))
        {
            if(!areas.containsKey(id))
            {
                player.sendMessage("§cInvalid Area ID");
                return;
            }
            if(areas.size()==1 && !Residence.getConfig().allowEmptyResidences())
            {
                player.sendMessage("§cCannot remove the residence's last area, use '/res remove' instead...");
                return;
            }
            removeArea(id);
            player.sendMessage("§aArea ID §e" + id + "§a removed...");
        }
        else
            player.sendMessage("§cYou dont have permission...");
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new HashMap<String,Object>();
        Map<String,Object> areamap = new HashMap<String,Object>();
        root.put("EnterMessage", enterMessage);
        root.put("LeaveMessage", leaveMessage);
        root.put("StoredMoney", bank.getStoredMoney());
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
            throw new Exception("Invalid residence...");
        res.enterMessage = (String) root.get("EnterMessage");
        res.leaveMessage = (String) root.get("LeaveMessage");
        if(root.containsKey("StoredMoney"))
            res.bank.setStoredMoney((Integer)root.get("StoredMoney"));
        Map<String,Object> areamap = (Map<String, Object>) root.get("Areas");
        res.perms = ResidencePermissions.load(res,(Map<String, Object>) root.get("Permissions"));
        World world = Residence.getServ().getWorld(res.perms.getWorld());
        if(world==null)
            throw new Exception("Can't find world:" + res.perms.getWorld());
        for(Entry<String, Object> map : areamap.entrySet())
        {
            res.areas.put(map.getKey(), CuboidArea.load((Map<String, Object>) map.getValue(),world));
        }
        Map<String,Object> subzonemap = (Map<String, Object>) root.get("Subzones");
        for(Entry<String, Object> map : subzonemap.entrySet())
        {
            ClaimedResidence subres = ClaimedResidence.load((Map<String, Object>) map.getValue(), res);
            if(Residence.getConfig().flagsInherit())
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

    public void renameSubzone(Player player, String oldName, String newName)
    {
        newName = newName.replace(".", "_");
        newName = newName.replace(":", "_");
        ClaimedResidence res = subzones.get(oldName);
        if(res==null)
        {
            player.sendMessage("§cInvalid Subzone...");
        	return;
        }
        if(!res.getPermissions().hasResidencePermission(player, true))
        {
            player.sendMessage("§cYou dont have permission...");
            return;
        }
        if(subzones.containsKey(newName))
        {
            player.sendMessage("§cNew subzone name already exists...");
            return;
        }
        subzones.put(newName, res);
        subzones.remove(oldName);
        player.sendMessage("§aRenamed " + oldName + " to " + newName + "...");
    }
    public void renameArea(Player player, String oldName, String newName)
    {
        newName = newName.replace(".", "_");
        newName = newName.replace(":", "_");
        if(perms.hasResidencePermission(player, true))
        {
            if(areas.containsKey(newName))
            {
                player.sendMessage("§cArea name already exists...");
                return;
            }
            CuboidArea area = areas.get(oldName);
            if(area == null)
            {
                player.sendMessage("§cInvalid Area Name...");
                return;
            }
            areas.put(newName, area);
            areas.remove(oldName);
            player.sendMessage("§aRenamed area " + oldName + " to " + newName);
        }
    }

    public CuboidArea getArea(String name)
    {
        return areas.get(name);
    }

    public String getName()
    {
        return Residence.getResidenceManger().getNameByRes(this);
    }

    public ResidenceBank getBank()
    {
        return bank;
    }
}

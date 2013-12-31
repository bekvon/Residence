package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

public class ClaimedResidence extends CuboidArea {
    private String name;
    private ClaimedResidence parent;
    private Map<String, ClaimedResidence> subzones;
    private ResidencePermissions perms;
    private Location tpLoc;
    private String enterMessage;
    private String leaveMessage;

    public ClaimedResidence() { }

    public ClaimedResidence(String name, CuboidArea area) {
        this("Server Land", name, area);
    }

    public ClaimedResidence(String creator, String name, CuboidArea area) {
        super(area);
        this.name = name;
        this.subzones = new HashMap<String, ClaimedResidence>();
        this.perms = new ResidencePermissions(this, creator, name);
    }

    public ClaimedResidence(String creator, String name, ClaimedResidence parentResidence, CuboidArea area) {
        this(creator, name, area);
        parent = parentResidence;
    }

    public boolean addSubzone(String name, Location loc1, Location loc2) {
        if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
            return false;
        }
        if (subzones.containsKey(name)) {
            return false;
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        for (ClaimedResidence subzone : getSubzoneList()) {
            if (subzone.checkCollision(newArea)) {
                return false;
            }
        }
        ClaimedResidence newres  = new ClaimedResidence(getOwner(), perms.getWorld(), this, newArea);
        subzones.put(name, newres);
        return true;
    }

    public ClaimedResidence getSubzoneByLoc(Location loc) {
        ClaimedResidence residence = null;
        for (ClaimedResidence res : subzones.values()) {
            if (res.containsLoc(loc)) {
                residence = res;
                break;
            }
        }
        if (residence == null) {
            return null;
        }
        ClaimedResidence subrez = residence.getSubzoneByLoc(loc);
        if (subrez == null) {
            return residence;
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

    public Collection<ClaimedResidence> getSubzoneList() {
        return subzones.values();
    }

    public Collection<String> getSubzoneNameList() {
        return subzones.keySet();
    }

    public ClaimedResidence getParent() {
        return parent;
    }

    public ClaimedResidence getTopParent() {
        if (parent != null) {
            return this;
        }
        return parent.getTopParent();
    }

    public int getSubzoneDepth() {
        int count = 0;
        ClaimedResidence res = parent;
        while (res != null) {
            count++;
            res = res.parent;
        }
        return count;
    }

    public boolean removeSubzone(String subzone) {
        return subzones.remove(subzone) != null;
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

    public Location getOutsideFreeLoc(Location insideLoc) {
        int maxIt = 100;
        if (!containsLoc(insideLoc)) {
            return insideLoc;
        }
        Location highLoc = getHighLoc();
        Location newLoc = new Location(highLoc.getWorld(), highLoc.getBlockX(), highLoc.getBlockY(), highLoc.getBlockZ());
        boolean found = false;
        int it = 0;
        while (!found && it < maxIt) {
            it++;
            Location lowLoc;
            newLoc.setX(newLoc.getBlockX() + 1);
            newLoc.setZ(newLoc.getBlockZ() + 1);
            lowLoc = new Location(newLoc.getWorld(), newLoc.getBlockX(), 254, newLoc.getBlockZ());
            newLoc.setY(255);
            while ((newLoc.getBlock().getTypeId() != 0 || lowLoc.getBlock().getTypeId() == 0) && lowLoc.getBlockY() > -126) {
                newLoc.setY(newLoc.getY() - 1);
                lowLoc.setY(lowLoc.getY() - 1);
            }
            if (newLoc.getBlock().getTypeId() == 0 && lowLoc.getBlock().getTypeId() != 0) {
                found = true;
            }
        }
        if (found) {
            return newLoc;
        } else {
            World world = Residence.getInstance().getServer().getWorld(perms.getWorld());
            if (world != null) {
                return world.getSpawnLocation();
            }
            return insideLoc;
        }
    }

    public boolean setTeleportLocation(Location location) {
        if (!this.containsLoc(location)) {
            return false;
        }
        tpLoc = location;
        return true;
    }

    public boolean rename(String newName) {
        if (parent == null) {
            if (Residence.getInstance().getResidenceManager().rename(this, newName)) {
                this.name = newName;
                return true;
            }
            return false;
        } else {
            ClaimedResidence parent = getParent();
            if (parent.renameSubzone(name, newName)) {
                name = newName;
                return true;
            }
            return false;
        }
    }

    public boolean renameSubzone(String oldName, String newName) {
        ClaimedResidence res = subzones.get(oldName);
        if (res == null) {
            return false;
        }
        if (subzones.containsKey(newName)) {
            return false;
        }
        subzones.put(newName, res);
        subzones.remove(oldName);
        return true;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return perms.getOwner();
    }

    public ArrayList<Player> getPlayersInResidence() {
        ArrayList<Player> within = new ArrayList<Player>();
        Player[] players = Residence.getInstance().getServer().getOnlinePlayers();
        for (Player player : players) {
            if (containsLoc(player.getLocation())) {
                within.add(player);
            }
        }
        return within;
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("EnterMessage", enterMessage);
        root.put("LeaveMessage", leaveMessage);
        root.put("Areas", super.save());
        Map<String, Object> subzonemap = new HashMap<String, Object>();
        for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
            subzonemap.put(sz.getKey(), sz.getValue().save());
        }
        root.put("Subzones", subzonemap);
        root.put("Permissions", perms.save());
        if (tpLoc != null) {
            Map<String, Object> tpmap = new HashMap<String, Object>();
            tpmap.put("X", tpLoc.getBlockX());
            tpmap.put("Y", tpLoc.getBlockY());
            tpmap.put("Z", tpLoc.getBlockZ());
            root.put("TPLoc", tpmap);
        }
        return root;
    }

    public static ClaimedResidence load(Map<String, Object> root, ClaimedResidence parent) throws Exception {
        if (root == null) {
            throw new Exception("Null residence!");
        }
        ClaimedResidence res = new ClaimedResidence();
        res.enterMessage = (String) root.get("EnterMessage");
        res.leaveMessage = (String) root.get("LeaveMessage");
        res.load((Map<String, Object>) root.get("Area"));
        res.perms = ResidencePermissions.load(res, (Map<String, Object>) root.get("Permissions"));

        Map<String, Object> subzonemap = (Map<String, Object>) root.get("Subzones");
        for (Entry<String, Object> map : subzonemap.entrySet()) {
            ClaimedResidence subres = ClaimedResidence.load((Map<String, Object>) map.getValue(), res);
            subres.getPermissions().setParent(res.getPermissions());
            res.subzones.put(map.getKey(), subres);
        }
        res.parent = parent;
        Map<String, Object> tploc = (Map<String, Object>) root.get("TPLoc");
        if (tploc != null) {
            res.tpLoc = new Location(res.getWorld(), (Integer) tploc.get("X"), (Integer) tploc.get("Y"), (Integer) tploc.get("Z"));
        }
        return res;
    }
}

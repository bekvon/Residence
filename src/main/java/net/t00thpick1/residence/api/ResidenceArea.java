package net.t00thpick1.residence.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public interface ResidenceArea extends CuboidArea, PermissionsArea, RentableArea, BuyableArea {
    public String getOwner();
    public void setOwner(String owner);
    public void setPlayerFlag(String player, Flag flag, Boolean value);
    public ResidenceArea getSubzoneByLoc(Location loc);
    public ResidenceArea getSubzoneByName(String string);
    public ResidenceArea getParent();
    public List<Player> getPlayersInResidence();
    public String getName();
    public boolean removeSubzone(String name);
    public Location getOutsideFreeLoc(Location location);
    public String getEnterMessage();
    public String getLeaveMessage();
    public void rentLink(ResidenceArea link);
    public boolean rename(String newName);
    public void copyPermissions(ResidenceArea mirror);
    public void clearFlags();
    public void applyDefaultFlags();
    public ResidenceArea getTopParent();
    public String getFullName();
    public boolean renameSubzone(String name, String newName);
    public boolean setTeleportLocation(Location location);
    public Location getTeleportLocation();
    public boolean createSubzone(String zname, String name, Location playerLoc1, Location playerLoc2);
    public Collection<ResidenceArea> getSubzoneList();
    public void removeAllPlayerFlags(String string);
    public void setLeaveMessage(String string);
    public void setEnterMessage(String string);
    public Collection<String> getSubzoneNameList();
    public void setRentFlag(Flag flag, Boolean value);
    public Map<Flag, Boolean> getRentFlags();
    public Map<String, Map<Flag, Boolean>> getPlayerFlags();
    public Map<Flag, Boolean> getPlayerFlags(String name);
    public Map<Flag, Boolean> getAreaFlags();
    public Map<String, Map<Flag, Boolean>> getGroupFlags();
    public Collection<String> getRentLinkStrings();
}

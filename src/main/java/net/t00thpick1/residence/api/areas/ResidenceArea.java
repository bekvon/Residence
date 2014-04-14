package net.t00thpick1.residence.api.areas;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.t00thpick1.residence.api.flags.Flag;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * An object representation of a Residence area.
 *
 * Note: ResidenceArea does not have any reference equivalence contract. Any given area may
 * have multiple instances of a ResidenceArea object.  Equivalence should be determined using
 * {@link #equals(Object)}
 *
 * @author t00thpick1
 *
 */
public interface ResidenceArea extends CuboidArea, PermissionsArea, RentableArea, BuyableArea {
    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public String getOwner();
    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public void setOwner(String owner);

    public List<Player> getPlayersInResidence();
    public Location getOutsideFreeLoc(Location location);

    public String getEnterMessage();
    public void setEnterMessage(String string);
    public String getLeaveMessage();
    public void setLeaveMessage(String string);
    public Location getTeleportLocation();
    public boolean setTeleportLocation(Location location);

    public void applyDefaultFlags();
    public void copyFlags(ResidenceArea mirror);

    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public Map<Flag, Boolean> getPlayerFlags(String player);
    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public Map<String, Map<Flag, Boolean>> getPlayerFlags();
    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public void removeAllPlayerFlags(String player);
    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public void setPlayerFlag(String player, Flag flag, Boolean value);

    public void rentLink(ResidenceArea link);
    public Collection<ResidenceArea> getRentLinks();
    public void setRentFlag(Flag flag, Boolean value);
    public Map<Flag, Boolean> getRentFlags();

    public Collection<ResidenceArea> getSubzoneList();
    public ResidenceArea getSubzoneByLocation(Location loc);
    public ResidenceArea getSubzoneByName(String string);
    /**
    * @Deprecated Will be using a UUID system soonish
    */
    @Deprecated
    public boolean createSubzone(String name, String owner, CuboidArea area);
    public boolean removeSubzone(String name);
    public boolean removeSubzone(ResidenceArea subzone);
    public boolean renameSubzone(String name, String newName);
    public int getSubzoneDepth();
    public ResidenceArea getParent();

    /**
     * Gets the topmost parent of the ResidenceArea.  This function will never return
     * a ResidenceArea in which {@link #getParent()} returns anything other than null.
     * If the ResidenceArea is not a subzone, it returns itself.
     *
     * @return the topmost ResidenceArea within the subzone chain
     */
    public ResidenceArea getTopParent();

    /**
     * Returns the friendly name of this ResidenceArea.  If it is a subzone it will be the name
     * of the subzone itself.
     *
     * @return the name of the ResidenceArea
     */
    public String getName();

    /**
     * Attempts to rename the ResidenceArea to the given name.  If successful returns true,
     * otherwise false.
     *
     * @param newName the name to attempt to change to
     * @return whether or not the renaming was successful
     */
    public boolean rename(String newName);
    public String getFullName();
    public UUID getResidenceUUID();
    public long getCreationDate();

    /**
     * Checks equivalence with another ResidenceArea.
     *
     * @param object, a ResidenceArea to compare to
     * @return whether or not the given ResidenceArea is equivalent to this ResidenceArea.
     */
    public boolean equals(Object object);
}

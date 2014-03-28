package net.t00thpick1.residence.api.areas;

import java.util.Map;

import net.t00thpick1.residence.api.flags.Flag;

import org.bukkit.World;

/**
 * An object representation of a permission area.
 * Examples include WorldArea's and ResidenceArea's
 *
 * Note: PermissionsArea does not have any reference equivalence contract. Any given area may
 * have multiple instances of a PermissionsArea object.  Equivalence should be determined using
 * {@link #equals(Object)}
 *
 * @author t00thpick1
 *
 */
public interface PermissionsArea {

    /**
     * Determines whether or not a flag is allowed within the area.
     *
     * @param flag the flag to check
     * @return whether or not it is allowed
     */
    public boolean allowAction(Flag flag);

    /**
     * Determines whether or not a player has permission to a flag with the area.
     *
     * @param player the player to check
     * @param flag the flag to check
     * @return whether or not the player has permission in the area
     */
    public boolean allowAction(String player, Flag flag);

    /**
     * Gets the world that the PermissionArea is in.
     *
     * @return the world
     */
    public World getWorld();

    /**
     * Sets an area flag in an area.  A null value will remove the flag.
     *
     * @param flag the flag to set
     * @param value the value to set it to, or null to remove
     */
    public void setAreaFlag(Flag flag, Boolean value);

    /**
     * Gets a map of area flags.  The map contains Flag to value.
     *
     * Note: Map is immutable
     *
     * @return the area flag map.
     */
    public Map<Flag, Boolean> getAreaFlags();

    /**
     * Clears all area flags in the PermissionsArea.
     *
     */
    public void removeAllAreaFlags();

    /**
     * Sets an group flag in an area for a group.  A null value will remove the flag.
     *
     * @param group the group to set the flag for
     * @param flag the flag to set
     * @param value the value to set it to, or null to remove
     */
    public void setGroupFlag(String group, Flag flag, Boolean value);

    /**
     * Gets a map of group flags.  The outer map contains group names to inner map.
     * the inner map contains Flag to value.
     *
     * Note: Map is immutable
     *
     * @return the group flag maps.
     */
    public Map<String, Map<Flag, Boolean>> getGroupFlags();

    /**
     * Removes all flags for a group in the PermissionsArea.
     *
     * @param group the group to remove flags for
     */
    public void removeAllGroupFlags(String group);

    /**
     * Clears all group flags in the PermissionsArea.
     *
     */
    public void removeAllGroupFlags();

    /**
     * Clears all flags within the PermissionsArea.
     *
     */
    public void clearFlags();

    /**
     * Checks equivalence with another PermissionsArea.
     *
     * @param object, a PermissionsArea to compare to.
     * @return whether or not the given PermissionsArea is equivalent to this PermissionsArea.
     */
    public boolean equals(Object object);
}

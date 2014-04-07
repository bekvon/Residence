package net.t00thpick1.residence.api.flags;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * A representation of a Flag.
 *
 * @author t00thpick1
 */
public final class Flag {
    private final String name;
    private final Flag parent;
    private final FlagType type;
    private final Permission perm;
    private final String description;

    /**
     * Constructs a new Flag.
     * <p>
     * This process includes creating its permission node and handling permission
     * inheritance.
     *
     * @param flag the unique name of the flag, case insensitive
     * @param type the type of the flag
     * @param parent the parent flag
     * @param description the display description
     */
    public Flag(String flag, FlagType type, Flag parent, String description) {
        this.name = flag.toLowerCase();
        this.type = type;
        this.parent = parent;
        this.perm = new org.bukkit.permissions.Permission("residence.flags." + name, PermissionDefault.TRUE);
        if (parent != null) {
            this.perm.addParent(parent.getPermission(), true);
        } else {
            this.perm.addParent("residence.flags.all", true);
        }
        this.description = description;
    }

    /**
     * Gets the name of this Flag.  This will be unique for all registered Flags.
     *
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the type of Flag this is.
     *
     * @return the flag type
     */
    public final FlagType getType() {
        return type;
    }

    /**
     * Gets the parent Flag of this Flag, or returns null if none.
     *
     * @return the parent Flag
     */
    public final Flag getParent() {
        return parent;
    }

    /**
     * The type of flag this is.
     */
    public enum FlagType {
        /**
         * A Flag of type PLAYER_ONLY can only be applied to a player.
         */
        PLAYER_ONLY,
        /**
         * A Flag of type AREA_ONLY can be applied only to an area.
         */
        AREA_ONLY,
        /**
         * A Flag of type ANY can be applied to either a player or an area.
         */
        ANY,
        /**
         * For internal use only.
         * This is for flags that are on a PermissionsArea but not registered in the FlagManager.
         * Allowing us to preserve the flag data even though there is no valid flag for it.
         * Note: Flags of this type cannot be assigned to an area.
         */
        DUMMY;
    }

    /**
     * Gets the Permission for this Flag, which is generated at instantiation.
     *
     * @return the flag's permission
     */
    public final Permission getPermission() {
        return perm;
    }

    /**
     * Gets the display description of this Flag.
     *
     * @return the flag's description
     */
    public final String getDescription() {
        return description;
    }
}

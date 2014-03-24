package net.t00thpick1.residence.api;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public abstract class Flag {
    private final String name;
    private final Flag parent;
    private final FlagType type;
    private final Permission perm;

    public Flag(String flag, FlagType type, Flag parent) {
        this.name = flag.toLowerCase();
        this.type = type;
        this.parent = parent;
        this.perm = new org.bukkit.permissions.Permission("residence.flags." + name, PermissionDefault.TRUE);
        if (parent != null) {
            perm.addParent(parent.getPermission(), true);
        } else {
            perm.addParent("residence.flags.all", true);
        }
    }

    public final String getName() {
        return name;
    }

    public final FlagType getType() {
        return type;
    }

    public final Flag getParent() {
        return parent;
    }

    public enum FlagType {
        PLAYER_ONLY,
        AREA_ONLY,
        ANY;
    }

    public Permission getPermission() {
        // TODO Auto-generated method stub
        return null;
    }
}

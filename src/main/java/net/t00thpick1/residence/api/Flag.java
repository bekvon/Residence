package net.t00thpick1.residence.api;

public abstract class Flag {
    private final String name;
    private final Flag parent;
    private final FlagType type;

    public Flag(String flag, FlagType type, Flag parent) {
        this.name = flag.toLowerCase();
        this.type = type;
        this.parent = parent;
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
}

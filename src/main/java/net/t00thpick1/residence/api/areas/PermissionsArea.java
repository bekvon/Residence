package net.t00thpick1.residence.api.areas;

import net.t00thpick1.residence.api.flags.Flag;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface PermissionsArea {
    public boolean allowAction(Flag flag);
    public boolean allowAction(Player player, Flag flag);
    public World getWorld();
    public void setFlag(Flag flag, Boolean value);
    public void setGroupFlag(String group, Flag flag, Boolean value);
}

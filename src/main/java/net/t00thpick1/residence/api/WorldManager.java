package net.t00thpick1.residence.api;

import net.t00thpick1.residence.api.areas.WorldArea;

import org.bukkit.World;

public interface WorldManager {

    public WorldArea getResidenceWorld(World world);

}

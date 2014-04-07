package net.t00thpick1.residence.api;

import java.io.IOException;
import java.util.Collection;

import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * This API class allows for the getting, creation, and removal of ResidenceAreas.
 *
 * @author t00thpick1
 */
public interface ResidenceManager {

    /**
     * Gets a ResidenceArea by location.
     *
     * @param location the location to check
     * @return a ResidenceArea at that location, or null if none
     */
    public ResidenceArea getByLocation(Location location);

    /**
     * Gets a ResidenceArea by it's fully qualified name.
     *
     * @param name the fully qualified name of the ResidenceArea
     * @return the ResidenceArea by that name, or null
     */
    public ResidenceArea getByName(String name);

    public Collection<ResidenceArea> getOwnedResidences(String player);

    public int getOwnedZoneCount(String player);

    /**
     * Gets all ResidenceArea's within the provided world.
     *
     * @param world the world to get the ResidenceAreas from
     * @return an immutable collection of ResidenceAreas from the supplied world
     */
    public Collection<ResidenceArea> getResidencesInWorld(World world);

    /**
     * Attempts to create a new ResidenceArea with the given name, owner, and area.
     *
     * @param residenceName the name to create a ResidenceArea with
     * @param owner the person to own the ResidenceArea (null for server land)
     * @param area the area for the ResidenceArea to encompass
     * @return the created ResidenceArea or null if not successful
     */
    public ResidenceArea createResidence(String residenceName, String owner, CuboidArea area);

    /**
     * Removes the ResidenceArea from the world.
     *
     * @param res the ResidenceArea to remove
     */
    public void remove(ResidenceArea res);

    /**
     * This method is really implementation specific, depending on the BackEnd, this can have different
     * behavoir.  Generally it will save all valid ResidenceArea's currently in memory.
     * @throws IOException
     */
    public void save() throws IOException;
}

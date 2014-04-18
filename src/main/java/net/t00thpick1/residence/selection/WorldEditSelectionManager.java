package net.t00thpick1.residence.selection;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldEditSelectionManager extends SelectionManager {
    private WorldEditPlugin wep;

    public WorldEditSelectionManager(Plugin plugin) {
        super();
        wep = (WorldEditPlugin) plugin;
    }

    @Override
    public boolean worldEdit(Player player) {
        Selection sel = wep.getSelection(player);
        if (sel != null) {
            Location pos1 = sel.getMinimumPoint();
            Location pos2 = sel.getMaximumPoint();
            try {
                CuboidRegion region = (CuboidRegion) sel.getRegionSelector().getRegion();
                pos1 = new Location(player.getWorld(), region.getPos1().getX(), region.getPos1().getY(), region.getPos1().getZ());
                pos2 = new Location(player.getWorld(), region.getPos2().getX(), region.getPos2().getY(), region.getPos2().getZ());
            } catch (Exception e) {
            }
            this.playerLoc1.put(player.getName(), pos1);
            this.playerLoc2.put(player.getName(), pos2);
            return true;
        }
        return false;
    }
}

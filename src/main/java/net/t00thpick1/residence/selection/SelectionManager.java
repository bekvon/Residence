package net.t00thpick1.residence.selection;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.CuboidArea;
import net.t00thpick1.residence.protection.GroupManager;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SelectionManager {
    protected Map<String, Location> playerLoc1;
    protected Map<String, Location> playerLoc2;

    public static final int MAX_HEIGHT = 255, MIN_HEIGHT = 0;

    public enum Direction {
        PLUSY, PLUSX, PLUSZ, MINUSY, MINUSX, MINUSZ
    }

    public SelectionManager() {
        playerLoc1 = new HashMap<String, Location>();
        playerLoc2 = new HashMap<String, Location>();
    }

    public void placeLoc1(Player player, Location loc) {
        if (loc != null) {
            playerLoc1.put(player.getName(), loc);
        }
    }

    public void placeLoc2(Player player, Location loc) {
        if (loc != null) {
            playerLoc2.put(player.getName(), loc);
        }
    }

    public Location getPlayerLoc1(Player player) {
        return playerLoc1.get(player.getName());
    }

    public Location getPlayerLoc2(Player player) {
        return playerLoc2.get(player.getName());
    }

    public boolean hasPlacedBoth(Player player) {
        return (playerLoc1.containsKey(player.getName()) && playerLoc2.containsKey(player.getName()));
    }

    public void showSelectionInfo(Player player) {
        if (this.hasPlacedBoth(player)) {
            CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(player), getPlayerLoc2(player));
            player.sendMessage(LocaleLoader.getString("Selection.Total.Size", cuboidArea.getSize()));
            if (ConfigManager.getInstance().isEconomy()) {
                player.sendMessage(LocaleLoader.getString("Land.Cost", (int) Math.ceil((double) cuboidArea.getSize() * GroupManager.getCostPerBlock(player))));
            }
            player.sendMessage(LocaleLoader.getString("Selection.Size.X", cuboidArea.getXSize()));
            player.sendMessage(LocaleLoader.getString("Selection.Size.Y", cuboidArea.getYSize()));
            player.sendMessage(LocaleLoader.getString("Selection.Size.Z", cuboidArea.getZSize()));
        } else
            player.sendMessage(LocaleLoader.getString("SelectPoints"));
    }

    public void vert(Player player, boolean resadmin) {
        if (hasPlacedBoth(player)) {
            this.sky(player, resadmin);
            this.bedrock(player, resadmin);
        } else {
            player.sendMessage(LocaleLoader.getString("SelectPoints"));
        }
    }

    public void sky(Player player, boolean resadmin) {
        if (hasPlacedBoth(player)) {
            int y1 = playerLoc1.get(player.getName()).getBlockY();
            int y2 = playerLoc2.get(player.getName()).getBlockY();
            if (y1 > y2) {
                playerLoc1.get(player.getName()).setY(MAX_HEIGHT);
            } else {
                playerLoc2.get(player.getName()).setY(MAX_HEIGHT);
            }
            player.sendMessage(LocaleLoader.getString("SelectionSky"));
        } else {
            player.sendMessage(LocaleLoader.getString("SelectPoints"));
        }
    }

    public void bedrock(Player player, boolean resadmin) {
        if (hasPlacedBoth(player)) {
            int y1 = playerLoc1.get(player.getName()).getBlockY();
            int y2 = playerLoc2.get(player.getName()).getBlockY();
            if (y1 < y2) {
                playerLoc1.get(player.getName()).setY(MIN_HEIGHT);
            } else {
                playerLoc2.get(player.getName()).setY(MIN_HEIGHT);
            }
            player.sendMessage(LocaleLoader.getString("SelectionBedrock"));
        } else {
            player.sendMessage(LocaleLoader.getString("SelectPoints"));
        }
    }

    public void clearSelection(Player player) {
        playerLoc1.remove(player.getName());
        playerLoc2.remove(player.getName());
    }

    public void selectChunk(Player player) {
        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
        int xcoord = chunk.getX() * 16;
        int zcoord = chunk.getZ() * 16;
        int ycoord = MIN_HEIGHT;
        int xmax = xcoord + 15;
        int zmax = zcoord + 15;
        int ymax = MAX_HEIGHT;
        this.playerLoc1.put(player.getName(), new Location(player.getWorld(), xcoord, ycoord, zcoord));
        this.playerLoc2.put(player.getName(), new Location(player.getWorld(), xmax, ymax, zmax));
        player.sendMessage(LocaleLoader.getString("SelectionSuccess"));
    }

    public boolean worldEdit(Player player) {
        player.sendMessage(LocaleLoader.getString("WorldEditNotFound"));
        return false;
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
        Location myloc = player.getLocation();
        Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
        Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);
        placeLoc1(player, loc1);
        placeLoc2(player, loc2);
        player.sendMessage(LocaleLoader.getString("SelectionSuccess"));
        showSelectionInfo(player);
    }

    public void modify(Player player, boolean shift, int amount) {
        if (!this.hasPlacedBoth(player)) {
            player.sendMessage(LocaleLoader.getString("SelectPoints"));
            return;
        }
        Direction d = this.getDirection(player);
        if (d == null) {
            player.sendMessage(LocaleLoader.getString("InvalidDirection"));
        }
        Location loc1 = playerLoc1.get(player.getName());
        Location loc2 = playerLoc2.get(player.getName());
        Location highLoc;
        Location lowLoc;
        if (loc1.getBlockY() > loc2.getBlockY()) {
            highLoc = loc1;
            lowLoc = loc2;
        } else {
            highLoc = loc2;
            lowLoc = loc1;
        }
        if (d == Direction.PLUSY) {
            if (highLoc.getBlockY() + amount > MAX_HEIGHT) {
                player.sendMessage(LocaleLoader.getString("Select.TooHigh"));
                return;
            }
            highLoc.add(0, amount, 0);
            if (shift) {
                lowLoc.add(0, amount, 0);
                player.sendMessage(LocaleLoader.getString("Shifting.Y.Positive"));
            } else
                player.sendMessage(LocaleLoader.getString("Expanding.Y.Positive"));
        }
        if (d == Direction.MINUSY) {
            if (lowLoc.getBlockY() - amount < MIN_HEIGHT) {
                player.sendMessage(LocaleLoader.getString("Select.TooLow"));
                return;
            }
            lowLoc.add(0, -amount, 0);
            if (shift) {
                highLoc.add(0, -amount, 0);
                player.sendMessage(LocaleLoader.getString("Shifting.Y.Negative"));
            } else
                player.sendMessage(LocaleLoader.getString("Expanding.Y.Negative"));
        }
        if (d == Direction.MINUSX) {
            lowLoc.add(-amount, 0, 0);
            if (shift) {
                highLoc.add(-amount, 0, 0);
                player.sendMessage(LocaleLoader.getString("Shifting.X.Negative"));
            } else
                player.sendMessage(LocaleLoader.getString("Expanding.X.Negative"));
        }
        if (d == Direction.PLUSX) {
            highLoc.add(amount, 0, 0);
            if (shift) {
                lowLoc.add(amount, 0, 0);
                player.sendMessage(LocaleLoader.getString("Shifting.X.Positive"));
            } else
                player.sendMessage(LocaleLoader.getString("Expanding.X.Positive"));
        }
        if (d == Direction.MINUSZ) {
            lowLoc.add(0, 0, -amount);
            if (shift) {
                highLoc.add(0, 0, -amount);
                player.sendMessage(LocaleLoader.getString("Shifting.Z.Negative"));
            } else
                player.sendMessage(LocaleLoader.getString("Expanding.Z.Negative"));
        }
        if (d == Direction.PLUSZ) {
            highLoc.add(0, 0, amount);
            if (shift) {
                lowLoc.add(0, 0, amount);
                player.sendMessage(LocaleLoader.getString("Shifting.Z.Positive"));
            } else
                player.sendMessage(LocaleLoader.getString("Expanding.Z.Postive"));
        }
    }

    private Direction getDirection(Player player) {
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();
        if (pitch < -50)
            return Direction.PLUSY;
        if (pitch > 50)
            return Direction.MINUSY;
        if ((yaw > 45 && yaw < 135) || (yaw < -45 && yaw > -135))
            return Direction.PLUSX;
        if ((yaw > 225 && yaw < 315) || (yaw < -225 && yaw > -315))
            return Direction.MINUSX;
        if ((yaw > 135 && yaw < 225) || (yaw < -135 && yaw > -225))
            return Direction.MINUSZ;
        if ((yaw < 45 || yaw > 315) || (yaw > -45 || yaw < -315))
            return Direction.PLUSZ;
        return null;
    }

}

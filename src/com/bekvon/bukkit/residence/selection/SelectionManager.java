/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class SelectionManager {
    private Map<String,Location> playerLoc1;
    private Map<String,Location> playerLoc2;

    public enum Direction
    {
        UP,DOWN,PLUSX,PLUSZ,MINUSX,MINUSZ
    }

    public SelectionManager()
    {
        playerLoc1 = Collections.synchronizedMap(new HashMap<String,Location>());
        playerLoc2 = Collections.synchronizedMap(new HashMap<String,Location>());
    }

    public void placeLoc1(String player, Location loc)
    {
        if(loc!=null)
            playerLoc1.put(player, loc);
    }

    public void placeLoc2(String player, Location loc)
    {
        if(loc!=null)
            playerLoc2.put(player, loc);
    }

    public Location getPlayerLoc1(String player)
    {
        return playerLoc1.get(player);
    }

    public Location getPlayerLoc2(String player)
    {
        return playerLoc2.get(player);
    }

    public boolean hasPlacedBoth(String player)
    {
        return (playerLoc1.containsKey(player) && playerLoc2.containsKey(player));
    }

    public void showSelectionInfo(Player player) {
        String pname = player.getName();
        if (this.hasPlacedBoth(pname)) {
            CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("Selection.Total.Size")+":§3 " + cuboidArea.getSize());
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if(Residence.getConfigManager().enableEconomy())
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("Land.Cost")+":§3 " + ((int)Math.ceil((double)cuboidArea.getSize()*group.getCostPerBlock())));
            player.sendMessage("§eX"+Residence.getLanguage().getPhrase("Size")+":§3 " + cuboidArea.getXSize());
            player.sendMessage("§eY"+Residence.getLanguage().getPhrase("Size")+":§3 " + cuboidArea.getYSize());
            player.sendMessage("§eZ"+Residence.getLanguage().getPhrase("Size")+":§3 " + cuboidArea.getZSize());
        }
        else
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("SelectPoints"));
    }

    public void vert(Player player, boolean resadmin)
    {
        if(hasPlacedBoth(player.getName()))
        {
            this.sky(player, resadmin);
            this.bedrock(player, resadmin);
        }
        else
        {
            player.sendMessage("§c" + Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void sky(Player player, boolean resadmin)
    {
        if(hasPlacedBoth(player.getName()))
        {
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            int y1 = playerLoc1.get(player.getName()).getBlockY();
            int y2 = playerLoc2.get(player.getName()).getBlockY();
            if(y1>y2)
            {
                int newy = 127;
                if(!resadmin)
                {
                    if(group.getMaxHeight()<newy)
                        newy = group.getMaxHeight();
                    if(newy - y2 > (group.getMaxY()-1))
                        newy = y2 + (group.getMaxY()-1);
                }
                playerLoc1.get(player.getName()).setY(newy);
            }
            else
            {
                int newy = 127;
                if(!resadmin)
                {
                    if(group.getMaxHeight()<newy)
                        newy = group.getMaxHeight();
                    if(newy - y1 > (group.getMaxY()-1))
                        newy = y1 + (group.getMaxY()-1);
                }
                playerLoc2.get(player.getName()).setY(newy);
            }
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("SelectionSky"));
        }
        else
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void bedrock(Player player, boolean resadmin)
    {
        if(hasPlacedBoth(player.getName()))
        {
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            int y1 = playerLoc1.get(player.getName()).getBlockY();
            int y2 = playerLoc2.get(player.getName()).getBlockY();
            if(y1<y2)
            {
                int newy = 0;
                if(!resadmin)
                {
                    if(newy<group.getMinHeight())
                        newy = group.getMinHeight();
                    if(y2 - newy > (group.getMaxY()-1))
                        newy = y2 - (group.getMaxY()-1);
                }
                playerLoc1.get(player.getName()).setY(newy);
            }
            else
            {
                int newy = 0;
                if(!resadmin)
                {
                    if(newy<group.getMinHeight())
                        newy = group.getMinHeight();
                    if(y1 - newy > (group.getMaxY()-1))
                        newy = y1 - (group.getMaxY()-1);
                }
                playerLoc2.get(player.getName()).setY(newy);
            }
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("SelectionBedrock"));
        }
        else
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void clearSelection(Player player)
    {
        playerLoc1.remove(player.getName());
        playerLoc2.remove(player.getName());
    }

    public void selectChunk(Player player)
    {
        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
        int xcoord = chunk.getX() * 16;
        int zcoord = chunk.getZ() * 16;
        int ycoord = 0;
        int xmax = xcoord + 15;
        int zmax = zcoord + 15;
        int ymax = 127;
        this.playerLoc1.put(player.getName(), new Location(player.getWorld(), xcoord, ycoord, zcoord));
        this.playerLoc2.put(player.getName(), new Location(player.getWorld(), xmax,ymax,zmax));
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("SelectionSuccess"));
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
        Location myloc = player.getLocation();
        Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
        Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);
        placeLoc1(player.getName(), loc1);
        placeLoc2(player.getName(), loc2);
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("SelectionSuccess"));
        showSelectionInfo(player);
    }

    public void modify(Player player, boolean shift, int amount)
    {
        if(!this.hasPlacedBoth(player.getName()))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("SelectPoints"));
            return;
        }
        Direction d = this.getDirection(player);
        if(d==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidDirection"));
        }
        CuboidArea area = new CuboidArea (playerLoc1.get(player.getName()),playerLoc2.get(player.getName()));
        if(d == Direction.UP)
        {
            int oldy = area.getHighLoc().getBlockY();
            oldy = oldy + amount;
            if(oldy>127)
            {
                player.sendMessage("§cError, attempted to go beyond the top of the map.");
            }
            area.getHighLoc().setY(oldy);
            if(shift)
            {
                int oldy2 = area.getLowLoc().getBlockY();
                oldy2 = oldy2 + amount;
                area.getLowLoc().setY(oldy2);
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Shifting.Up")+"...");
            }
            else
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Expanding.Up")+"...");
        }
        if(d == Direction.DOWN)
        {
            int oldy = area.getLowLoc().getBlockY();
            oldy = oldy - amount;
            if(oldy<0)
            {
                player.sendMessage("§cError, attempted to go beyond bottem of the map.");
                return;
            }
            area.getLowLoc().setY(oldy);
            if(shift)
            {
                int oldy2 = area.getHighLoc().getBlockY();
                oldy2 = oldy2 - amount;
                area.getHighLoc().setY(oldy2);
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Shifting.Down")+"...");
            }
            else
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Expanding.Down")+"...");
        }
        if(d == Direction.MINUSX)
        {
            int oldx = area.getLowLoc().getBlockX();
            oldx = oldx - amount;
            area.getLowLoc().setX(oldx);
            if(shift)
            {
                int oldx2 = area.getHighLoc().getBlockX();
                oldx2 = oldx2 - amount;
                area.getHighLoc().setX(oldx2);
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Shifting")+" -X...");
            }
            else
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Expanding")+" -X...");
        }
        if(d == Direction.PLUSX)
        {
            int oldx = area.getHighLoc().getBlockX();
            oldx = oldx + amount;
            area.getHighLoc().setX(oldx);
            if(shift)
            {
                int oldx2 = area.getLowLoc().getBlockX();
                oldx2 = oldx2 + amount;
                area.getLowLoc().setX(oldx2);
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Shifting")+" +X...");
            }
            else
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Expanding")+" +X...");
        }
        if(d == Direction.MINUSZ)
        {
            int oldz = area.getLowLoc().getBlockZ();
            oldz = oldz - amount;
            area.getLowLoc().setZ(oldz);
            if(shift)
            {
                int oldz2 = area.getHighLoc().getBlockZ();
                oldz2 = oldz2 - amount;
                area.getHighLoc().setZ(oldz2);
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Shifting")+" -Z...");
            }
            else
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Expanding")+" -Z...");
        }
        if(d == Direction.PLUSZ)
        {
            int oldz = area.getHighLoc().getBlockZ();
            oldz = oldz + amount;
            area.getHighLoc().setZ(oldz);
            if(shift)
            {
                int oldz2 = area.getLowLoc().getBlockZ();
                oldz2 = oldz2 + amount;
                area.getLowLoc().setZ(oldz2);
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Shifting")+" +Z...");
            }
            else
                player.sendMessage("§e"+Residence.getLanguage().getPhrase("Expanding")+" +Z...");
        }
        playerLoc1.put(player.getName(), area.getHighLoc());
        playerLoc2.put(player.getName(), area.getLowLoc());
    }

    private Direction getDirection(Player player)
    {
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();
        if(pitch<-50)
            return Direction.UP;
        if(pitch > 50)
            return Direction.DOWN;
        if((yaw>45 && yaw<135) || (yaw<-45 && yaw>-135))
            return Direction.PLUSX;
        if((yaw>225 && yaw<315) ||  (yaw<-225 && yaw>-315))
            return Direction.MINUSX;
        if((yaw>135 && yaw<225) || (yaw<-135 && yaw>-225))
            return Direction.MINUSZ;
        if((yaw<45 || yaw>315) || (yaw>-45 && yaw<-315))
            return Direction.PLUSZ;
        return null;
    }

}

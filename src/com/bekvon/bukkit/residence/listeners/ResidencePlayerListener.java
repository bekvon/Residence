/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Administrator
 */
public class ResidencePlayerListener extends PlayerListener {

    protected Map<String,String> cache;
    protected Map<String,Long> lastUpdate;
    protected Map<String,Location> lastOutsideLoc;
    protected int minUpdateTime;
    
    public ResidencePlayerListener(int updateTime)
    {
        cache = new HashMap<String,String>();
        lastUpdate = new HashMap<String,Long>();
        lastOutsideLoc = new HashMap<String,Location>();
        minUpdateTime = updateTime;
    }


    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        cache.remove(pname);
        lastUpdate.remove(pname);
        lastOutsideLoc.remove(pname);
        super.onPlayerQuit(event);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Material mat = block.getType();
        if(!Residence.getPermissionManager().isResidenceAdmin(player))
        {
            if(mat == Material.CHEST || mat == Material.FURNACE || mat == Material.BURNING_FURNACE || mat == Material.DISPENSER)
            {
                ClaimedResidence res = Residence.getResidenceManger().getByLoc(block.getLocation());
                if(res!=null)
                {
                    if(!res.getPermissions().has("container", true))
                    {
                        event.setCancelled(true);
                        player.sendMessage("§cYou dont have container access for this Residence.");
                    }
                }
            }
            else if(mat == Material.BED || mat == Material.LEVER || mat == Material.STONE_BUTTON || mat == Material.WOODEN_DOOR || mat == Material.WORKBENCH)
            {
                ClaimedResidence res = Residence.getResidenceManger().getByLoc(block.getLocation());
                if(res!=null)
                {
                    if(!res.getPermissions().has("use", true))
                    {
                        event.setCancelled(true);
                        player.sendMessage("§cYou dont have use access for this Residence.");
                    }
                }
                else
                {
                    if(!Residence.getConfig().worldUseEnabled())
                    {
                        event.setCancelled(true);
                        player.sendMessage("§cWorld use is disabled.");
                    }
                }
            }
        }
        super.onPlayerInteract(event);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String pname = player.getName();
        long lastCheck = 0;
        if (lastUpdate.containsKey(pname)) {
            lastCheck = lastUpdate.get(pname);
        }
        long now = System.currentTimeMillis();
        if (now - lastCheck > minUpdateTime) {
            ResidenceManager manager = Residence.getResidenceManger();
            ClaimedResidence res = null;
            Location ploc = event.getTo();
            boolean showenter = false;
            String areaname = cache.get(pname);
            if (areaname != null) {
                res = manager.getByName(areaname);
                if (res == null) {
                    cache.remove(pname);
                    areaname = null;
                } else {
                    if (!res.containsLoc(ploc)) {
                        String leave = res.getLeaveMessage();
                        if (leave != null && !leave.equals("")) {
                            player.sendMessage("§e" + this.insertMessages(player, areaname, res, leave));
                        }
                        res = res.getParent();
                        while (res != null && !res.containsLoc(ploc)) {
                            res = res.getParent();
                        }
                        if (res == null) {
                            cache.remove(pname);
                            areaname = null;
                        } else {
                            areaname = Residence.getResidenceManger().getNameByLoc(ploc);
                            cache.put(pname, areaname);
                        }
                    } else {
                        String subzone = res.getSubzoneNameByLoc(ploc);
                        if (subzone != null) {
                            areaname = areaname + "." + subzone;
                            cache.put(pname, areaname);
                            res = res.getSubzone(subzone);
                            showenter = true;
                        }
                    }
                }
            }
            if(areaname == null)
            {
                areaname = manager.getNameByLoc(ploc);
                showenter = true;
            }
            if (areaname != null) {
                res = manager.getByName(areaname);
                if (res.getPermissions().playerHas(pname, "move", true) || Residence.getPermissionManager().isResidenceAdmin(player)) {
                    cache.put(pname, areaname);
                    String enter = res.getEnterMessage();
                    if (enter != null && showenter) {
                        player.sendMessage("§e" + this.insertMessages(player, areaname, res, enter));
                    }
                } else {
                    event.setCancelled(true);
                    Location lastLoc = lastOutsideLoc.get(pname);
                    if (lastLoc != null) {
                        player.teleport(lastLoc);
                    } else {
                        player.teleport(res.getOutsideFreeLoc(event.getTo()));
                    }
                    player.sendMessage("§cYou dont have permission to move in residence: " + areaname);
                }
            }
            else
            {
                lastOutsideLoc.put(pname, ploc);
            }
            lastUpdate.put(pname, System.currentTimeMillis());
        }
        super.onPlayerMove(event);
    }

    public String insertMessages(Player player, String areaname, ClaimedResidence res, String message) {
        message = message.replaceAll("%player", player.getName());
        message = message.replaceAll("%owner", res.getPermissions().getOwner());
        message = message.replaceAll("%residence", areaname);
        return message;
    }

}

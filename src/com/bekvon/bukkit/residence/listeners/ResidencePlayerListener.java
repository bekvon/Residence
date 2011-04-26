/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;

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
        //super.onPlayerQuit(event); // This just causes a nag to appear, the parent has no actions
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if (player.getItemInHand().getTypeId() == Residence.getConfig().getSelectionTooldID()) {
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                if(group.getMaxSubzoneDepth() > 0 || group.canCreateResidences() || Residence.getPermissionManager().isResidenceAdmin(player))
                {
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc1(event.getPlayer().getName(), loc);
                        player.sendMessage("§aPlaced Primary Selection Point §c(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")§a!");
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc2(player.getName(), loc);
                        player.sendMessage("§aPlaced Secondary Selection Point §c(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")§a!");
                    }
                }
            }
            if(player.getItemInHand().getTypeId() == Residence.getConfig().getInfoToolID())
            {
                if(event.getAction() == Action.LEFT_CLICK_BLOCK)
                {
                    Location loc = block.getLocation();
                    String res = Residence.getResidenceManger().getNameByLoc(loc);
                    if(res!=null)
                        Residence.getResidenceManger().printAreaInfo(res, player);
                }
            }
            Material mat = block.getType();
            if(!Residence.getPermissionManager().isResidenceAdmin(player))
            {
                if(mat == Material.CHEST || mat == Material.FURNACE || mat == Material.BURNING_FURNACE || mat == Material.DISPENSER)
                {
                    ClaimedResidence res = Residence.getResidenceManger().getByLoc(block.getLocation());
                    if(res!=null)
                    {
                        if(!res.getPermissions().playerHas(player.getName(),"container", true))
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
                        if(!res.getPermissions().playerHas(player.getName(),"use", true))
                        {
                            event.setCancelled(true);
                            player.sendMessage("§cYou dont have use access for this Residence.");
                        }
                    }
                    else
                    {
                        if(!Residence.getWorldFlags().getPerms(player).has("use", true))
                        {
                            event.setCancelled(true);
                            player.sendMessage("§cWorld use is disabled.");
                        }
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

    @Override
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerInventory items = player.getInventory();
        Material heldItem = items.getItem(event.getNewSlot()).getType();
        String world = player.getWorld().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if(!Residence.getItemManager().isAllowed(heldItem, group, world))
        {
            ItemStack olditem = items.getItem(event.getPreviousSlot());
            ItemStack newitem = items.getItem(event.getNewSlot());
            items.remove(newitem);
            items.setItem(event.getPreviousSlot(), newitem);
            if(olditem!=null && olditem.getType()!=Material.AIR)
                items.setItem(event.getNewSlot(), olditem);
            player.sendMessage("§cYou are currently blacklisted from using that item.");
        }
        super.onItemHeldChange(event);
    }

    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Material groundItem = event.getItem().getItemStack().getType();
        String world = player.getWorld().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if(!Residence.getItemManager().isAllowed(groundItem, group, world))
        {
            event.getItem().remove();
            event.setCancelled(true);
            player.sendMessage("§cYou are currently blacklisted from using that item.");
        }
        super.onPlayerPickupItem(event);
    }

}

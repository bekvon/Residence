/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerQuitEvent;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceEnterEvent;
import com.bekvon.bukkit.residence.event.ResidenceLeaveEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ResidencePlayerListener extends PlayerListener {

    protected Map<String,String> cache;
    protected Map<String,Long> lastUpdate;
    protected Map<String,Location> lastOutsideLoc;
    protected List<String> healing;
    protected int minUpdateTime;
    protected boolean chatenabled;
    protected List<String> playerToggleChat;
    
    public ResidencePlayerListener()
    {
        cache = new HashMap<String,String>();
        lastUpdate = new HashMap<String,Long>();
        lastOutsideLoc = new HashMap<String,Location>();
        healing = Collections.synchronizedList(new ArrayList<String>());
        playerToggleChat = new ArrayList<String>();
        minUpdateTime = Residence.getConfig().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfig().chatEnabled();
    }

    public void reload()
    {
        cache = new HashMap<String,String>();
        lastUpdate = new HashMap<String,Long>();
        lastOutsideLoc = new HashMap<String,Location>();
        healing = Collections.synchronizedList(new ArrayList<String>());
        playerToggleChat = new ArrayList<String>();
        minUpdateTime = Residence.getConfig().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfig().chatEnabled();
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        cache.remove(pname);
        lastUpdate.remove(pname);
        lastOutsideLoc.remove(pname);
        healing.remove(pname);
        Residence.getChatManager().removeFromChannel(pname);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.isCancelled())
            return;
        Player player = event.getPlayer();
        Material heldItem = player.getItemInHand().getType();
        String world = player.getWorld().getName();
        String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if(!Residence.getItemManager().isAllowed(heldItem, permgroup, world))
        {
            player.sendMessage("§cYou are currently blacklisted from using your equiped item.");
            event.setCancelled(true);
        }
        if(!event.isCancelled() && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK))
        {
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
                        if(!res.getPermissions().playerHas(player.getName(),"container", res.getPermissions().playerHas(player.getName(), "use", true)))
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
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if(event.isCancelled())
            return;
        String resname = Residence.getResidenceManger().getNameByLoc(event.getBlockClicked().getLocation());
        ClaimedResidence res = Residence.getResidenceManger().getByName(resname);
        Player player = event.getPlayer();
        String pname = player.getName();
        boolean hasbuild;
        boolean hasbucket;
        if(res!=null)
        {
            if (Residence.getConfig().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(resname)) {
                    player.sendMessage("§cCannot modify a rented residence!");
                    event.setCancelled(true);
                    return;
                }
                res = Residence.getResidenceManger().getByName(resname);
            }
            ResidencePermissions perms = res.getPermissions();
            hasbuild = perms.playerHas(pname,"build", true);
            hasbucket = perms.playerHas(pname,"bucket", hasbuild);
        }
        else
        {
            hasbuild = Residence.getWorldFlags().getPerms(player).playerHas(pname, player.getWorld().getName(), "build", true);
            hasbucket = Residence.getWorldFlags().getPerms(player).playerHas(pname, player.getWorld().getName(), "bucket", hasbuild);
        }
        if ((!hasbuild && !hasbucket) || !hasbucket) {
            player.sendMessage("§cYou don't have permission to use buckets here here.");
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if(event.isCancelled())
            return;
        String resname = Residence.getResidenceManger().getNameByLoc(event.getBlockClicked().getLocation());
        ClaimedResidence res = Residence.getResidenceManger().getByName(resname);
        Player player = event.getPlayer();
        String pname = player.getName();
        boolean hasbuild;
        boolean hasbucket;
        if(res!=null)
        {
            if (Residence.getConfig().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(resname)) {
                    player.sendMessage("§cCannot modify a rented residence!");
                    event.setCancelled(true);
                    return;
                }
                res = Residence.getResidenceManger().getByName(resname);
            }
            ResidencePermissions perms = res.getPermissions();
            hasbuild = perms.playerHas(pname,"build", true);
            hasbucket = perms.playerHas(pname,"bucket", hasbuild);
        }
        else
        {
            hasbuild = Residence.getWorldFlags().getPerms(player).playerHas(pname, player.getWorld().getName(), "build", true);
            hasbucket = Residence.getWorldFlags().getPerms(player).playerHas(pname, player.getWorld().getName(), "bucket", hasbuild);
        }
        if ((!hasbuild && !hasbucket) || !hasbucket) {
            player.sendMessage("§cYou don't have permission to use buckets here.");
            event.setCancelled(true);
        }
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
            boolean enterArea = false;
            boolean chatchange = false;
            String areaname = cache.get(pname);
            if (areaname != null) {
                res = manager.getByName(areaname);
                if (res == null) {
                    cache.remove(pname);
                    areaname = null;
                } else {
                    if (!res.containsLoc(ploc)) {
                        String leave = res.getLeaveMessage();
                        ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(res,player);
                        Residence.getServ().getPluginManager().callEvent(leaveevent);
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
                            Residence.getChatManager().removeFromChannel(pname);
                        } else {
                            areaname = Residence.getResidenceManger().getNameByLoc(ploc);
                            cache.put(pname, areaname);
                            chatchange = true;
                        }
                    } else {
                        String subzone = res.getSubzoneNameByLoc(ploc);
                        if (subzone != null) {
                            areaname = areaname + "." + subzone;
                            cache.put(pname, areaname);
                            res = res.getSubzone(subzone);
                            enterArea = true;
                            chatchange = true;
                        }
                    }
                }
            }
            if(areaname == null)
            {
                areaname = manager.getNameByLoc(ploc);
                chatchange = true;
                enterArea = true;
            }
            if (areaname != null) {
                if(chatchange && chatenabled)
                    Residence.getChatManager().setChannel(pname, areaname);
                res = manager.getByName(areaname);
                if (res.getPermissions().playerHas(pname, "move", true) || Residence.getPermissionManager().isResidenceAdmin(player)) {
                    cache.put(pname, areaname);
                    if (enterArea) {
                        String enterMessage = res.getEnterMessage();
                        ResidenceEnterEvent enterevent = new ResidenceEnterEvent(res, player);
                        Residence.getServ().getPluginManager().callEvent(enterevent);
                        if(enterMessage!=null)
                            player.sendMessage("§e" + this.insertMessages(player, areaname, res, enterMessage));
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
                int health = player.getHealth();
                if(health<20)
                {
                    if(res.getPermissions().has("healing", false))
                    {
                        if(!healing.contains(pname))
                            healing.add(pname);
                    }
                    else
                    {
                        if(healing.contains(pname))
                            healing.remove(pname);
                    }
                }
            }
            else
            {
                lastOutsideLoc.put(pname, ploc);
                if(healing.contains(pname))
                    healing.remove(pname);
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

    public void doHeals() {
        try {
            Player[] p = Residence.getServ().getOnlinePlayers();
            for (Player player : p) {
                if (healing.contains(player.getName())) {
                    int health = player.getHealth();
                    if (health < 20) {
                        player.setHealth(health + 1);
                        //System.out.println("heal:" +player.getName() + " oldhealth = "+health+" newhealth = " + player.getHealth());
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        String pname = event.getPlayer().getName();
        if(chatenabled && playerToggleChat.contains(pname))
        {
            String area = cache.get(pname);
            if(area!=null)
            {
                ChatChannel channel = Residence.getChatManager().getChannel(area);
                if(channel!=null)
                    channel.chat(pname, event.getMessage());
                event.setCancelled(true);
            }
        }
    }

    public void tooglePlayerResidenceChat(Player player)
    {
        String pname = player.getName();
        if(playerToggleChat.contains(pname))
        {
            playerToggleChat.remove(pname);
            player.sendMessage("§eResidence chat toggled §cOFF§e!");
        }
        else
        {
            playerToggleChat.add(pname);
            player.sendMessage("§eResidence chat toggled §aON§e!");
        }
    }

    public String getLastAreaName(String player)
    {
        return cache.get(player);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.event.*;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

/**
 * 
 * @author Administrator
 */
public class ResidencePlayerListener implements Listener {

    protected Map<String, String> currentRes;
    protected Map<String, Long> lastUpdate;
    protected Map<String, Location> lastOutsideLoc;
    protected int minUpdateTime;
    protected boolean chatenabled;
    protected List<String> playerToggleChat;

    public ResidencePlayerListener() {
        currentRes = new HashMap<String, String>();
        lastUpdate = new HashMap<String, Long>();
        lastOutsideLoc = new HashMap<String, Location>();
        playerToggleChat = new ArrayList<String>();
        minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfigManager().chatEnabled();
        Player[] players = Residence.getServ().getOnlinePlayers();
        for (Player player : players) {
            lastUpdate.put(player.getName(), System.currentTimeMillis());
        }
    }

    public void reload() {
        currentRes = new HashMap<String, String>();
        lastUpdate = new HashMap<String, Long>();
        lastOutsideLoc = new HashMap<String, Location>();
        playerToggleChat = new ArrayList<String>();
        minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfigManager().chatEnabled();
        Player[] players = Residence.getServ().getOnlinePlayers();
        for (Player player : players) {
            lastUpdate.put(player.getName(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        currentRes.remove(pname);
        lastUpdate.remove(pname);
        lastOutsideLoc.remove(pname);
        Residence.getChatManager().removeFromChannel(pname);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastUpdate.put(player.getName(), 0L);
        if (Residence.getPermissionManager().isResidenceAdmin(player)) {
            Residence.turnResAdminOn(player);
        }
        handleNewLocation(player, player.getLocation(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Location loc = event.getRespawnLocation();
        Boolean bed = event.isBedSpawn();
        Player player = event.getPlayer();
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        if (res == null) {
            return;
        }
        if (res.getPermissions().playerHas(player.getName(), "move", true)) {
            return;
        }
        if (bed) {
            loc = player.getWorld().getSpawnLocation();
        }
        res = Residence.getResidenceManager().getByLoc(loc);
        if (res != null) {
            if (!res.getPermissions().playerHas(player.getName(), "move", true)) {
                loc = res.getOutsideFreeLoc(loc);
            }
        }
        player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoSpawn"));
        event.setRespawnLocation(loc);
    }

    private boolean isContainer(Material mat, Block block) {
        return FlagPermissions.getMaterialUseFlagList().containsKey(mat) && FlagPermissions.getMaterialUseFlagList().get(mat).equals("container") || Residence.getConfigManager().getCustomContainers().contains(Integer.valueOf(block.getTypeId()));
    }

    private boolean isCanUseEntity_BothClick(Material mat, Block block) {
        return mat == Material.LEVER || mat == Material.STONE_BUTTON || mat == Material.WOOD_BUTTON ||
                mat == Material.WOODEN_DOOR || mat == Material.SPRUCE_DOOR || mat == Material.BIRCH_DOOR ||
                mat == Material.JUNGLE_DOOR || mat == Material.ACACIA_DOOR || mat == Material.DARK_OAK_DOOR ||
                mat == Material.SPRUCE_FENCE_GATE || 
                mat == Material.BIRCH_FENCE_GATE || mat == Material.JUNGLE_FENCE_GATE || mat == Material.ACACIA_FENCE_GATE ||
                mat == Material.DARK_OAK_FENCE_GATE || mat == Material.TRAP_DOOR || mat == Material.FENCE_GATE ||
                mat == Material.PISTON_BASE || mat == Material.PISTON_STICKY_BASE || mat == Material.DRAGON_EGG ||
                Residence.getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getTypeId()));
    }

    private boolean isCanUseEntity_RClickOnly(Material mat, Block block) {
        return  mat == Material.ITEM_FRAME || mat == Material.BEACON || mat == Material.FLOWER_POT || mat == Material.COMMAND || mat == Material.ANVIL || mat == Material.CAKE_BLOCK || mat == Material.NOTE_BLOCK 
        		|| mat == Material.DIODE || mat == Material.DIODE_BLOCK_OFF || mat == Material.DIODE_BLOCK_ON 
        		|| mat == Material.BED_BLOCK || mat == Material.WORKBENCH || mat == Material.BREWING_STAND 
        		|| mat == Material.ENCHANTMENT_TABLE ||
                Residence.getConfigManager().getCustomRightClick().contains(Integer.valueOf(block.getTypeId()));
    }

    private boolean isCanUseEntity(Material mat, Block block) {
        return isCanUseEntity_BothClick(mat, block) || isCanUseEntity_RClickOnly(mat, block);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material heldItem = player.getItemInHand().getType();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Material mat = block.getType();
        if (!((isContainer(mat, block) || isCanUseEntity_RClickOnly(mat, block)) && event.getAction() == Action.RIGHT_CLICK_BLOCK || isCanUseEntity_BothClick(mat, block) || event.getAction() == Action.PHYSICAL)) {
            int typeId = player.getItemInHand().getTypeId();
            if (typeId != Residence.getConfigManager().getSelectionTooldID() && typeId != Residence.getConfigManager().getInfoToolID() && typeId != 351 && typeId != 416) {
                return;
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
        String world = player.getWorld().getName();
        String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
        boolean resadmin = Residence.isResAdminOn(player);
        if (event.getAction() == Action.PHYSICAL) {
            if (!resadmin) {
                boolean hasuse = perms.playerHas(player.getName(), world, "use", true);
                boolean haspressure = perms.playerHas(player.getName(), world, "pressure", hasuse);
                if ((!hasuse && !haspressure || !haspressure) && (mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (!perms.playerHas(player.getName(), world, "trample", perms.playerHas(player.getName(), world, "build", true)) && (mat == Material.SOIL || mat == Material.SOUL_SAND)) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!resadmin && !Residence.getItemManager().isAllowed(heldItem, permgroup, world)) {
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.getItemInHand().getTypeId() == Residence.getConfigManager().getSelectionTooldID()) {
                Plugin wep = Bukkit.getPluginManager().getPlugin("WorldEdit");
                if (wep != null) {
                    if (((WorldEditPlugin) wep).getConfig().getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
                        return;
                    }
                }
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                if (player.hasPermission("residence.select") || player.hasPermission("residence.create") && !player.isPermissionSet("residence.select") || group.canCreateResidences() && !player.isPermissionSet("residence.create") && !player.isPermissionSet("residence.select") || resadmin) {
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc1(player, loc);
                        player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectPoint", Residence.getLanguage().getPhrase("Primary")) + ChatColor.RED + "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")" + ChatColor.GREEN + "!");
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc2(player, loc);
                        player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectPoint", Residence.getLanguage().getPhrase("Secondary")) + ChatColor.RED + "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")" + ChatColor.GREEN + "!");
                    }
                }
            }
            if (player.getItemInHand().getTypeId() == Residence.getConfigManager().getInfoToolID()) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Location loc = block.getLocation();
                    String res = Residence.getResidenceManager().getNameByLoc(loc);
                    if (res != null) {
                        Residence.getResidenceManager().printAreaInfo(res, player);
                        event.setCancelled(true);
                    }
                    if (res == null) {
                        event.setCancelled(true);
                        player.sendMessage(Residence.getLanguage().getPhrase("NoResHere"));
                    }
                }
            }
            if (!resadmin) {
                if (heldItem != null) {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (player.getItemInHand().getTypeId() == 351) {
                            if (player.getItemInHand().getData().getData() == 15 && block.getType() == Material.GRASS || player.getItemInHand().getData().getData() == 3 && block.getTypeId() == 17 && (block.getData() == 3 || block.getData() == 7 || block.getData() == 11 || block.getData() == 15)) {
                                perms = Residence.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
                                if (!perms.playerHas(player.getName(), world, "build", true)) {
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }
                        if(heldItem == Material.ARMOR_STAND){
                        	perms = Residence.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
                            if (!perms.playerHas(player.getName(), world, "build", true)) {
                            	player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
                if (isContainer(mat, block) || isCanUseEntity(mat, block)) {
                    boolean hasuse = perms.playerHas(player.getName(), world, "use", true);
                    for (Entry<Material, String> checkMat : FlagPermissions.getMaterialUseFlagList().entrySet()) {
                        if (mat == checkMat.getKey()) {
                            if (!perms.playerHas(player.getName(), world, checkMat.getValue(), hasuse)) {
                                if (hasuse || checkMat.getValue().equals("container")) {
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", checkMat.getValue()));
                                    return;
                                } else {
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
                                    return;
                                }
                            }
                        }
                    }
                    if (Residence.getConfigManager().getCustomContainers().contains(Integer.valueOf(block.getTypeId()))) {
                        if (!perms.playerHas(player.getName(), world, "container", hasuse)) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
                            return;
                        }
                    }
                    if (Residence.getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getTypeId()))) {
                        if (!hasuse) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
                            return;
                        }
                    }
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (Residence.getConfigManager().getCustomRightClick().contains(Integer.valueOf(block.getTypeId()))) {
                            if (!hasuse) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        Entity ent = event.getRightClicked();
        Material heldItem = player.getItemInHand().getType();
        if (!(ent instanceof Hanging)) {
            return;
        }
        Hanging hanging = (Hanging) ent;
        if (hanging.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(ent.getLocation(), player);
        String world = player.getWorld().getName();
        String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if (!Residence.getItemManager().isAllowed(heldItem, permgroup, world)) {
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
            event.setCancelled(true);
            return;
        }
        if (!perms.playerHas(player.getName(), world, "container", perms.playerHas(player.getName(), world, "use", true))) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
    	Player player = event.getPlayer();
    	 if (Residence.isResAdminOn(player)) {
            return;
        }
    	
        Entity ent = event.getRightClicked();
        if (ent.getType() != EntityType.ARMOR_STAND) {
            return;
        }
    		
        FlagPermissions perms = Residence.getPermsByLocForPlayer(ent.getLocation(), player);
        String world = player.getWorld().getName();
    		
        if (!perms.playerHas(player.getName(), world, "container", perms.playerHas(player.getName(), world, "use", true))) {
    	   event.setCancelled(true);
    	   player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
    	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        String pname = player.getName();
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
        if (res != null) {
            if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(res.getName())) {
                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
        if (!perms.playerHas(pname, player.getWorld().getName(), "bucket", perms.playerHas(pname, player.getWorld().getName(), "build", true))) {
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "bucket"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        String pname = player.getName();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
        if (res != null) {
            if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(res.getName())) {
                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
        boolean hasbucket = perms.playerHas(pname, player.getWorld().getName(), "bucket", perms.playerHas(pname, player.getWorld().getName(), "build", true));
        if (!hasbucket) {
            player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "bucket"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location loc = event.getTo();
        Player player = event.getPlayer();
        
        if (Residence.isResAdminOn(player)) {
            handleNewLocation(player, loc, false);
            return;
        }
        
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        if (event.getCause() == TeleportCause.ENDER_PEARL) {
            if (res != null) {
                String areaname = Residence.getResidenceManager().getNameByLoc(loc);
                if (!res.getPermissions().playerHas(player.getName(), "move", true)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceMoveDeny", areaname));
                    return;
                }
            }
        }
        if (event.getCause() == TeleportCause.PLUGIN) {
            if (res != null) {
                String areaname = Residence.getResidenceManager().getNameByLoc(loc);
                if (!res.getPermissions().playerHas(player.getName(), "tp", true) && !player.hasPermission("residence.admin.tp")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("TeleportDeny", areaname));
                    return;
                }
            }
        }
        handleNewLocation(player, loc, false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        long last = lastUpdate.get(player.getName());
        long now = System.currentTimeMillis();
        if (now - last < Residence.getConfigManager().getMinMoveUpdateInterval()) {
            return;
        }
        lastUpdate.put(player.getName(), now);
        if (event.getFrom().getWorld() == event.getTo().getWorld()) {
            if (event.getFrom().distance(event.getTo()) == 0) {
                return;
            }
        }
        handleNewLocation(player, event.getTo(), true);
    }

    public void handleNewLocation(Player player, Location loc, boolean move) {
        String pname = player.getName();

        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        String areaname = null;
        boolean chatchange = false;
        String subzone = null;
        if (res != null) {
            areaname = Residence.getResidenceManager().getNameByLoc(loc);
            while (res.getSubzoneByLoc(player.getLocation()) != null) {
                subzone = res.getSubzoneNameByLoc(player.getLocation());
                res = res.getSubzoneByLoc(player.getLocation());
                areaname = areaname + "." + subzone;
            }
        }
        ClaimedResidence ResOld = null;
        if (currentRes.containsKey(pname)) {
            ResOld = Residence.getResidenceManager().getByName(currentRes.get(pname));
            if (ResOld == null) {
                currentRes.remove(pname);
            }
        }
        if (res == null) {
            lastOutsideLoc.put(pname, loc);
            if (ResOld != null) {
                String leave = ResOld.getLeaveMessage();
                /*
                 * TODO - ResidenceLeaveEvent is deprecated as of 21-MAY-2013. Its functionality is replaced by
                 * ResidenceChangedEvent. For now, this event is still supported until it is removed at a
                 * suitable time in the future.
                 */
                ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(ResOld, player);
                Residence.getServ().getPluginManager().callEvent(leaveevent);
                
                // New ResidenceChangeEvent
                ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(ResOld, null, player);
                Residence.getServ().getPluginManager().callEvent(chgEvent);
                
                if (leave != null && !leave.equals("")) {
                    player.sendMessage(ChatColor.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
                }
                currentRes.remove(pname);
                Residence.getChatManager().removeFromChannel(pname);
            }
            return;
        }
        if (move) {
            if (!res.getPermissions().playerHas(pname, "move", true) && !Residence.isResAdminOn(player) && !player.hasPermission("residence.admin.move")) {
                Location lastLoc = lastOutsideLoc.get(pname);
                if (lastLoc != null) {
                    player.teleport(lastLoc);
                } else {
                    player.teleport(res.getOutsideFreeLoc(loc));
                }
                player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceMoveDeny", res.getName().split("\\.")[res.getName().split("\\.").length - 1]));
                return;
            }
        }
        lastOutsideLoc.put(pname, loc);
        if (!currentRes.containsKey(pname) || ResOld != res) {
            currentRes.put(pname, areaname);
            if (subzone == null) {
                chatchange = true;
            }
            
            // "from" residence for ResidenceChangedEvent
            ClaimedResidence chgFrom = null;
            if (ResOld != res && ResOld != null) {
                String leave = ResOld.getLeaveMessage();
                chgFrom = ResOld;
                
                /*
                 * TODO - ResidenceLeaveEvent is deprecated as of 21-MAY-2013. Its functionality is replaced by
                 * ResidenceChangedEvent. For now, this event is still supported until it is removed at a
                 * suitable time in the future.
                 */
                ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(ResOld, player);
                Residence.getServ().getPluginManager().callEvent(leaveevent);
                
                if (leave != null && !leave.equals("") && ResOld != res.getParent()) {
                    player.sendMessage(ChatColor.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
                }
            }
            String enterMessage = res.getEnterMessage();
            
            /*
             * TODO - ResidenceEnterEvent is deprecated as of 21-MAY-2013. Its functionality is replaced by
             * ResidenceChangedEvent. For now, this event is still supported until it is removed at a
             * suitable time in the future.
             */
            ResidenceEnterEvent enterevent = new ResidenceEnterEvent(res, player);
            Residence.getServ().getPluginManager().callEvent(enterevent);
            
            // New ResidenceChangedEvent
            ResidenceChangedEvent chgEvent = new ResidenceChangedEvent( chgFrom, res, player);
            Residence.getServ().getPluginManager().callEvent(chgEvent);
            
            if (enterMessage != null && !enterMessage.equals("") && !(ResOld != null && res == ResOld.getParent())) {
                player.sendMessage(ChatColor.YELLOW + this.insertMessages(player, areaname, res, enterMessage));
            }
        }
        if (chatchange && chatenabled) {
            Residence.getChatManager().setChannel(pname, areaname);
        }
    }

    public String insertMessages(Player player, String areaname, ClaimedResidence res, String message) {
        try {
            message = message.replaceAll("%player", player.getName());
            message = message.replaceAll("%owner", res.getPermissions().getOwner());
            message = message.replaceAll("%residence", areaname);
        } catch (Exception ex) {
            return "";
        }
        return message;
    }

    public void doHeals() {
        try {
            Player[] p = Residence.getServ().getOnlinePlayers();
            for (Player player : p) {
                String resname = Residence.getPlayerListener().getCurrentResidenceName(player.getName());
                ClaimedResidence res = null;
                if (resname != null) {
                    res = Residence.getResidenceManager().getByName(resname);
                }
                if (res != null && res.getPermissions().has("healing", false)) {
                	Damageable damage = player;
                	double health = damage.getHealth();
                    if (health < 20 && !player.isDead()) {
                        player.setHealth(health + 1);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String pname = event.getPlayer().getName();
        if (chatenabled && playerToggleChat.contains(pname)) {
            String area = currentRes.get(pname);
            if (area != null) {
                ChatChannel channel = Residence.getChatManager().getChannel(area);
                if (channel != null) {
                    channel.chat(pname, event.getMessage());
                }
                event.setCancelled(true);
            }
        }
    }

    public void tooglePlayerResidenceChat(Player player) {
        String pname = player.getName();
        if (playerToggleChat.contains(pname)) {
            playerToggleChat.remove(pname);
            player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ResidenceChat", ChatColor.RED + "OFF" + ChatColor.YELLOW + "!"));
        } else {
            playerToggleChat.add(pname);
            player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ResidenceChat", ChatColor.RED + "ON" + ChatColor.YELLOW + "!"));
        }
    }

    public String getCurrentResidenceName(String player) {
        return currentRes.get(player);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;
import org.bukkit.ChatColor;

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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerQuitEvent;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceEnterEvent;
import com.bekvon.bukkit.residence.event.ResidenceLeaveEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.inori.utils.ILog;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author Administrator
 */
public class ResidencePlayerListener implements Listener {

    protected Map<String,String> currentRes;
    protected Map<String,Long> lastUpdate;
    protected Map<String,Location> lastOutsideLoc;
    protected int minUpdateTime;
    protected boolean chatenabled;
    protected List<String> playerToggleChat;
    
    public ResidencePlayerListener()
    {
        currentRes = new HashMap<String,String>();
        lastUpdate = new HashMap<String,Long>();
        lastOutsideLoc = new HashMap<String,Location>();
        playerToggleChat = new ArrayList<String>();
        minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfigManager().chatEnabled();
        for (Player player : Residence.getServ().getOnlinePlayers()) {
        	lastUpdate.put(player.getName(), System.currentTimeMillis());
    	}
    }

    public void reload()
    {
        currentRes = new HashMap<String,String>();
        lastUpdate = new HashMap<String,Long>();
        lastOutsideLoc = new HashMap<String,Location>();
        playerToggleChat = new ArrayList<String>();
        minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfigManager().chatEnabled();
        for (Player player : Residence.getServ().getOnlinePlayers()) {
        	lastUpdate.put(player.getName(), System.currentTimeMillis());
    	}
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        currentRes.remove(pname);
        lastUpdate.remove(pname);
        lastOutsideLoc.remove(pname);
        Residence.getChatManager().removeFromChannel(pname);
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastUpdate.put(player.getName(), 0L);
        if(Residence.getPermissionManager().isResidenceAdmin(player)){
        	Residence.turnResAdminOn(player);
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
        String areaname = Residence.getResidenceManager().getNameByLoc(player.getLocation());
        String subzone = null;
        if(res!=null){
    		while (res.getSubzoneByLoc(player.getLocation()) != null) {
    			subzone = res.getSubzoneNameByLoc(player.getLocation());
        		res = res.getSubzoneByLoc(player.getLocation());
        	    areaname = areaname + "." + subzone;
    		}
    		currentRes.put(player.getName(), areaname);
        }
    }
	private boolean isContainer(Material mat, Block block) {
		return mat == Material.JUKEBOX || mat == Material.CHEST || mat == Material.FURNACE || mat == Material.BURNING_FURNACE || mat == Material.DISPENSER || mat == Material.CAKE_BLOCK || Residence.getConfigManager().getCustomContainers().contains(Integer.valueOf(block.getTypeId()));
	}

	private boolean isCanUseEntity_BothClick(Material mat, Block block) {
		return mat == Material.LEVER || mat == Material.STONE_BUTTON || 
			   mat == Material.WOODEN_DOOR || mat == Material.TRAP_DOOR || mat == Material.FENCE_GATE|| mat == Material.NETHER_FENCE || 
			   mat == Material.PISTON_BASE || mat == Material.PISTON_STICKY_BASE || mat==Material.DRAGON_EGG || 
			   Residence.getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getTypeId()));
	}
	
	private boolean isCanUseEntity_RClickOnly(Material mat, Block block) {
		return mat == Material.NOTE_BLOCK || mat == Material.DIODE || mat == Material.DIODE_BLOCK_OFF || mat == Material.DIODE_BLOCK_ON || mat == Material.BED_BLOCK || mat == Material.WORKBENCH || mat == Material.BREWING_STAND || mat == Material.ENCHANTMENT_TABLE ||
			   Residence.getConfigManager().getCustomRightClick().contains(Integer.valueOf(block.getTypeId()));
	}

	private boolean isCanUseEntity(Material mat, Block block) {
		return isCanUseEntity_BothClick(mat, block) || isCanUseEntity_RClickOnly(mat, block);
	}

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.isCancelled())
            return;

        Player player = event.getPlayer();
        Material heldItem = player.getItemInHand().getType();
        Block block = event.getClickedBlock();
        Material mat = block.getType();
        ILog.sendToPlayer(player, mat.toString());
	if(!(((isContainer(mat, block) || isCanUseEntity_RClickOnly(mat, block)) && event.getAction() == Action.RIGHT_CLICK_BLOCK) || 
        		isCanUseEntity_BothClick(mat, block))||event.getAction() == Action.PHYSICAL)
        {            
        	int typeId = player.getItemInHand().getTypeId();
        	if(typeId != Residence.getConfigManager().getSelectionTooldID() &&
        	   typeId != Residence.getConfigManager().getInfoToolID()&&typeId!=351)
        	{
        		return;
        	}
        }
        ILog.sendToPlayer(player, "onPlayerInteract Fired");
        
        String world = player.getWorld().getName();
        String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
        boolean resadmin = Residence.isResAdminOn(player);
                if(event.getAction() == Action.PHYSICAL){
        	if(!resadmin){        		
        		ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
        		boolean hasuse = true;
        		boolean hastrample = true;
        		boolean haspressure = true;
        		boolean hasbuild = true;
                if (res != null) {
                    hasuse = res.getPermissions().playerHas(player.getName(), "use", true);
                    hasbuild = res.getPermissions().playerHas(player.getName(), "build", true);
                    haspressure = res.getPermissions().playerHas(player.getName(), "pressure", hasuse);
                    hastrample = res.getPermissions().playerHas(player.getName(), "trample", hasbuild);
                } else {
                    FlagPermissions perms = Residence.getWorldFlags().getPerms(player);
                    hasuse = perms.playerHas(player.getName(), player.getWorld().getName(), "use", true);
                    hasbuild = perms.playerHas(player.getName(), player.getWorld().getName(), "build", true);
                    haspressure = perms.playerHas(player.getName(), player.getWorld().getName(), "pressure", hasuse);
                    hastrample =  perms.playerHas(player.getName(), player.getWorld().getName(), "trample", hasbuild);
                }        			
        		if(((!hasuse && !haspressure) || !haspressure)&&(mat==Material.STONE_PLATE || mat == Material.WOOD_PLATE)){
        			event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","pressure"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			} 
        	    }
        	    if(((!hasbuild && !hastrample) || !hastrample) && (mat == Material.SOIL || mat == Material.SOUL_SAND)){
        	    	event.setCancelled(true);
        	    }
        	}
        }
        if(!resadmin && !Residence.getItemManager().isAllowed(heldItem, permgroup, world))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ItemBlacklisted"));
            event.setCancelled(true);
            return;
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (player.getItemInHand().getTypeId() == Residence.getConfigManager().getSelectionTooldID()) {
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                if(player.hasPermission("residence.create") || group.canCreateResidences() || resadmin)
                {
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc1(player, loc);
                        player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SelectPoint",Residence.getLanguage().getPhrase("Primary"))+ChatColor.RED+"(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")"+ChatColor.GREEN+"!");
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc2(player, loc);
                        player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SelectPoint",Residence.getLanguage().getPhrase("Secondary"))+ChatColor.RED+"(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")"+ChatColor.GREEN+"!");
                    }
                }
            }
            if(player.getItemInHand().getTypeId() == Residence.getConfigManager().getInfoToolID())
            {
                if(event.getAction() == Action.LEFT_CLICK_BLOCK)
                {
                    Location loc = block.getLocation();
                    String res = Residence.getResidenceManager().getNameByLoc(loc);
                    if(res!=null)
                        Residence.getResidenceManager().printAreaInfo(res, player);
                        event.setCancelled(true);
                    if(res==null){
                        event.setCancelled(true);
                        player.sendMessage(Residence.getLanguage().getPhrase("NoResHere"));
                    }
                }
            }
            if(!resadmin)
            {
                ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
                if(player.getItemInHand()!=null&&res!=null){
                	if(event.getAction()==Action.RIGHT_CLICK_BLOCK){
                		if(player.getItemInHand().getTypeId()==351&&block.getType()==Material.GRASS){
                			if(player.getItemInHand().getData().getData()==15){
                				if(!res.getPermissions().playerHas(player.getName(), "build", true)) {
                					event.setCancelled(true);
                				}
                			}
                		}
	              	}
                }
                if(isContainer(mat, block))
                {
                    boolean hasuse;
                    boolean hascontainer;
                    if (res != null) {
                        hasuse = res.getPermissions().playerHas(player.getName(), "use", true);
                        hascontainer = res.getPermissions().playerHas(player.getName(), "container", hasuse);
                    } else {
                        FlagPermissions perms = Residence.getWorldFlags().getPerms(player);
                        hasuse = perms.playerHas(player.getName(), player.getWorld().getName(), "use", true);
                        hascontainer = perms.playerHas(player.getName(), player.getWorld().getName(), "container", hasuse);
                    }
                    if ((!hasuse && !hascontainer) || !hascontainer) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","container"));
                    }
                }
                else if(isCanUseEntity(mat, block))
                {
                    if(res!=null)
                    {
                    	ResidencePermissions perms = res.getPermissions(); 
                    	boolean hasuse = perms.playerHas(player.getName(), "use", true);
                    	if(!perms.playerHas(player.getName(),"diode", hasuse) && (mat == Material.DIODE || mat == Material.DIODE_BLOCK_OFF || mat == Material.DIODE_BLOCK_ON)){
                    		event.setCancelled(true);
                    		if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","diode"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                    	}
                    	if(!perms.playerHas(player.getName(),"table", hasuse) && mat == Material.WORKBENCH){
                    		event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","table"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                    	}
                    	if(!perms.playerHas(player.getName(),"door", hasuse) && (mat == Material.WOODEN_DOOR || mat == Material.FENCE_GATE|| mat == Material.NETHER_FENCE || mat == Material.TRAP_DOOR)){
                    		event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","door"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}
                    	}
                    	if(!perms.playerHas(player.getName(),"enchant", hasuse)&& mat == Material.ENCHANTMENT_TABLE){
                    		event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","enchant"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}
                    	}
                    	if(!perms.playerHas(player.getName(),"button", hasuse)&& mat == Material.STONE_BUTTON){
                    		event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","button"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                    	}
                    	if(!perms.playerHas(player.getName(),"lever", hasuse)&& mat == Material.LEVER){
                    		event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","lever"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                    	}
                    	if(!perms.playerHas(player.getName(),"bed", hasuse)&& mat == Material.BED_BLOCK){
                    		event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","bed"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                        }
                        if(!perms.playerHas(player.getName(),"brew", hasuse)&& mat == Material.BREWING_STAND){
                        	event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","brew"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                        }
                        if(!perms.playerHas(player.getName(),"cake", hasuse) && mat==Material.CAKE_BLOCK){
                        	event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","cake"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                        }
                        if(!perms.playerHas(player.getName(),"note", hasuse) && mat==Material.NOTE_BLOCK){
                        	event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","note"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                        }                      
                        if(!perms.playerHas(player.getName(),"egg", hasuse) && mat==Material.DRAGON_EGG){
                        	event.setCancelled(true);
        			if(hasuse){
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","egg"));
        			} else {
        			    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
        			}   
                        }
                    }
                    else
                    {
                        if(!Residence.getWorldFlags().getPerms(player).has("use", true))
                        {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","use"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if(event.isCancelled())
            return;
        String resname = Residence.getResidenceManager().getNameByLoc(event.getBlockClicked().getLocation());
        ClaimedResidence res = Residence.getResidenceManager().getByName(resname);
        Player player = event.getPlayer();
        String pname = player.getName();
        boolean hasbuild;
        boolean hasbucket;
        boolean resadmin = Residence.isResAdminOn(player);
        if(res!=null)
        {
            if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(resname)) {
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
                res = Residence.getResidenceManager().getByName(resname);
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
        if ((!hasbuild && !hasbucket && !resadmin) || !hasbucket && !resadmin) {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","bucket"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if(event.isCancelled())
            return;
        String resname = Residence.getResidenceManager().getNameByLoc(event.getBlockClicked().getLocation());
        ClaimedResidence res = Residence.getResidenceManager().getByName(resname);
        Player player = event.getPlayer();
        String pname = player.getName();
        boolean hasbuild;
        boolean hasbucket;
        boolean resadmin = Residence.isResAdminOn(player);
        if(res!=null)
        {
            if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(resname)) {
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
                res = Residence.getResidenceManager().getByName(resname);
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
        if ((!hasbuild && !hasbucket && !resadmin) || !hasbucket && !resadmin) {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagDeny","bucket"));
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event){
    	if(event.isCancelled())
            return;
    	Location loc = event.getTo();
    	Player player = event.getPlayer();
    	ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
    	boolean resadmin = Residence.isResAdminOn(player);	
	if(event.getCause()==TeleportCause.ENDER_PEARL){
	    if(res!=null){
	        String areaname = Residence.getResidenceManager().getNameByLoc(loc);
		if(!res.getPermissions().playerHas(player.getName(), "move", true)&&!resadmin){
		    event.setCancelled(true);
		    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceMoveDeny",areaname));
		}
	    } 
    	}
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.isCancelled())
            return;
    	Player player = event.getPlayer();
        long last = lastUpdate.get(player.getName());
        long now = System.currentTimeMillis();
        if(now - last < Residence.getConfigManager().getMinMoveUpdateInterval())
            return;
        lastUpdate.put(player.getName(), now);
        if(event.getFrom().getWorld() == event.getTo().getWorld())
        {
            ILog.sendToPlayer(player, "onPlayerMove("+event.getFrom().distance(event.getTo())+") Fired");
            if(event.getFrom().distance(event.getTo()) == 0)
    		return;
        }
    	String pname = player.getName();
    	Location loc = event.getTo();
    	ResidenceManager Manager = Residence.getResidenceManager();
    	ClaimedResidence res = Manager.getByLoc(loc);
    	String areaname = Residence.getResidenceManager().getNameByLoc(loc);
    	boolean chatchange = false;
    	String subzone = null;
    	if(res!=null){
    		while (res.getSubzoneByLoc(player.getLocation()) != null) {
    			subzone = res.getSubzoneNameByLoc(player.getLocation());
        		res = res.getSubzoneByLoc(player.getLocation());
        	    areaname = areaname + "." + subzone;
    		}
    	}
        ClaimedResidence ResOld = null;
	if(currentRes.containsKey(pname)){
    		ResOld = Residence.getResidenceManager().getByName(currentRes.get(pname));
    		if(ResOld==null){
    			currentRes.remove(pname);
    		}
	}
    	if(res==null){
    		if(lastOutsideLoc.containsKey(pname)){
    			lastOutsideLoc.remove(pname);
    		}
    		lastOutsideLoc.put(pname, loc);
    		if(ResOld!=null){
                	String leave = ResOld.getLeaveMessage();
                	ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(ResOld,player);
                	Residence.getServ().getPluginManager().callEvent(leaveevent);
               		if (leave != null && !leave.equals("")) {
                		player.sendMessage(ChatColor.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
                	}
    			currentRes.remove(pname);
    			Residence.getChatManager().removeFromChannel(pname);
    		}
    		return;
    	}
        if (!res.getPermissions().playerHas(pname, "move", true) && !Residence.isResAdminOn(player)) {
            Location lastLoc = lastOutsideLoc.get(pname);
            if (lastLoc != null) {
                player.teleport(lastLoc);
            } else {
                player.teleport(res.getOutsideFreeLoc(event.getTo()));
            }
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceMoveDeny", res.getName().split("\\.")[(res.getName().split("\\.").length)-1]));
            return;
        }
	if(lastOutsideLoc.containsKey(pname)){
		lastOutsideLoc.remove(pname);
	}
	lastOutsideLoc.put(pname, loc);
        if(!currentRes.containsKey(pname)||ResOld!=res){
        	if(currentRes.containsKey(pname)){
        		currentRes.remove(pname);
        	}
        	currentRes.put(pname, areaname);
        	if(subzone==null){
        		chatchange = true;
        	}
        	if(ResOld!=res&&ResOld!=null){
                	String leave = ResOld.getLeaveMessage();
                	ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(ResOld,player);
                	Residence.getServ().getPluginManager().callEvent(leaveevent);
                	if (leave != null && !leave.equals("")) {
                   		player.sendMessage(ChatColor.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
                	}
        	}
        	String enterMessage = res.getEnterMessage();
		ResidenceEnterEvent enterevent = new ResidenceEnterEvent(res, player);
		Residence.getServ().getPluginManager().callEvent(enterevent);
		if(enterMessage!=null){
                	player.sendMessage(ChatColor.YELLOW + this.insertMessages(player, areaname, res, enterMessage));
        	}
        }
        if(chatchange && chatenabled){
	        Residence.getChatManager().setChannel(pname, areaname);
        }
    }
    public String insertMessages(Player player, String areaname, ClaimedResidence res, String message) {
        try
        {
            message = message.replaceAll("%player", player.getName());
            message = message.replaceAll("%owner", res.getPermissions().getOwner());
            message = message.replaceAll("%residence", areaname);
        } catch(Exception ex)
        {
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
                if(resname!=null)
                    res = Residence.getResidenceManager().getByName(resname);
                if (res != null && res.getPermissions().has("healing", false)) {
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if(event.isCancelled()){return;}
        String pname = event.getPlayer().getName();
        if(chatenabled && playerToggleChat.contains(pname))
        {
            String area = currentRes.get(pname);
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
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("ResidenceChat",ChatColor.RED+"OFF"+ChatColor.YELLOW+"!"));
        }
        else
        {
            playerToggleChat.add(pname);
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("ResidenceChat",ChatColor.RED+"ON"+ChatColor.YELLOW+"!"));
        }
    }

    @Deprecated
    public String getLastAreaName(String player)
    {
        return currentRes.get(player);
    }
    
    public String getCurrentResidenceName(String player)
    {
        return currentRes.get(player);
    }
}

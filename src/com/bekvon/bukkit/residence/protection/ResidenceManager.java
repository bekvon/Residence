/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.persistance.SQLManager;
import com.bekvon.bukkit.residence.text.help.InformationPager;

/**
 *
 * @author Administrator
 */
public class ResidenceManager {
	protected Map<String,ClaimedResidence> residences;
	protected SQLManager sql;

	public ResidenceManager() {
		residences = Collections.synchronizedMap(new HashMap<String,ClaimedResidence>());
		sql = Residence.getSQLManager();
	}

	public ClaimedResidence getByLoc(Location loc) {
		if(loc==null) {
			return null;
		}
		ClaimedResidence res = sql.getResByLocation(loc);
		if (res == null) {
			return null;
		}
		ClaimedResidence subres = res.getSubzoneByLoc(loc);
		if (subres == null) {
			return res;
		}
		return subres;
	}
	public ClaimedResidence getByName(String name) {
		if(name==null) {
			return null;
		}
		String[] split = name.split("\\.");
		if (split.length == 1) {
			return sql.getResByName(name);
		}
		ClaimedResidence res = sql.getResByName(name);
		for (int i = 1; i < split.length; i++) {
			if (res != null) {
				res = res.getSubzone(split[i]);
			} else {
				return null;
			}
		}
		return res;
	}

	public String getNameByLoc(Location loc) {
		if(loc==null) {
			return null;
		}
		ClaimedResidence res = sql.getResByLocation(loc);
		if(res==null) {
			return null;
		}
		String name = res.getName();
		String szname = res.getSubzoneNameByLoc(loc);
		if (szname != null) {
			return name + "." + szname;
		}
		return name;
	}

	public String getNameByRes(ClaimedResidence res) {
		return res.getName();
	}

	public boolean addResidence(String name, String owner, Location loc1, Location loc2) {
		if(!Residence.validName(name)) {
			return false;
		}
		if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
			return false;
		}
		PermissionGroup group = Residence.getPermissionManager().getGroup(owner, loc1.getWorld().getName());
		CuboidArea newArea = new CuboidArea(loc1, loc2,"main");
		ClaimedResidence newRes = new ClaimedResidence(owner, loc1.getWorld().getName());
		newRes.getPermissions().applyDefaultFlags();
		newRes.setEnterMessage(group.getDefaultEnterMessage());
		newRes.setLeaveMessage(group.getDefaultLeaveMessage());
		ResidenceCreationEvent resevent = new ResidenceCreationEvent(null, name, newRes, newArea);
		Residence.getServ().getPluginManager().callEvent(resevent);
		if (resevent.isCancelled()) {
			return false;
		}
		newArea = resevent.getPhysicalArea();
		name = resevent.getResidenceName();
		if (residences.containsKey(name)) {
			return false;
		}
		newRes.addArea(newArea, "main");
		if (newRes.getAreaCount() != 0) {
			sql.addResidence(newRes);
		}
		return true;
	}

	public void addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin) {
		if(!Residence.validName(name)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
			return;
		}
		if(player == null) {
			return;
		}
		if(loc1==null || loc2==null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectPoints"));
			return;
		}
		PermissionGroup group = Residence.getPermissionManager().getGroup(player);
		boolean createpermission = group.canCreateResidences() || Residence.getPermissionManager().hasAuthority(player, "residence.create");
		if (!createpermission && !resadmin) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			return;
		}
		if (getOwnedZoneCount(player.getName()) >= group.getMaxZones() && !resadmin) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceTooMany"));
			return;
		}
		CuboidArea newArea = new CuboidArea(loc1, loc2, "main");
		ClaimedResidence newRes = new ClaimedResidence(player.getName(), loc1.getWorld().getName());
		newRes.getPermissions().applyDefaultFlags();
		newRes.setEnterMessage(group.getDefaultEnterMessage());
		newRes.setLeaveMessage(group.getDefaultLeaveMessage());
		ResidenceCreationEvent resevent = new ResidenceCreationEvent(player,name, newRes, newArea);
		Residence.getServ().getPluginManager().callEvent(resevent);
		if(resevent.isCancelled()) {
			return;
		}
		newArea = resevent.getPhysicalArea();
		name = resevent.getResidenceName();
		if (residences.containsKey(name)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceAlreadyExists",ChatColor.YELLOW+name+ChatColor.RED));
			return;
		}
		newRes.addArea(player, newArea, "main", resadmin);
		if(newRes.getAreaCount()!=0) {
			sql.addResidence(newRes);
			Residence.getLeaseManager().removeExpireTime(name);
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceCreate",ChatColor.YELLOW + name + ChatColor.GREEN));
			if(Residence.getConfigManager().useLeases()) {
				Residence.getLeaseManager().setExpireTime(player, name, group.getLeaseGiveTime());
			}
		}
	}

	public void listResidences(Player player) {
		this.listResidences(player, player.getName(), 1);
	}

	public void listResidences(Player player, int page) {
		this.listResidences(player, player.getName(), page);
	}

	public void listResidences(Player player, String targetplayer) {
		this.listResidences(player, targetplayer, 1);
	}

	public void listResidences(Player player, String targetplayer, int page) {
		this.listResidences(player, targetplayer, page, false);
	}

	public void listResidences(Player player, int page, boolean showhidden) {
		this.listResidences(player, player.getName(), page, showhidden);
	}

	public void listResidences(Player player, String targetplayer, int page, boolean showhidden) {
		ArrayList<String> temp = new ArrayList<String>();
		for(ClaimedResidence res: sql.getAllResidences(player.getName())) {
			boolean hidden = res.getPermissions().has("hidden", false);
			if( showhidden && hidden || !showhidden && !hidden || res.getPermissions().getOwner().equals(player.getName()) && targetplayer.equals(player.getName()) && !showhidden && hidden) {
				if(res.getPermissions().getOwner().equalsIgnoreCase(targetplayer)) {
					temp.add(ChatColor.GREEN+res.getName()+ChatColor.YELLOW+" - "+Residence.getLanguage().getPhrase("World") + ": " + res.getWorld());
				}
			}
		}
		InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences") + " - " + targetplayer, temp, page);
	}

	public String checkAreaCollision(CuboidArea newarea, ClaimedResidence parentResidence) {
		for (ClaimedResidence res : sql.getAllResidences(null)) {
			if (res != parentResidence && res.checkCollision(newarea)) {
				return res.getName();
			}
		}
		return null;
	}

	public void removeResidence(String name) {
		this.removeResidence(null, name, true);
	}

	public void removeResidence(Player player, String name, boolean resadmin) {
		ClaimedResidence res = this.getByName(name);
		if (res != null) {
			if (player != null && !resadmin) {
				if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
					return;
				}
			}
			ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, player==null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
			Residence.getServ().getPluginManager().callEvent(resevent);
			if(resevent.isCancelled()) {
				return;
			}
			ClaimedResidence parent = res.getParent();
			if (parent == null) {
				sql.removeResidence(name);
				residences.remove(name);
				if(player != null) {
					player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceRemove",ChatColor.YELLOW + name + ChatColor.GREEN));
				}
			} else {
				String[] split = name.split("\\.");
				if(player != null) {
					parent.removeSubzone(player, split[split.length - 1], true);
				} else {
					parent.removeSubzone(split[split.length - 1]);
				}
			}
			//Residence.getLeaseManager().removeExpireTime(name); - causing concurrent modification exception in lease manager... worked around for now
			Residence.getRentManager().removeRentable(name);

		} else {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
			}
		}
	}

	public void removeAllByOwner(String owner) {
		for(ClaimedResidence res : sql.getAllResidences(owner)){
			res.remove();
		}
	}

	public int getOwnedZoneCount(String player) {
		return sql.getAllResidences(player).size();
	}

	public String[] getResidenceList() {
		return sql.getAllResNames();
	}

	public void listAllResidences(Player player, int page) {
		this.listAllResidences(player, page, false);
	}

	public void listAllResidences(Player player, int page, boolean showhidden) {
		ArrayList<String> temp = new ArrayList<String>();
		for(ClaimedResidence res : sql.getAllResidences(player.getName())){
			boolean hidden = res.getPermissions().has("hidden", false);
			if( showhidden && hidden || !showhidden && !hidden || player.getName().equals(res.getOwner())) {
				temp.add(ChatColor.GREEN + res.getName() + ChatColor.YELLOW+" - "+Residence.getLanguage().getPhrase("Owner") + ": " + res.getOwner() + " - " + Residence.getLanguage().getPhrase("World")+": " + res.getWorld());
			}
		}
		InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences"), temp, page);
	}

	public void printAreaInfo(String areaname, Player player) {
		ClaimedResidence res = this.getByName(areaname);
		if(res == null) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
			return;
		}
		ResidencePermissions perms = res.getPermissions();
		if(Residence.getConfigManager().enableEconomy()) {
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Residence")+":"+ChatColor.DARK_GREEN+" " + areaname + " "+ChatColor.YELLOW+"Bank: "+ChatColor.GOLD + res.getBank().getStoredMoney());
		} else {
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Residence")+":"+ChatColor.DARK_GREEN+" " + areaname);
		}
		if(Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isRented(areaname)) {
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Owner")+":"+ChatColor.RED+" " + perms.getOwner() + ChatColor.YELLOW+" Rented by: "+ChatColor.RED + Residence.getRentManager().getRentingPlayer(areaname));
		} else {
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Owner")+":"+ChatColor.RED+" " + perms.getOwner() + ChatColor.YELLOW+" - " + Residence.getLanguage().getPhrase("World")+": "+ChatColor.RED+ res.getWorld());
		}
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Flags")+":"+ChatColor.BLUE+" " + perms.listFlags());
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Your.Flags")+": "+ChatColor.GREEN + perms.listPlayerFlags(player.getName()));
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Group.Flags")+":"+ChatColor.RED+" " + perms.listGroupFlags());
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Others.Flags")+":"+ChatColor.RED+" " + perms.listOtherPlayersFlags(player.getName()));
		String aid = res.getAreaIDbyLoc(player.getLocation());
		if(aid != null) {
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("CurrentArea")+": "+ChatColor.GOLD + aid);
		}
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Total.Size")+":"+ChatColor.LIGHT_PURPLE+" " + res.getTotalSize());
		if(aid != null){
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("CoordsT")+": "+ChatColor.LIGHT_PURPLE+Residence.getLanguage().getPhrase("CoordsTop",res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockX() + "." + res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockY() + "." + res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockZ()));
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("CoordsB")+": "+ChatColor.LIGHT_PURPLE+Residence.getLanguage().getPhrase("CoordsBottom",res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockX() + "." + res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockY() + "." + res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockZ()));
		}
		if (Residence.getConfigManager().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("LeaseExpire")+":"+ChatColor.GREEN+" " + Residence.getLeaseManager().getExpireTime(areaname));
		}
	}

	public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea, boolean resadmin) {
		ClaimedResidence reciever = this.getByName(targetArea);
		ClaimedResidence source = this.getByName(sourceArea);
		if (source == null || reciever == null) {
			reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
			return;
		}
		if (!resadmin) {
			if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
				reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return;
			}
		}
		reciever.getPermissions().applyTemplate(reqPlayer, source.getPermissions(), resadmin);
	}

	public boolean renameResidence(String oldName, String newName) {
		return this.renameResidence(null, oldName, newName, true);
	}

	public boolean renameResidence(Player player, String oldName, String newName, boolean resadmin) {
		if(!Residence.validName(newName)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
			return false;
		}
		ClaimedResidence res = this.getByName(oldName);
		if(res == null) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
			}
			return false;
		}
		if(res.getPermissions().hasResidencePermission(player, true) || resadmin) {
			if(res.getParent() == null) {
				if(sql.getResByName(newName) != null) {
					if(player != null) {
						player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceAlreadyExists",ChatColor.YELLOW + newName + ChatColor.RED));
					}
					return false;
				}
				ResidenceRenameEvent resevent = new ResidenceRenameEvent(res, newName, oldName);
				Residence.getServ().getPluginManager().callEvent(resevent);
				sql.renameResidence(newName, oldName);
				if(Residence.getConfigManager().useLeases()) {
					Residence.getLeaseManager().updateLeaseName(oldName, newName);
				}
				if(Residence.getConfigManager().enabledRentSystem()) {
					Residence.getRentManager().updateRentableName(oldName, newName);
				}
				if(player != null) {
					player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceRename",ChatColor.YELLOW + oldName + ChatColor.GREEN+"."+ChatColor.YELLOW + newName + ChatColor.GREEN));
				}
				return true;
			} else {
				String[] oldname = oldName.split("\\.");
				ClaimedResidence parent = res.getParent();
				return parent.renameSubzone(player, oldname[oldname.length-1], newName, resadmin);
			}
		} else {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			}
			return false;
		}
	}

	public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin) {
		ClaimedResidence res = getByName(residence);
		if(res == null) {
			reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
			return;
		}
		if(!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin) {
			reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			return;
		}
		Player giveplayer = Residence.getServ().getPlayer(targPlayer);
		if (giveplayer == null || !giveplayer.isOnline()) {
			reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NotOnline"));
			return;
		}
		CuboidArea[] areas = res.getAreaArray();
		PermissionGroup g = Residence.getPermissionManager().getGroup(giveplayer);
		if (areas.length > g.getMaxPhysicalPerResidence() && !resadmin) {
			reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
			return;
		}
		if (getOwnedZoneCount(giveplayer.getName()) >= g.getMaxZones() && !resadmin) {
			reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
			return;
		}
		if(!resadmin) {
			for (CuboidArea area : areas) {
				if (!g.inLimits(area)) {
					reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
					return;
				}
			}
		}
		res.getPermissions().setOwner(giveplayer.getName(), true);
		reqPlayer.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ResidenceGive",ChatColor.YELLOW + residence + ChatColor.GREEN+"."+ChatColor.YELLOW + giveplayer.getName() + ChatColor.GREEN));
		giveplayer.sendMessage(Residence.getLanguage().getPhrase("ResidenceRecieve",ChatColor.GREEN + residence + ChatColor.YELLOW+"."+ChatColor.GREEN + reqPlayer.getName() + ChatColor.YELLOW));
	}

	public void removeAllFromWorld(CommandSender sender, String world) {
		int count= sql.removeAllResidencesIn(world);
		if(count==0) {
			sender.sendMessage(ChatColor.RED+"No residences found in world: "+ChatColor.YELLOW + world);
		} else {
			sender.sendMessage(ChatColor.RED+"Removed "+ChatColor.YELLOW+count+ChatColor.RED+" residences in world: "+ChatColor.YELLOW + world);
		}
	}

	public int getResidenceCount() {
		return sql.getAllResNames().length;
	}
}

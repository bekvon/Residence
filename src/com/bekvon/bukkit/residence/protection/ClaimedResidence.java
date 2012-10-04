/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.itemlist.ItemList.ListType;
import com.bekvon.bukkit.residence.itemlist.ResidenceItemList;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.persistance.SQLManager;
import com.bekvon.bukkit.residence.text.help.InformationPager;

/**
 *
 * @author Administrator
 * 
 */
public class ClaimedResidence {

	protected ClaimedResidence parent;
	protected Set<String> areas;
	protected Set<String> subzones;
	protected ResidenceBank bank;
	protected Location tpLoc;
	protected String enterMessage;
	protected String leaveMessage;
	protected ResidenceItemList ignorelist;
	protected ResidenceItemList blacklist;
	protected SQLManager sql;
	protected int id;
	protected String name;
	protected String world;
	protected ResidencePermissions perms;
	protected String owner;

	private ClaimedResidence() {
		subzones = Collections.synchronizedSet(new HashSet<String>());
		areas = Collections.synchronizedSet(new HashSet<String>());
		bank = new ResidenceBank(this);
		blacklist = new ResidenceItemList(this, ListType.BLACKLIST);
		ignorelist = new ResidenceItemList(this, ListType.IGNORELIST);
		sql = Residence.getSQLManager();
	}

	public ClaimedResidence(String creator, String creationWorld) {
		this();
		owner = creator;
		perms = new ResidencePermissions(this, creator, creationWorld);
	}

	public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence) {
		this(creator, creationWorld);
		parent = parentResidence;
	}

	public ClaimedResidence(String resname, String owner, int resid, String enter, String leave, int Bank, String Blacklist, String IgnoreList, String[] Subzones, String[] Areas, Location tp, String world){
		name = resname;
		id = resid;
		enterMessage = enter;
		leaveMessage = leave;
		subzones = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(Subzones)));
		areas = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(Areas)));
		bank = new ResidenceBank(this);
		bank.setStoredMoney(Bank);
		blacklist = new ResidenceItemList(this, ListType.BLACKLIST);
		blacklist.load(this, ListType.BLACKLIST, Blacklist);
		ignorelist = new ResidenceItemList(this, ListType.IGNORELIST);
		ignorelist.load(this, ListType.IGNORELIST, IgnoreList);
		sql = Residence.getSQLManager();
		this.world = world;
		tpLoc = tp;
		this.owner = owner;
	}

	public boolean addArea(CuboidArea area, String name) {
		return addArea(null,area,name,true);
	}

	public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin) {
		if(!Residence.validName(name)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
			}
			return false;
		}
		if(areas.contains(name)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaExists"));
			}
			return false;
		}
		if (!area.getWorld().getName().equalsIgnoreCase(world)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaDiffWorld"));
			}
			return false;
		}
		if(parent == null) {
			String collideResidence = Residence.getResidenceManager().checkAreaCollision(area, this);
			if(collideResidence != null) {
				if(player!=null) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaCollision",ChatColor.YELLOW + collideResidence));
				}
				return false;
			}
		} else {
			String[] szs = parent.listSubzones();
			for(String sz : szs) {
				ClaimedResidence res = parent.getSubzone(sz);
				if(res != null && res != this) {
					if(res.checkCollision(area)) {
						if(player!=null) {
							player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaSubzoneCollision",ChatColor.YELLOW + sz));
						}
						return false;
					}
				}
			}
		}
		if(!resadmin && player != null) {
			if (!this.perms.hasResidencePermission(player, true)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return false;
			}
			if (parent != null) {
				if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaNotWithinParent"));
					return false;
				}
				if(!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(),"subzone", true)) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ParentNoPermission"));
					return false;
				}
			}
			PermissionGroup group = Residence.getPermissionManager().getGroup(player);
			if(!group.canCreateResidences() && !Residence.getPermissionManager().hasAuthority(player, "residence.create")) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return false;
			}
			if(areas.size()>=group.getMaxPhysicalPerResidence()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaMaxPhysical"));
				return false;
			}
			if(!group.inLimits(area)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaSizeLimit"));
				return false;
			}
			if(group.getMinHeight()>area.getLowLoc().getBlockY()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaLowLimit",ChatColor.YELLOW + String.format("%d",group.getMinHeight())));
				return false;
			}
			if(group.getMaxHeight()<area.getHighLoc().getBlockY()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaHighLimit",ChatColor.YELLOW + String.format("%d",group.getMaxHeight())));
				return false;
			}
			if(parent==null && Residence.getConfigManager().enableEconomy()) {
				int chargeamount = (int) Math.ceil((double)area.getSize() * group.getCostPerBlock());
				if(!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
					return false;
				}
			}
		}
		areas.add(name);
		sql.addArea(name, area, id);
		if(player!=null) {
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("AreaCreate",ChatColor.YELLOW + name));
		}
		return true;
	}

	public boolean replaceArea(CuboidArea neware, String name) {
		return this.replaceArea(null, neware, name, true);
	}

	public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {
		if (!areas.contains(name)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaNonExist"));
			}
			return false;
		}
		CuboidArea oldarea = sql.getArea(name, id);
		if (!newarea.getWorld().getName().equalsIgnoreCase(world)) {
			if(player!=null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaDiffWorld"));
			}
			return false;
		}
		if (parent == null) {
			String collideResidence = Residence.getResidenceManager().checkAreaCollision(newarea, this);
			if (collideResidence != null) {
				if(player!=null) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaCollision",ChatColor.YELLOW + collideResidence));
				}
				return false;
			}
		} else {
			String[] szs = parent.listSubzones();
			for (String sz : szs) {
				ClaimedResidence res = parent.getSubzone(sz);
				if (res != null && res != this) {
					if (res.checkCollision(newarea)) {
						if(player!=null) {
							player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaSubzoneCollision",ChatColor.YELLOW + sz));
						}
						return false;
					}
				}
			}
		}
		if (!resadmin && player!=null) {
			if (!this.perms.hasResidencePermission(player, true)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return false;
			}
			if (parent != null) {
				if (!parent.containsLoc(newarea.getHighLoc()) || !parent.containsLoc(newarea.getLowLoc())) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaNotWithinParent"));
					return false;
				}
				if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ParentNoPermission"));
					return false;
				}
			}
			PermissionGroup group = Residence.getPermissionManager().getGroup(player);
			if (!group.canCreateResidences() && !Residence.getPermissionManager().hasAuthority(player, "residence.create")) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return false;
			}
			if (!group.inLimits(newarea)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaSizeLimit"));
				return false;
			}
			if (group.getMinHeight() > newarea.getLowLoc().getBlockY()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaLowLimit",ChatColor.YELLOW + String.format("%d",group.getMinHeight())));
				return false;
			}
			if (group.getMaxHeight() < newarea.getHighLoc().getBlockY()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaHighLimit",ChatColor.YELLOW + String.format("%d",group.getMaxHeight())));
				return false;
			}
			if (parent == null && Residence.getConfigManager().enableEconomy()) {
				int chargeamount = (int) Math.ceil((double) (newarea.getSize()-oldarea.getSize()) * group.getCostPerBlock());
				if(chargeamount>0)
				{
					if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
						return false;
					}
				}
			}

		}
		areas.remove(name);
		sql.removeArea(name, id);
		sql.addArea(name, newarea, id);
		areas.add(name);
		player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("AreaUpdate"));
		return true;
	}

	public boolean addSubzone(String name, Location loc1, Location loc2) {
		return this.addSubzone(null, loc1, loc2, name, true);
	}

	public boolean addSubzone(Player player, Location loc1, Location loc2, String name, boolean resadmin) {
		if(!Residence.validName(name)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
			}
			return false;
		}
		if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SubzoneSelectInside"));
			}
			return false;
		}
		if (subzones.contains(name)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SubzoneExists",ChatColor.YELLOW + name));
			}
			return false;
		}
		if(!resadmin && player!=null) {
			if (!this.perms.hasResidencePermission(player, true)) {
				if(!this.perms.playerHas(player.getName(), "subzone", false)) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
					return false;
				}
			}
			PermissionGroup group = Residence.getPermissionManager().getGroup(player);
			if(this.getZoneDepth()>=group.getMaxSubzoneDepth()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SubzoneMaxDepth"));
				return false;
			}
		}
		CuboidArea newArea = new CuboidArea(loc1, loc2, name);
		synchronized (subzones) {
			for (String subzone : subzones) {
				ClaimedResidence res = sql.getSubzoneByName(subzone, id);
				if (res.checkCollision(newArea)) {
					if(player!=null) {
						player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SubzoneCollide",ChatColor.YELLOW + subzone));
					}
					return false;
				}
			}
		}
		ClaimedResidence newres = new ClaimedResidence(player.getName(), world, this);
		newres.addArea(player, newArea, name, resadmin);
		if(newres.getAreaCount() != 0) {
			newres.getPermissions().applyDefaultFlags();
			PermissionGroup group = Residence.getPermissionManager().getGroup(player);
			newres.setEnterMessage(group.getDefaultEnterMessage());
			newres.setLeaveMessage(group.getDefaultLeaveMessage());
			if(Residence.getConfigManager().flagsInherit()) {
				newres.getPermissions().setParent(perms);
			}
			subzones.add(name);
			sql.addSubzone(name, newres, id);
			if(player != null) {
				player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SubzoneCreate",ChatColor.YELLOW + name));
			}
			return true;
		} else {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SubzoneCreateFail",ChatColor.YELLOW + name));
			}
			return false;
		}
	}
	public int getId(){
		return id;
	}
	public String getSubzoneNameByLoc(Location loc) {
		ClaimedResidence res = this.getSubzoneByLoc(loc);
		if(res==null){
			return null;
		}
		return this.getName()+res.getName();
	}

	public ClaimedResidence getSubzoneByLoc(Location loc) {
		ClaimedResidence res = sql.getSubzoneByLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), id);
		if (res==null) {
			return null;
		}
		ClaimedResidence subres = res.getSubzoneByLoc(loc);
		if (subres == null) {
			return res;
		}
		return subres;
	}

	public ClaimedResidence getSubzone(String subzonename) {
		if (!subzonename.contains(".")) {
			return sql.getSubzoneByName(subzonename, id);
		}
		String split[] = subzonename.split("\\.");
		ClaimedResidence get = sql.getSubzoneByName(subzonename, id);
		for (int i = 1; i < split.length; i++) {
			if (get == null) {
				return null;
			}
			get = sql.getSubzoneByName(split[i], get.getId());
		}
		return get;
	}

	public String getSubzoneNameByRes(ClaimedResidence res) {
		Set<String> set = subzones;
		for(String entry : set) {
			if(sql.getSubzoneByName(entry, id) == res) {
				return res.getName();
			}
			String n = sql.getSubzoneByName(entry, id).getSubzoneNameByRes(res);
			if(n!=null) {
				return res.getName() + "." + n;
			}
		}
		return null;
	}

	public String[] getSubzoneList() {
		return (String[]) subzones.toArray();
	}

	public boolean checkCollision(CuboidArea area) {
		Set<String> set = areas;
		for (String key : set) {
			CuboidArea checkarea = sql.getArea(key, id);
			if (checkarea != null) {
				if (checkarea.checkCollision(area)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsLoc(Location loc) {
		return sql.isInArea(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), id);
	}

	public ClaimedResidence getParent() {
		return parent;
	}

	public ClaimedResidence getTopParent() {
		if(parent==null) {
			return this;
		}
		return parent.getTopParent();
	}

	public boolean removeSubzone(String name) {
		return this.removeSubzone(null, name, true);
	}

	public boolean removeSubzone(Player player, String name, boolean resadmin) {
		ClaimedResidence res = sql.getSubzoneByName(name, id);
		if (player!=null && !res.perms.hasResidencePermission(player, true) && !resadmin) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			return false;
		}
		sql.removeSubzone(name, id);
		subzones.remove(name);
		if(player!=null) {
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SubzoneRemove",ChatColor.YELLOW+name+ChatColor.GREEN));
		}
		return true;
	}

	public long getTotalSize() {
		Collection<CuboidArea> set = sql.getAreas(id);
		long size = 0;
		for (CuboidArea entry : set) {
			size = size + entry.getSize();
		}
		return size;
	}

	public CuboidArea[] getAreaArray() {
		return (CuboidArea[]) sql.getAreas(id).toArray();
	}

	public ResidencePermissions getPermissions() {
		return perms;
	}

	public String getEnterMessage() {
		return enterMessage;
	}

	public String getLeaveMessage() {
		return leaveMessage;
	}

	public void setEnterMessage(String message) {
		enterMessage = message;
	}

	public void setLeaveMessage(String message) {
		leaveMessage = message;
	}

	public void setEnterLeaveMessage(Player player, String message, boolean enter, boolean resadmin) {
		if(message != null && Residence.getConfigManager().getResidenceNameRegex() != null && !Residence.validString(message)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidCharacters"));
			return;
		}
		if(message != null) {
			if(message.equals("")) {
				message = null;
			}
		}
		PermissionGroup group = Residence.getPermissionManager().getGroup(perms.getOwner(), world);
		if(!group.canSetEnterLeaveMessages() && !resadmin) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("OwnerNoPermission"));
			return;
		}
		if(!perms.hasResidencePermission(player, false) && !resadmin) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			return;
		}
		if(enter) {
			this.setEnterMessage(message);
		} else {
			this.setLeaveMessage(message);
		}
		player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("MessageChange"));
	}

	public Location getOutsideFreeLoc(Location insideLoc) {
		int maxIt = 100;
		CuboidArea area = this.getAreaByLoc(insideLoc);
		if(area == null) {
			return insideLoc;
		}
		Location highLoc = area.getHighLoc();
		Location newLoc = new Location(highLoc.getWorld(), highLoc.getBlockX(), highLoc.getBlockY(), highLoc.getBlockZ());
		boolean found = false;
		int it = 0;
		while (!found && it < maxIt) {
			it++;
			Location lowLoc;
			newLoc.setX(newLoc.getBlockX() + 1);
			newLoc.setZ(newLoc.getBlockZ() + 1);
			newLoc.setY(255);
			if(newLoc.getWorld().getEnvironment() == Environment.NETHER){
				newLoc.setY(newLoc.getWorld().getHighestBlockAt(newLoc).getY());
			}
			lowLoc = new Location(newLoc.getWorld(), newLoc.getBlockX(), newLoc.getY()-1, newLoc.getBlockZ());
			while ((newLoc.getBlock().getTypeId() != 0 || lowLoc.getBlock().getTypeId() == 0) && lowLoc.getBlockY() > -126) {
				newLoc.setY(newLoc.getY() - 1);
				lowLoc.setY(lowLoc.getY() - 1);
			}
			if (newLoc.getBlock().getTypeId() == 0 && lowLoc.getBlock().getTypeId() != 0) {
				found = true;
			}
		}
		if(found) {
			return newLoc;
		} else {
			World theworld = Residence.getServ().getWorld(world);
			if(theworld!=null) {
				return theworld.getSpawnLocation();
			}
			return insideLoc;
		}
	}

	protected CuboidArea getAreaByLoc(Location loc) {
		return sql.getAreaByLoc(loc, id);
	}

	public String[] listSubzones() {
		return (String[]) subzones.toArray();
	}

	public void printSubzoneList(Player player, int page) {
		ArrayList<String> temp = new ArrayList<String>();
		for(String sz : subzones) {
			temp.add(ChatColor.GREEN+ sz + ChatColor.YELLOW+" - " +Residence.getLanguage().getPhrase("Owner")+": " + sql.getSubzoneByName(sz, id).getOwner());
		}
		InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Subzones"), temp, page);
	}

	public void printAreaList(Player player, int page) {
		ArrayList<String> temp = new ArrayList<String>(areas);
		InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
	}

	public void printAdvancedAreaList(Player player, int page) {
		ArrayList<String> temp = new ArrayList<String>();
		for(String entry : areas) {
			CuboidArea a = sql.getArea(entry, id);
			Location h = a.getHighLoc();
			Location l = a.getLowLoc();
			temp.add(ChatColor.GREEN+"{"+ChatColor.YELLOW+"ID:"+ChatColor.RED+entry+" "+ChatColor.YELLOW+"P1:"+ChatColor.RED+"("+h.getBlockX()+","+h.getBlockY()+","+h.getBlockZ()+") "+ChatColor.YELLOW+"P2:"+ChatColor.RED+"("+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ()+") "+ChatColor.YELLOW+"(Size:"+ChatColor.RED + a.getSize() + ChatColor.YELLOW+")"+ChatColor.GREEN+"} ");
		}
		InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
	}

	public String[] getAreaList() {
		return (String[]) areas.toArray();
	}

	public int getZoneDepth() {
		int count = 0;
		ClaimedResidence res = parent;
		while(res!=null) {
			count++;
			res = res.getParent();
		}
		return count;
	}

	public void setTpLoc(Player player, boolean resadmin) {
		if(!this.perms.hasResidencePermission(player, false) && !resadmin) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			return;
		}
		Location loc = player.getLocation();
		if(!this.containsLoc(loc)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NotInResidence"));
			return;
		}
		tpLoc = loc;
		sql.setTpLoc(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(), loc.getWorld(), id);
		player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SetTeleportLocation"));
	}
	public Location getTPLoc(){
		return tpLoc;
	}
	public void tpToResidence(Player reqPlayer, Player targetPlayer, boolean resadmin) {
		if (!resadmin) {
			PermissionGroup group = Residence.getPermissionManager().getGroup(reqPlayer);
			if (!group.hasTpAccess()) {
				reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("TeleportDeny"));
				return;
			}
			if (!reqPlayer.equals(targetPlayer)) {
				reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return;
			}
			if (!this.perms.playerHas(reqPlayer.getName(), "tp", true)) {
				reqPlayer.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("TeleportNoFlag"));
				return;
			}
		}
		if (tpLoc != null) {
			ResidenceTPEvent tpevent = new ResidenceTPEvent(this,tpLoc, targetPlayer, reqPlayer);
			Residence.getServ().getPluginManager().callEvent(tpevent);
			if(!tpevent.isCancelled()) {
				targetPlayer.teleport(tpLoc);
				targetPlayer.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("TeleportSuccess"));
			}
		} else {
			CuboidArea area = sql.getAreas(id).iterator().next();
			if (area == null) {
				reqPlayer.sendMessage(ChatColor.RED+"Could not find area to teleport to...");
				return;
			}
			Location targloc = this.getOutsideFreeLoc(area.getHighLoc());
			ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
			Residence.getServ().getPluginManager().callEvent(tpevent);
			if(!tpevent.isCancelled()) {
				targetPlayer.teleport(targloc);
				targetPlayer.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("TeleportNear"));
			}
		}
	}


	public String getAreaIDbyLoc(Location loc) {
		CuboidArea area = sql.getAreaByLoc(loc, id);
		if(area!=null){
			return area.getId();
		}
		return null;
	}

	public void removeArea(String areaid) {
		sql.removeArea(areaid, id);
		areas.remove(areaid);
	}

	public void removeArea(Player player, String areaid, boolean resadmin) {

		if(this.getPermissions().hasResidencePermission(player, true) || resadmin) {
			if(!areas.contains(areaid)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaNonExist"));
				return;
			}
			if(areas.size()==1 && !Residence.getConfigManager().allowEmptyResidences()) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaRemoveLast"));
				return;
			}
			removeArea(areaid);
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("AreaRemove"));
		} else {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
		}
	}

	public int getAreaCount() {
		return areas.size();
	}

	public boolean renameSubzone(String oldName, String newName) {
		return this.renameSubzone(null, oldName, newName, true);
	}

	public boolean renameSubzone(Player player, String oldName, String newName, boolean resadmin) {
		if(!Residence.validName(newName)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
			return false;
		}
		ClaimedResidence res = sql.getSubzoneByName(oldName, id);
		if(res == null) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidSubzone"));
			}
			return false;
		}
		if(player != null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
			if(player != null) {
				player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			}
			return false;
		}
		if(subzones.contains(newName)) {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SubzoneExists",ChatColor.YELLOW+newName));
			}
			return false;
		}
		sql.renameSubzone(oldName, newName, id);
		subzones.remove(oldName);
		subzones.add(newName);
		if(player!=null) {
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SubzoneRename",oldName+"."+newName));
		}
		return true;
	}

	public boolean renameArea(String oldName, String newName) {
		return this.renameArea(null, oldName, newName, true);
	}

	public boolean renameArea(Player player, String oldName, String newName, boolean resadmin) {
		if(!Residence.validName(newName)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidNameCharacters"));
			return false;
		}
		if(player == null || perms.hasResidencePermission(player, true) || resadmin) {
			if(areas.contains(newName)) {
				if(player != null) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaExists"));
				}
				return false;
			}
			CuboidArea area = sql.getArea(oldName, id);
			if(area == null) {
				if(player != null) {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("AreaInvalidName"));
				}
				return false;
			}
			sql.renameArea(oldName, newName, id);
			areas.remove(oldName);
			areas.add(newName);
			if(player != null) {
				player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("AreaRename", oldName+"."+newName));
			}
			return true;
		} else {
			if(player != null) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
			}
			return false;
		}
	}

	public CuboidArea getArea(String name) {
		return sql.getArea(name, id);
	}

	public String getName() {
		return name;
	}

	public void remove() {
		String name = getName();
		if(name != null) {
			Residence.getResidenceManager().removeResidence(name);
		}
	}

	public ResidenceBank getBank() {
		return bank;
	}

	public String getWorld() {
		return world;
	}

	public String getOwner() {
		return owner;
	}

	public ResidenceItemList getItemBlacklist() {
		return blacklist;
	}

	public ResidenceItemList getItemIgnoreList() {
		return ignorelist;
	}

	public ArrayList<Player> getPlayersInResidence() {
		ArrayList<Player> within = new ArrayList<Player>();
		Player[] players = Residence.getServ().getOnlinePlayers();
		for(Player player : players) {
			if(this.containsLoc(player.getLocation())) {
				within.add(player);
			}
		}
		return within;
	}
}

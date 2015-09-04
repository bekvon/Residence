/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.Debug;

/**
 * 
 * @author Administrator
 */
public class ResidenceManager {
    protected Map<String, ClaimedResidence> residences;
    protected Map<String, Map<ChunkRef, List<String>>> chunkResidences;

    public ResidenceManager() {
	residences = new HashMap<>();
	chunkResidences = new HashMap<>();
    }

    public ClaimedResidence getByLoc(Location loc) {
	if (loc == null)
	    return null;
	ClaimedResidence res = null;
	String world = loc.getWorld().getName();
	ChunkRef chunk = new ChunkRef(loc);
	//if (chunkResidences.containsKey(world)) {

	Map<ChunkRef, List<String>> ChunkMap = chunkResidences.get(world);

	if (ChunkMap.containsKey(chunk)) {
	    for (String key : ChunkMap.get(chunk)) {
		ClaimedResidence entry = residences.get(key);
		if (entry.containsLoc(loc)) {
		    res = entry;
		    break;
		}
	    }
	}
	//}
	if (res == null)
	    return null;

	ClaimedResidence subres = res.getSubzoneByLoc(loc);
	if (subres == null)
	    return res;
	return subres;
    }

    public ClaimedResidence getByName(String name) {
	if (name == null) {
	    return null;
	}
	String[] split = name.split("\\.");
	if (split.length == 1) {
	    return residences.get(name);
	}
	ClaimedResidence res = residences.get(split[0]);
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
	ClaimedResidence res = this.getByLoc(loc);
	if (res == null)
	    return null;
	String name = res.getName();
	if (name == null)
	    return null;
	String szname = res.getSubzoneNameByLoc(loc);
	if (szname != null)
	    return name + "." + szname;
	return name;
    }

    public String getNameByRes(ClaimedResidence res) {
	Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
	for (Entry<String, ClaimedResidence> check : set) {
	    if (check.getValue() == res) {
		return check.getKey();
	    }
	    String n = check.getValue().getSubzoneNameByRes(res);
	    if (n != null) {
		return check.getKey() + "." + n;
	    }
	}
	return null;
    }

    public boolean addResidence(String name, Location loc1, Location loc2) {
	return this.addResidence(name, "Server Land", loc1, loc2);
    }

    public boolean addResidence(String name, String owner, Location loc1, Location loc2) {
	return this.addResidence(null, owner, name, loc1, loc2, true);
    }

    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin) {
	return this.addResidence(player, player.getName(), name, loc1, loc2, resadmin);
    }

    public boolean addResidence(Player player, String owner, String name, Location loc1, Location loc2, boolean resadmin) {

	long time = System.currentTimeMillis();
	if (!Residence.validName(name)) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
	    }
	    return false;
	}
	if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	    }
	    return false;
	}
	Debug.D("1 " + (System.currentTimeMillis() - time));
	PermissionGroup group = Residence.getPermissionManager().getGroup(owner, loc1.getWorld().getName());
	boolean createpermission = group.canCreateResidences() || (player == null ? true : player.hasPermission("residence.create"));
	if (!createpermission && !resadmin) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    }
	    return false;
	}
	Debug.D("2 " + (System.currentTimeMillis() - time));
	if (player != null) {
	    if (!hasMaxZones(player.getName(), group.getMaxZones()) && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceTooMany"));
		return false;
	    }
	}
	Debug.D("3 " + (System.currentTimeMillis() - time));
	CuboidArea newArea = new CuboidArea(loc1, loc2);
	ClaimedResidence newRes = new ClaimedResidence(owner, loc1.getWorld().getName());
	newRes.getPermissions().applyDefaultFlags();
	newRes.setEnterMessage(group.getDefaultEnterMessage());
	newRes.setLeaveMessage(group.getDefaultLeaveMessage());

	ResidenceCreationEvent resevent = new ResidenceCreationEvent(player, name, newRes, newArea);
	Residence.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled()) {
	    return false;
	}
	newArea = resevent.getPhysicalArea();
	name = resevent.getResidenceName();
	if (residences.containsKey(name)) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceAlreadyExists", ChatColor.YELLOW + name + ChatColor.RED));
	    }
	    return false;
	}
	newRes.BlockSellPrice = group.getSellPerBlock();
	if (player != null) {
	    newRes.addArea(player, newArea, "main", resadmin);
	} else {
	    newRes.addArea(newArea, "main");
	}
	if (newRes.getAreaCount() != 0) {
	    residences.put(name, newRes);
	    calculateChunks(name);
	    Residence.getLeaseManager().removeExpireTime(name);
	    if (player != null) {
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceCreate", ChatColor.YELLOW + name + ChatColor.GREEN));
	    }
	    if (Residence.getConfigManager().useLeases()) {
		if (player != null) {
		    Residence.getLeaseManager().setExpireTime(player, name, group.getLeaseGiveTime());
		} else {
		    Residence.getLeaseManager().setExpireTime(name, group.getLeaseGiveTime());
		}
	    }
	    return true;
	}
	return false;
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
	this.listResidences(player, targetplayer, page, showhidden, false);
    }

    public void listResidences(Player player, String targetplayer, int page, boolean showhidden, boolean showsubzones) {
	if (showhidden && !Residence.isResAdminOn(player) && !player.getName().equals(targetplayer)) {
	    showhidden = false;
	}
	InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences") + " - " + targetplayer, this.getResidenceList(targetplayer, showhidden,
	    showsubzones, true), page);
    }

    public void listAllResidences(Player player, int page) {
	this.listAllResidences(player, page, false);
    }

    public void listAllResidences(Player player, int page, boolean showhidden) {
	this.listAllResidences(player, page, showhidden, false);
    }

    public void listAllResidences(Player player, int page, boolean showhidden, boolean showsubzones) {
	if (showhidden && !Residence.isResAdminOn(player)) {
	    showhidden = false;
	}
	InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Residences"), this.getResidenceList(null, showhidden, showsubzones, true), page);
    }

    public String[] getResidenceList() {
	return this.getResidenceList(true, true).toArray(new String[0]);
    }

    public ArrayList<String> getResidenceList(boolean showhidden, boolean showsubzones) {
	return this.getResidenceList(null, showhidden, showsubzones, false);
    }

    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones) {
	return this.getResidenceList(targetplayer, showhidden, showsubzones, false);
    }

    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, boolean formattedOutput) {
	ArrayList<String> list = new ArrayList<>();
	for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
	    this.getResidenceList(targetplayer, showhidden, showsubzones, "", res.getKey(), res.getValue(), list, formattedOutput);
	}
	return list;
    }

    private void getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, String parentzone, String resname, ClaimedResidence res,
	ArrayList<String> list, boolean formattedOutput) {
	boolean hidden = res.getPermissions().has("hidden", false);
	if ((showhidden) || (!showhidden && !hidden)) {
	    if (targetplayer == null || res.getPermissions().getOwner().equalsIgnoreCase(targetplayer)) {
		if (formattedOutput) {
		    list.add(ChatColor.GREEN + parentzone + resname + ChatColor.YELLOW + " - " + Residence.getLanguage().getPhrase("World") + ": " + res.getWorld());
		} else {
		    list.add(parentzone + resname);
		}
	    }
	    if (showsubzones) {
		for (Entry<String, ClaimedResidence> sz : res.subzones.entrySet()) {
		    this.getResidenceList(targetplayer, showhidden, showsubzones, parentzone + resname + ".", sz.getKey(), sz.getValue(), list, formattedOutput);
		}
	    }
	}
    }

    public String checkAreaCollision(CuboidArea newarea, ClaimedResidence parentResidence) {
	Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
	for (Entry<String, ClaimedResidence> entry : set) {
	    ClaimedResidence check = entry.getValue();
	    if (check != parentResidence && check.checkCollision(newarea)) {
		return entry.getKey();
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
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return;
		}
	    }
	    ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, player == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
	    Residence.getServ().getPluginManager().callEvent(resevent);
	    if (resevent.isCancelled()) {
		return;
	    }
	    ClaimedResidence parent = res.getParent();
	    if (parent == null) {
		removeChunkList(name);
		residences.remove(name);
		if (player != null) {
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceRemove", ChatColor.YELLOW + name + ChatColor.GREEN));
		}
	    } else {
		String[] split = name.split("\\.");
		if (player != null) {
		    parent.removeSubzone(player, split[split.length - 1], true);
		} else {
		    parent.removeSubzone(split[split.length - 1]);
		}
	    }
	    // Residence.getLeaseManager().removeExpireTime(name); - causing
	    // concurrent modification exception in lease manager... worked
	    // around for now
	    Residence.getRentManager().removeRentable(name);
	    if (parent == null && Residence.getConfigManager().enableEconomy() && Residence.getConfigManager().useResMoneyBack()) {
		int chargeamount = (int) Math.ceil((double) res.getAreaArray()[0].getSize() * res.getBlockSellPrice());
		TransactionManager.giveEconomyMoney(player, chargeamount);
	    }
	} else {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	}
    }

    public void removeAllByOwner(String owner) {
	this.removeAllByOwner(null, owner, residences);
    }

    public void removeAllByOwner(Player player, String owner) {
	this.removeAllByOwner(player, owner, residences);
    }

    private void removeAllByOwner(Player player, String owner, Map<String, ClaimedResidence> resholder) {
	Iterator<ClaimedResidence> it = resholder.values().iterator();
	while (it.hasNext()) {
	    ClaimedResidence res = it.next();
	    if (res.getOwner().equalsIgnoreCase(owner)) {
		ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, player == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
		Residence.getServ().getPluginManager().callEvent(resevent);
		if (resevent.isCancelled())
		    return;
		removeChunkList(res.getName());
		it.remove();
	    } else {
		this.removeAllByOwner(player, owner, res.subzones);
	    }
	}
    }

    public int getOwnedZoneCount(String player) {
	Collection<ClaimedResidence> set = residences.values();
	int count = 0;
	for (ClaimedResidence res : set) {
	    if (res.getPermissions().getOwner().equalsIgnoreCase(player)) {
		count++;
	    }
	}
	return count;
    }

    public boolean hasMaxZones(String player, int target) {
	Collection<ClaimedResidence> set = residences.values();
	int count = 0;
	for (ClaimedResidence res : set) {
	    if (res.getPermissions().getOwner().equalsIgnoreCase(player)) {
		count++;
		if (count >= target)
		    return false;
	    }
	}
	return true;
    }

    public void printAreaInfo(String areaname, Player player) {
	ClaimedResidence res = this.getByName(areaname);
	if (res == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	ResidencePermissions perms = res.getPermissions();
	if (Residence.getConfigManager().enableEconomy()) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Residence") + ":" + ChatColor.DARK_GREEN + " " + areaname + " " + ChatColor.YELLOW
		+ "Bank: " + ChatColor.GOLD + res.getBank().getStoredMoney());
	} else {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Residence") + ":" + ChatColor.DARK_GREEN + " " + areaname);
	}
	if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isRented(areaname)) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Owner") + ":" + ChatColor.RED + " " + perms.getOwner() + ChatColor.YELLOW
		+ " Rented by: " + ChatColor.RED + Residence.getRentManager().getRentingPlayer(areaname));
	} else {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Owner") + ":" + ChatColor.RED + " " + perms.getOwner() + ChatColor.YELLOW + " - "
		+ Residence.getLanguage().getPhrase("World") + ": " + ChatColor.RED + perms.getWorld());
	}
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Flags") + ":" + ChatColor.BLUE + " " + perms.listFlags());
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Your.Flags") + ": " + ChatColor.GREEN + perms.listPlayerFlags(player.getName()));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Group.Flags") + ":" + ChatColor.RED + " " + perms.listGroupFlags());
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Others.Flags") + ":" + ChatColor.RED + " " + perms.listOtherPlayersFlags(player
	    .getName()));
	String aid = res.getAreaIDbyLoc(player.getLocation());
	if (aid != null) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("CurrentArea") + ": " + ChatColor.GOLD + aid);
	}
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Total.Size") + ":" + ChatColor.LIGHT_PURPLE + " " + res.getTotalSize());

	if (Residence.getEconomyManager() != null) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(res.getOwner(), res.getWorld());
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("TotalWorth", String.valueOf((res.getTotalSize() * group.getCostPerBlock())).replace(
		".", ",") + "." +
		String.valueOf(res.getTotalSize() * res.getBlockSellPrice()).replace(".", ",")));
	}

	if (aid != null) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("CoordsT") + ": " + ChatColor.LIGHT_PURPLE + Residence.getLanguage().getPhrase(
		"CoordsTop", res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockX() + "." + res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockY() + "."
		    + res.getAreaByLoc(player.getLocation()).getHighLoc().getBlockZ()));
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("CoordsB") + ": " + ChatColor.LIGHT_PURPLE + Residence.getLanguage().getPhrase(
		"CoordsBottom", res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockX() + "." + res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockY()
		    + "." + res.getAreaByLoc(player.getLocation()).getLowLoc().getBlockZ()));
	}
	if (Residence.getConfigManager().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LeaseExpire") + ":" + ChatColor.GREEN + " " + Residence.getLeaseManager()
		.getExpireTime(areaname));
	}
    }

    public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea, boolean resadmin) {
	ClaimedResidence reciever = this.getByName(targetArea);
	ClaimedResidence source = this.getByName(sourceArea);
	if (source == null || reciever == null) {
	    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	if (!resadmin) {
	    if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
		reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return;
	    }
	}
	reciever.getPermissions().applyTemplate(reqPlayer, source.getPermissions(), resadmin);
    }

    public Map<String, Object> save() {
	Map<String, Object> worldmap = new LinkedHashMap<>();
	for (World world : Residence.getServ().getWorlds()) {
	    Map<String, Object> resmap = new LinkedHashMap<>();
	    for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
		if (res.getValue().getWorld().equals(world.getName())) {
		    try {
			resmap.put(res.getKey(), res.getValue().save());
		    } catch (Exception ex) {
			System.out.println("[Residence] Failed to save residence (" + res.getKey() + ")!");
			Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    }
	    worldmap.put(world.getName(), resmap);
	}
	return worldmap;
    }

    public static ResidenceManager load(Map<String, Object> root) throws Exception {
	ResidenceManager resm = new ResidenceManager();
	if (root == null)
	    return resm;

	for (World world : Residence.getServ().getWorlds()) {
	    @SuppressWarnings("unchecked")
	    Map<String, Object> reslist = (Map<String, Object>) root.get(world.getName());
	    if (reslist != null) {
		try {
		    resm.chunkResidences.put(world.getName(), loadMap(world.getName(), reslist, resm));
		} catch (Exception ex) {
		    System.out.println("Error in loading save file for world: " + world.getName());
		    if (Residence.getConfigManager().stopOnSaveError())
			throw (ex);
		}
	    }
	}
	return resm;
    }

    public static Map<ChunkRef, List<String>> loadMap(String worldName, Map<String, Object> root, ResidenceManager resm) throws Exception {
	Map<ChunkRef, List<String>> retRes = new HashMap<>();
	if (root != null) {
	    for (Entry<String, Object> res : root.entrySet()) {

		try {
		    @SuppressWarnings("unchecked")
		    ClaimedResidence residence = ClaimedResidence.load((Map<String, Object>) res.getValue(), null);
		    if (residence.getPermissions().getOwnerUUID().toString().equals("ffffffff-ffff-ffff-ffff-ffffffffffff"))
			continue;

		    for (ChunkRef chunk : getChunks(residence)) {
			List<String> ress = new ArrayList<>();
			if (retRes.containsKey(chunk)) {
			    ress.addAll(retRes.get(chunk));
			}
			ress.add(res.getKey());
			retRes.put(chunk, ress);
		    }

		    resm.residences.put(res.getKey(), residence);
		} catch (Exception ex) {
		    System.out.print("[Residence] Failed to load residence (" + res.getKey() + ")! Reason:" + ex.getMessage() + " Error Log:");
		    Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
		    if (Residence.getConfigManager().stopOnSaveError()) {
			throw (ex);
		    }
		}
	    }
	}
	return retRes;
    }

    private static List<ChunkRef> getChunks(ClaimedResidence res) {
	List<ChunkRef> chunks = new ArrayList<>();
	for (CuboidArea area : res.getAreaArray()) {
	    chunks.addAll(area.getChunks());
	}
	return chunks;
    }

    public boolean renameResidence(String oldName, String newName) {
	return this.renameResidence(null, oldName, newName, true);
    }

    public boolean renameResidence(Player player, String oldName, String newName, boolean resadmin) {
	if (!Residence.validName(newName)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
	    return false;
	}
	ClaimedResidence res = this.getByName(oldName);
	if (res == null) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return false;
	}
	if (res.getPermissions().hasResidencePermission(player, true) || resadmin) {
	    if (res.getParent() == null) {
		if (residences.containsKey(newName)) {
		    if (player != null)
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceAlreadyExists", ChatColor.YELLOW + newName + ChatColor.RED));
		    return false;
		}
		ResidenceRenameEvent resevent = new ResidenceRenameEvent(res, newName, oldName);
		Residence.getServ().getPluginManager().callEvent(resevent);
		removeChunkList(oldName);
		residences.put(newName, res);
		residences.remove(oldName);
		calculateChunks(newName);
		if (Residence.getConfigManager().useLeases()) {
		    Residence.getLeaseManager().updateLeaseName(oldName, newName);
		}
		if (Residence.getConfigManager().enabledRentSystem()) {
		    Residence.getRentManager().updateRentableName(oldName, newName);
		}
		if (player != null) {
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceRename", ChatColor.YELLOW + oldName + ChatColor.GREEN + "."
			+ ChatColor.YELLOW + newName + ChatColor.GREEN));
		}
		return true;
	    } else {
		String[] oldname = oldName.split("\\.");
		ClaimedResidence parent = res.getParent();
		return parent.renameSubzone(player, oldname[oldname.length - 1], newName, resadmin);
	    }
	} else {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    }
	    return false;
	}
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin) {
	ClaimedResidence res = getByName(residence);
	if (res == null) {
	    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	if (!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin) {
	    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return;
	}
	Player giveplayer = Residence.getServ().getPlayer(targPlayer);
	if (giveplayer == null || !giveplayer.isOnline()) {
	    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotOnline"));
	    return;
	}
	CuboidArea[] areas = res.getAreaArray();
	PermissionGroup g = Residence.getPermissionManager().getGroup(giveplayer);
	if (areas.length > g.getMaxPhysicalPerResidence() && !resadmin) {
	    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
	    return;
	}
	if (!hasMaxZones(giveplayer.getName(), g.getMaxZones()) && !resadmin) {
	    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
	    return;
	}
	if (!resadmin) {
	    for (CuboidArea area : areas) {
		if (!g.inLimits(area)) {
		    reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceGiveLimits"));
		    return;
		}
	    }
	}
	res.getPermissions().setOwner(giveplayer.getName(), true);
	// Fix phrases here
	reqPlayer.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceGive", ChatColor.YELLOW + residence + ChatColor.GREEN + "." + ChatColor.YELLOW
	    + giveplayer.getName() + ChatColor.GREEN));
	giveplayer.sendMessage(Residence.getLanguage().getPhrase("ResidenceRecieve", ChatColor.GREEN + residence + ChatColor.YELLOW + "." + ChatColor.GREEN + reqPlayer
	    .getName() + ChatColor.YELLOW));
    }

    public void removeAllFromWorld(CommandSender sender, String world) {
	int count = 0;
	Iterator<ClaimedResidence> it = residences.values().iterator();
	while (it.hasNext()) {
	    ClaimedResidence next = it.next();
	    if (next.getWorld().equals(world)) {
		it.remove();
		count++;
	    }
	}
	chunkResidences.remove(world);
	chunkResidences.put(world, new HashMap<ChunkRef, List<String>>());
	if (count == 0) {
	    sender.sendMessage(ChatColor.RED + "No residences found in world: " + ChatColor.YELLOW + world);
	} else {
	    sender.sendMessage(ChatColor.RED + "Removed " + ChatColor.YELLOW + count + ChatColor.RED + " residences in world: " + ChatColor.YELLOW + world);
	}
    }

    public int getResidenceCount() {
	return residences.size();
    }

    public void removeChunkList(String name) {
	ClaimedResidence res = residences.get(name);
	if (res != null) {
	    String world = res.getWorld();
	    if (chunkResidences.get(world) != null) {
		for (ChunkRef chunk : getChunks(res)) {
		    List<String> ress = new ArrayList<>();
		    if (chunkResidences.get(world).containsKey(chunk)) {
			ress.addAll(chunkResidences.get(world).get(chunk));
		    }
		    ress.remove(name);
		    chunkResidences.get(world).put(chunk, ress);
		}
	    }
	}
    }

    public void calculateChunks(String name) {
	ClaimedResidence res = residences.get(name);
	if (res != null) {
	    String world = res.getWorld();
	    if (chunkResidences.get(world) == null) {
		chunkResidences.put(world, new HashMap<ChunkRef, List<String>>());
	    }
	    for (ChunkRef chunk : getChunks(res)) {
		List<String> ress = new ArrayList<>();
		if (chunkResidences.get(world).containsKey(chunk)) {
		    ress.addAll(chunkResidences.get(world).get(chunk));
		}
		ress.add(name);
		chunkResidences.get(world).put(chunk, ress);
	    }
	}
    }

    public static final class ChunkRef {

	public static int getChunkCoord(final int val) {
	    // For more info, see CraftBukkit.CraftWorld.getChunkAt( Location )
	    return val >> 4;
	}

	private final int z;
	private final int x;

	public ChunkRef(Location loc) {
	    this.x = getChunkCoord(loc.getBlockX());
	    this.z = getChunkCoord(loc.getBlockZ());
	}

	public ChunkRef(int x, int z) {
	    this.x = x;
	    this.z = z;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    ChunkRef other = (ChunkRef) obj;
	    return this.x == other.x && this.z == other.z;
	}

	@Override
	public int hashCode() {
	    return x ^ z;
	}

	/**
	 * Useful for debug
	 * 
	 * @return
	 */
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("{ x: ").append(x).append(", z: ").append(z).append(" }");
	    return sb.toString();
	}
    }
}

package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.GetTime;
import com.griefcraft.cache.ProtectionCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

public class ResidenceManager implements ResidenceInterface {
    protected SortedMap<String, ClaimedResidence> residences;
    protected Map<String, Map<ChunkRef, List<String>>> chunkResidences;
    protected List<String> shops = new ArrayList<String>();
    private Residence plugin;

    public ResidenceManager(Residence plugin) {
	residences = new TreeMap<>();
	chunkResidences = new HashMap<>();
	this.plugin = plugin;
    }

    public boolean isOwnerOfLocation(Player player, Location loc) {
	ClaimedResidence res = getByLoc(loc);
	if (res != null && res.isOwner(player))
	    return true;
	return false;
    }

    @Override
    public ClaimedResidence getByLoc(Location loc) {
	if (loc == null)
	    return null;
	ClaimedResidence res = null;
	String world = loc.getWorld().getName();
	ChunkRef chunk = new ChunkRef(loc);
	if (!chunkResidences.containsKey(world))
	    return null;

	Map<ChunkRef, List<String>> ChunkMap = chunkResidences.get(world);

	if (ChunkMap.containsKey(chunk)) {
	    for (String key : ChunkMap.get(chunk)) {
		ClaimedResidence entry = residences.get(key);
		if (entry == null)
		    continue;
		if (entry.containsLoc(loc)) {
		    res = entry;
		    break;
		}
	    }
	}

	if (res == null)
	    return null;

	ClaimedResidence subres = res.getSubzoneByLoc(loc);
	if (subres == null)
	    return res;
	return subres;
    }

    @Override
    public ClaimedResidence getByName(String name) {
	if (name == null) {
	    return null;
	}
	String[] split = name.split("\\.");
	if (split.length == 1) {
	    return residences.get(name.toLowerCase());
	}
	ClaimedResidence res = residences.get(split[0].toLowerCase());
	for (int i = 1; i < split.length; i++) {
	    if (res != null) {
		res = res.getSubzone(split[i].toLowerCase());
	    } else {
		return null;
	    }
	}
	return res;
    }

    @Override
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

    @Override
    public String getNameByRes(ClaimedResidence res) {
	Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
	for (Entry<String, ClaimedResidence> check : set) {
	    if (check.getValue() == res) {
		return check.getValue().getResidenceName();
	    }
	    String n = check.getValue().getSubzoneNameByRes(res);
	    if (n != null) {
		return check.getValue().getResidenceName() + "." + n;
	    }
	}
	return null;
    }

    @Override
    public String getSubzoneNameByRes(ClaimedResidence res) {
	Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
	for (Entry<String, ClaimedResidence> check : set) {
	    if (check.getValue() == res) {
		return check.getKey();
	    }
	    String n = check.getValue().getSubzoneNameByRes(res);
	    if (n != null) {
		return n;
	    }
	}
	return null;
    }

    @Override
    public void addShop(String res) {
	shops.add(res);
    }

    @Override
    public void removeShop(ClaimedResidence res) {
	removeShop(res.getName());
    }

    @Override
    public void removeShop(String resName) {
	shops.remove(resName);
    }

    @Override
    public List<String> getShops() {
	return shops;
    }

    @Override
    public boolean addResidence(String name, Location loc1, Location loc2) {
	return this.addResidence(name, Residence.getServerLandname(), loc1, loc2);
    }

    @Override
    public boolean addResidence(String name, String owner, Location loc1, Location loc2) {
	return this.addResidence(null, owner, name, loc1, loc2, true);
    }

    @Override
    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin) {
	return this.addResidence(player, player.getName(), name, loc1, loc2, resadmin);
    }

    public boolean addResidence(Player player, String owner, String name, Location loc1, Location loc2, boolean resadmin) {
	if (!Residence.validName(name)) {
	    Residence.msg(player, lm.Invalid_NameCharacters);
	    return false;
	}
	if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
	    Residence.msg(player, lm.Select_Points);
	    return false;
	}

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);

	PermissionGroup group = rPlayer.getGroup();
//	PermissionGroup group = Residence.getPermissionManager().getGroup(owner, loc1.getWorld().getName());
	boolean createpermission = group.canCreateResidences() && (player == null ? true : player.hasPermission("residence.create"));
	if (!createpermission && !resadmin) {
	    Residence.msg(player, lm.General_NoPermission);
	    return false;
	}

	if (rPlayer.getResAmount() >= rPlayer.getMaxRes() && !resadmin) {
	    Residence.msg(player, lm.Residence_TooMany);
	    return false;
	}

	CuboidArea newArea = new CuboidArea(loc1, loc2);
	ClaimedResidence newRes = new ClaimedResidence(owner, loc1.getWorld().getName(), plugin);
	newRes.getPermissions().applyDefaultFlags();
	newRes.setEnterMessage(group.getDefaultEnterMessage());
	newRes.setLeaveMessage(group.getDefaultLeaveMessage());
	newRes.setName(name);

	if (residences.containsKey(name.toLowerCase())) {
	    Residence.msg(player, lm.Residence_AlreadyExists, residences.get(name.toLowerCase()).getResidenceName());
	    return false;
	}

	newRes.BlockSellPrice = group.getSellPerBlock();

	if (!newRes.addArea(player, newArea, "main", resadmin, false))
	    return false;

	ResidenceCreationEvent resevent = new ResidenceCreationEvent(player, name, newRes, newArea);
	Residence.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return false;

	if (!newRes.isSubzone() && Residence.getConfigManager().enableEconomy() && !resadmin) {
	    double chargeamount = Math.ceil(newArea.getSize() * group.getCostPerBlock());
	    if (!TransactionManager.chargeEconomyMoney(player, chargeamount))
		return false;
	}

	residences.put(name.toLowerCase(), newRes);

	calculateChunks(name);
	Residence.getLeaseManager().removeExpireTime(name);
	Residence.getPlayerManager().addResidence(newRes.getOwner(), newRes);

	if (player != null) {
	    Residence.getSelectionManager().NewMakeBorders(player, newArea.getHighLoc(), newArea.getLowLoc(), false);
	    Residence.getAutoSelectionManager().getList().remove(player.getName().toLowerCase());
	    Residence.msg(player, lm.Area_Create, "main");
	    Residence.msg(player, lm.Residence_Create, name);
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

    public void listResidences(CommandSender sender) {
	this.listResidences(sender, sender.getName(), 1);
    }

    public void listResidences(CommandSender sender, boolean resadmin) {
	this.listResidences(sender, sender.getName(), 1, false, false, resadmin);
    }

    public void listResidences(CommandSender sender, String targetplayer, boolean showhidden) {
	this.listResidences(sender, targetplayer, 1, showhidden, false, showhidden);
    }

    public void listResidences(CommandSender sender, String targetplayer, int page) {
	this.listResidences(sender, targetplayer, page, false, false, false);
    }

    public void listResidences(CommandSender sender, int page, boolean showhidden) {
	this.listResidences(sender, sender.getName(), page, showhidden, false, showhidden);
    }

    public void listResidences(CommandSender sender, int page, boolean showhidden, boolean onlyHidden) {
	this.listResidences(sender, sender.getName(), page, showhidden, onlyHidden, showhidden);
    }

    public void listResidences(CommandSender sender, String string, int page, boolean showhidden) {
	this.listResidences(sender, string, page, showhidden, false, showhidden);
    }

    public void listResidences(final CommandSender sender, final String targetplayer, final int page, boolean showhidden, final boolean onlyHidden, boolean resadmin) {
	if (showhidden && !Residence.isResAdminOn(sender) && !sender.getName().equalsIgnoreCase(targetplayer)) {
	    showhidden = false;
	} else if (sender.getName().equalsIgnoreCase(targetplayer))
	    showhidden = true;
	final boolean hidden = showhidden;
	ArrayList<ClaimedResidence> ownedResidences = Residence.getPlayerManager().getResidences(targetplayer, hidden, onlyHidden);
	ownedResidences.addAll(Residence.getRentManager().getRents(targetplayer, onlyHidden));
	InformationPager.printListInfo(sender, targetplayer, ownedResidences, page, resadmin);
    }

    public void listAllResidences(CommandSender sender, int page) {
	this.listAllResidences(sender, page, false);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden) {
	this.listAllResidences(sender, page, showhidden, false);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden, boolean onlyHidden) {
	List<ClaimedResidence> list = getFromAllResidences(showhidden, onlyHidden);
	InformationPager.printListInfo(sender, null, list, page, showhidden);
    }

    public String[] getResidenceList() {
	return this.getResidenceList(true, true).toArray(new String[0]);
    }

    public Map<String, ClaimedResidence> getResidenceMapList(String targetplayer, boolean showhidden) {
	Map<String, ClaimedResidence> temp = new HashMap<String, ClaimedResidence>();
	for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
	    if (res.getValue().isOwner(targetplayer)) {
		boolean hidden = res.getValue().getPermissions().has("hidden", false);
		if ((showhidden) || (!showhidden && !hidden)) {
		    temp.put(res.getValue().getName().toLowerCase(), res.getValue());
		}
	    }
	}
	return temp;
    }

    public ArrayList<String> getResidenceList(boolean showhidden, boolean showsubzones) {
	return this.getResidenceList(null, showhidden, showsubzones, false);
    }

    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones) {
	return this.getResidenceList(targetplayer, showhidden, showsubzones, false, false);
    }

    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, boolean onlyHidden) {
	return this.getResidenceList(targetplayer, showhidden, showsubzones, false, onlyHidden);
    }

    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, boolean formattedOutput, boolean onlyHidden) {
	ArrayList<String> list = new ArrayList<>();
	for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
	    this.getResidenceList(targetplayer, showhidden, showsubzones, "", res.getKey(), res.getValue(), list, formattedOutput, onlyHidden);
	}
	return list;
    }

    public ArrayList<ClaimedResidence> getFromAllResidences(boolean showhidden, boolean onlyHidden) {
	ArrayList<ClaimedResidence> list = new ArrayList<>();
	for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
	    boolean hidden = res.getValue().getPermissions().has("hidden", false);
	    if (onlyHidden && !hidden)
		continue;
	    if ((showhidden) || (!showhidden && !hidden)) {
		list.add(res.getValue());
	    }
	}
	return list;
    }

    private void getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, String parentzone, String resname, ClaimedResidence res,
	ArrayList<String> list, boolean formattedOutput, boolean onlyHidden) {
	boolean hidden = res.getPermissions().has("hidden", false);

	if (onlyHidden && !hidden)
	    return;

	if ((showhidden) || (!showhidden && !hidden)) {
	    if (targetplayer == null || res.getPermissions().getOwner().equals(targetplayer)) {
		if (formattedOutput) {
		    list.add(Residence.msg(lm.Residence_List, parentzone, resname, res.getWorld()) +
			(hidden ? Residence.msg(lm.Residence_Hidden) : ""));
		} else {
		    list.add(parentzone + resname);
		}
	    }
	    if (showsubzones) {
		for (Entry<String, ClaimedResidence> sz : res.subzones.entrySet()) {
		    this.getResidenceList(targetplayer, showhidden, showsubzones, parentzone + resname + ".", sz.getKey(), sz.getValue(), list, formattedOutput,
			onlyHidden);
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

    public void removeResidence(CommandSender sender, String name, boolean resadmin) {
	if (sender instanceof Player)
	    removeResidence((Player) sender, name, resadmin);
	else
	    this.removeResidence(null, name, true);
    }

    @SuppressWarnings("deprecation")
    public void removeResidence(Player player, String name, boolean resadmin) {

	ClaimedResidence res = this.getByName(name);
	if (res == null) {
	    Residence.msg(player, lm.Invalid_Residence);
	    return;
	}

	name = res.getName();

	if (player != null && !resadmin) {
	    if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
		Residence.msg(player, lm.General_NoPermission);
		return;
	    }
	}

	if (Residence.getConfigManager().isRentPreventRemoval() && !resadmin) {
	    ClaimedResidence rented = res.getRentedSubzone();
	    if (rented != null) {
		Residence.msg(player, lm.Residence_CantRemove, res.getName(), rented.getName(), rented.getRentedLand().player);
		return;
	    }
	}

	ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, player == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
	Residence.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return;

	ClaimedResidence parent = res.getParent();
	if (parent == null) {
	    removeChunkList(name);

	    residences.remove(name.toLowerCase());

	    if (Residence.getConfigManager().isUseClean() && Residence.getConfigManager().getCleanWorlds().contains(res.getWorld())) {
		for (CuboidArea area : res.getAreaArray()) {

		    Location low = area.getLowLoc();
		    Location high = area.getHighLoc();

		    if (high.getBlockY() > Residence.getConfigManager().getCleanLevel()) {

			if (low.getBlockY() < Residence.getConfigManager().getCleanLevel())
			    low.setY(Residence.getConfigManager().getCleanLevel());

			World world = low.getWorld();

			Location temploc = new Location(world, low.getBlockX(), low.getBlockY(), low.getBlockZ());

			for (int x = low.getBlockX(); x <= high.getBlockX(); x++) {
			    temploc.setX(x);
			    for (int y = low.getBlockY(); y <= high.getBlockY(); y++) {
				temploc.setY(y);
				for (int z = low.getBlockZ(); z <= high.getBlockZ(); z++) {
				    temploc.setZ(z);
				    if (Residence.getConfigManager().getCleanBlocks().contains(temploc.getBlock().getTypeId())) {
					temploc.getBlock().setTypeId(0);
				    }
				}
			    }
			}
		    }
		}
	    }

	    if (Residence.getConfigManager().isRemoveLwcOnDelete())
		removeLwcFromResidence(player, res);

	    Residence.msg(player, lm.Residence_Remove, name);

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

	for (String oneSub : res.getSubzoneList()) {
	    Residence.getPlayerManager().removeResFromPlayer(res.getOwner(), name + "." + oneSub);
	    Residence.getRentManager().removeRentable(name + "." + oneSub);
	    Residence.getTransactionManager().removeFromSale(name + "." + oneSub);
	}

	Residence.getPlayerManager().removeResFromPlayer(res.getOwner(), name);
	Residence.getRentManager().removeRentable(name);
	Residence.getTransactionManager().removeFromSale(name);

	if (parent == null && Residence.getConfigManager().enableEconomy() && Residence.getConfigManager().useResMoneyBack()) {
	    int chargeamount = (int) Math.ceil(res.getAreaArray()[0].getSize() * res.getBlockSellPrice());
	    TransactionManager.giveEconomyMoney(player, chargeamount);
	}
    }

    public void removeLwcFromResidence(final Player player, final ClaimedResidence res) {
	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {
		long time = System.currentTimeMillis();
		LWC lwc = Residence.getLwc();
		if (lwc == null)
		    return;
		if (res == null)
		    return;
		int i = 0;

		ProtectionCache cache = lwc.getProtectionCache();

		List<Material> list = Residence.getConfigManager().getLwcMatList();

		try {
		    for (CuboidArea area : res.getAreaArray()) {
			Location low = area.getLowLoc();
			Location high = area.getHighLoc();
			World world = low.getWorld();
			for (int x = low.getBlockX(); x <= high.getBlockX(); x++) {
			    for (int y = low.getBlockY(); y <= high.getBlockY(); y++) {
				for (int z = low.getBlockZ(); z <= high.getBlockZ(); z++) {
				    Block b = world.getBlockAt(x, y, z);
				    if (!list.contains(b.getType()))
					continue;
				    Protection prot = cache.getProtection(b);
				    if (prot == null)
					continue;
				    prot.remove();
				    i++;
				}
			    }
			}
		    }
		} catch (Exception e) {
		}
		if (i > 0)
		    Residence.msg(player, lm.Residence_LwcRemoved, i, System.currentTimeMillis() - time);
		return;
	    }
	});
    }

    public void removeAllByOwner(String owner) {
	ArrayList<String> list = Residence.getPlayerManager().getResidenceList(owner);
	for (String oneRes : list) {
	    removeResidence(null, oneRes, true);
	}
    }

    public int getOwnedZoneCount(String player) {
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	return rPlayer.getResAmount();
    }

    public boolean hasMaxZones(String player, int target) {
	return getOwnedZoneCount(player) < target;
    }

    public void printAreaInfo(String areaname, CommandSender sender) {
	printAreaInfo(areaname, sender, false);
    }

    public void printAreaInfo(String areaname, CommandSender sender, boolean resadmin) {
	ClaimedResidence res = this.getByName(areaname);
	if (res == null) {
	    Residence.msg(sender, lm.Invalid_Residence);
	    return;
	}

	areaname = res.getName();

	Residence.msg(sender, lm.General_Separator);

	ResidencePermissions perms = res.getPermissions();

	String resNameOwner = "&e" + Residence.msg(lm.Residence_Line, areaname);
	resNameOwner += Residence.msg(lm.General_Owner, perms.getOwner());
	if (Residence.getConfigManager().enableEconomy()) {
	    if (res.isOwner(sender.getName()) || !(sender instanceof Player) || resadmin)
		resNameOwner += Residence.msg(lm.Bank_Name, res.getBank().getStoredMoney());
	}
	resNameOwner = ChatColor.translateAlternateColorCodes('&', resNameOwner);

	String worldInfo = Residence.msg(lm.General_World, perms.getWorld());

	if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
	    worldInfo += "&6 (&3";
	    CuboidArea area = res.getAreaArray()[0];
	    worldInfo += Residence.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
	    worldInfo += "&6; &3";
	    worldInfo += Residence.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
	    worldInfo += "&6)";
	    worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
	}

	worldInfo += "\n" + Residence.msg(lm.General_CreatedOn, GetTime.getTime(res.createTime));

	String ResFlagList = perms.listFlags(5);
	if (!(sender instanceof Player))
	    ResFlagList = perms.listFlags();
	String ResFlagMsg = Residence.msg(lm.General_ResidenceFlags, ResFlagList);

	if (perms.getFlags().size() > 2 && sender instanceof Player) {
	    ResFlagMsg = Residence.msg(lm.General_ResidenceFlags, perms.listFlags(5, 3)) + "...";
	}

	if (sender instanceof Player) {
	    String raw = convertToRaw(null, resNameOwner, worldInfo);
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + raw);

	    raw = convertToRaw(null, ResFlagMsg, ResFlagList);
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + raw);
	} else {
	    Residence.msg(sender, resNameOwner);
	    Residence.msg(sender, worldInfo);
	    Residence.msg(sender, ResFlagMsg);
	}

	if (!Residence.getConfigManager().isShortInfoUse() || !(sender instanceof Player))
	    sender.sendMessage(Residence.msg(lm.General_PlayersFlags, perms.listPlayersFlags()));
	else if (Residence.getConfigManager().isShortInfoUse() || sender instanceof Player) {
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + perms.listPlayersFlagsRaw(sender.getName(), Residence.msg(
		lm.General_PlayersFlags, "")));
	}

	String groupFlags = perms.listGroupFlags();
	if (groupFlags.length() > 0)
	    Residence.msg(sender, lm.General_GroupFlags, groupFlags);

	String msg = "";
	msg += Residence.msg(lm.General_TotalResSize, res.getTotalSize(), res.getXZSize());

	Residence.msg(sender, ChatColor.translateAlternateColorCodes('&', msg));

	if (Residence.getEconomyManager() != null) {
	    Residence.msg(sender, lm.General_TotalWorth, (int) ((res.getTotalSize() * res.getOwnerGroup().getCostPerBlock())
		* 100) / 100.0, (int) ((res.getTotalSize() * res.getBlockSellPrice()) * 100) / 100.0);
	}
	if (Residence.getConfigManager().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
	    String time = Residence.getLeaseManager().getExpireTime(areaname);
	    if (time != null)
		Residence.msg(sender, lm.Economy_LeaseExpire, time);
	}

	if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isForRent(areaname) && !Residence.getRentManager().isRented(areaname)) {
	    String forRentMsg = Residence.msg(lm.Rent_isForRent);

	    RentableLand rentable = Residence.getRentManager().getRentableLand(areaname);
	    StringBuilder rentableString = new StringBuilder();
	    if (rentable != null) {
		rentableString.append(Residence.msg(lm.General_Cost, rentable.cost, rentable.days) + "\n");
		rentableString.append(Residence.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing) + "\n");
		rentableString.append(Residence.msg(lm.Rentable_StayInMarket, rentable.StayInMarket) + "\n");
		rentableString.append(Residence.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }
	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + convertToRaw(null, forRentMsg, rentableString.toString()));
	    else
		Residence.msg(sender, forRentMsg);
	} else if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isRented(areaname)) {
	    String RentedMsg = Residence.msg(lm.Residence_RentedBy, Residence.getRentManager().getRentingPlayer(areaname));

	    RentableLand rentable = Residence.getRentManager().getRentableLand(areaname);
	    RentedLand rented = Residence.getRentManager().getRentedLand(areaname);

	    StringBuilder rentableString = new StringBuilder();
	    if (rented != null) {
		rentableString.append(Residence.msg(lm.Rent_Expire, GetTime.getTime(rented.endTime)) + "\n");
		if (rented.player.equals(sender.getName()) || resadmin || res.isOwner(sender.getName()))
		    rentableString.append((rented.AutoPay ? Residence.msg(lm.Rent_AutoPayTurnedOn) : Residence.msg(lm.Rent_AutoPayTurnedOff))
			+ "\n");
	    }

	    if (rentable != null) {
		rentableString.append(Residence.msg(lm.General_Cost, rentable.cost, rentable.days) + "\n");
		rentableString.append(Residence.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing) + "\n");
		rentableString.append(Residence.msg(lm.Rentable_StayInMarket, rentable.StayInMarket) + "\n");
		rentableString.append(Residence.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + convertToRaw(null, RentedMsg, rentableString.toString()));
	    else
		Residence.msg(sender, RentedMsg);
	} else if (Residence.getTransactionManager().isForSale(areaname)) {
	    int amount = Residence.getTransactionManager().getSaleAmount(areaname);
	    String SellMsg = Residence.msg(lm.Economy_LandForSale) + " " + amount;
	    Residence.msg(sender, SellMsg);
	}

	Residence.msg(sender, lm.General_Separator);
    }

    public String convertToRaw(String preText, String text, String hover) {
	return convertToRaw(preText, text, hover, null);
    }

    public String convertToRaw(String preText, String text, String hover, String command) {
	StringBuilder msg = new StringBuilder();
	String cmd = "";
	if (command != null) {
	    cmd = ",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/" + command + "\"}";
	}
	msg.append("[\"\",");
	if (preText != null)
	    msg.append("{\"text\":\"" + preText + "\"}");
	msg.append("{\"text\":\"" + text + "\"" + cmd + ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + hover + "\"}]}}}");
	msg.append("]");
	return msg.toString();
    }

    public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea, boolean resadmin) {
	ClaimedResidence reciever = this.getByName(targetArea);
	ClaimedResidence source = this.getByName(sourceArea);
	if (source == null || reciever == null) {
	    Residence.msg(reqPlayer, lm.Invalid_Residence);
	    return;
	}
	if (!resadmin) {
	    if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
		Residence.msg(reqPlayer, lm.General_NoPermission);
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
		if (!res.getValue().getWorld().equals(world.getName()))
		    continue;

		try {
		    resmap.put(res.getValue().getResidenceName(), res.getValue().save());
		} catch (Exception ex) {
		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + ChatColor.RED + " Failed to save residence (" + res.getKey() + ")!");
		    Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	    worldmap.put(world.getName(), resmap);
	}
	return worldmap;
    }

    public ResidenceManager load(Map<String, Object> root) throws Exception {
	ResidenceManager resm = new ResidenceManager(plugin);
	if (root == null)
	    return resm;

	for (World world : Residence.getServ().getWorlds()) {
	    long time = System.currentTimeMillis();
	    @SuppressWarnings("unchecked")
	    Map<String, Object> reslist = (Map<String, Object>) root.get(world.getName());
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Loading " + world.getName() + " data into memory...");
	    if (reslist != null) {
		try {
		    resm.chunkResidences.put(world.getName(), loadMap(world.getName(), reslist, resm));
		} catch (Exception ex) {
		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + ChatColor.RED + "Error in loading save file for world: " + world.getName());
		    if (Residence.getConfigManager().stopOnSaveError())
			throw (ex);
		}
	    }

	    long pass = System.currentTimeMillis() - time;
	    String PastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Loaded " + world.getName() + " data into memory. (" + PastTime + ")");
	}
	return resm;
    }

    public Map<ChunkRef, List<String>> loadMap(String worldName, Map<String, Object> root, ResidenceManager resm) throws Exception {
	Map<ChunkRef, List<String>> retRes = new HashMap<>();
	if (root == null)
	    return retRes;

	int i = 0;
	int y = 0;
	for (Entry<String, Object> res : root.entrySet()) {
	    if (i == 100 & Residence.getConfigManager().isUUIDConvertion())
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " " + worldName + " UUID conversion done: " + y + " of " + root.size());
	    if (i >= 100)
		i = 0;
	    i++;
	    y++;
	    try {
		@SuppressWarnings("unchecked")
		ClaimedResidence residence = ClaimedResidence.load((Map<String, Object>) res.getValue(), null, plugin);

		if (residence == null)
		    continue;

		if (residence.getPermissions().getOwnerUUID().toString().equals(Residence.getServerLandUUID()) &&
		    !residence.getOwner().equalsIgnoreCase("Server land") &&
		    !residence.getOwner().equalsIgnoreCase(Residence.getServerLandname()))
		    continue;

		if (residence.getOwner().equalsIgnoreCase("Server land")) {
		    residence.getPermissions().setOwner(Residence.getServerLandname(), false);
		}
		String resName = res.getKey().toLowerCase();

		// Checking for duplicated residence names and renaming them
		int increment = getNameIncrement(resName, resm);

		if (residence.getResidenceName() == null)
		    residence.setName(res.getKey());

		if (increment > 0) {
		    residence.setName(residence.getResidenceName() + increment);
		    resName += increment;
		}

		for (ChunkRef chunk : getChunks(residence)) {
		    List<String> ress = new ArrayList<>();
		    if (retRes.containsKey(chunk)) {
			ress.addAll(retRes.get(chunk));
		    }
		    ress.add(resName);
		    retRes.put(chunk, ress);
		}

		resm.residences.put(resName, residence);

	    } catch (Exception ex) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + ChatColor.RED + " Failed to load residence (" + res.getKey() + ")! Reason:" + ex.getMessage()
		    + " Error Log:");
		Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
		if (Residence.getConfigManager().stopOnSaveError()) {
		    throw (ex);
		}
	    }
	}

	return retRes;
    }

    private static int getNameIncrement(String name, ResidenceManager resm) {
	String orName = name;
	int i = 0;
	while (i < 1000) {
	    if (resm.residences.containsKey(name)) {
		i++;
		name = orName + i;
	    } else
		break;
	}
	return i;
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
	if (!player.hasPermission("residence.rename")) {
	    Residence.msg(player, lm.General_NoPermission);
	    return false;
	}

	if (!Residence.validName(newName)) {
	    Residence.msg(player, lm.Invalid_NameCharacters);
	    return false;
	}
	ClaimedResidence res = this.getByName(oldName);
	if (res == null) {
	    Residence.msg(player, lm.Invalid_Residence);
	    return false;
	}

	oldName = res.getName();

	if (res.getPermissions().hasResidencePermission(player, true) || resadmin) {
	    if (res.getParent() == null) {
		if (residences.containsKey(newName.toLowerCase())) {
		    Residence.msg(player, lm.Residence_AlreadyExists, newName);
		    return false;
		}

		ResidenceRenameEvent resevent = new ResidenceRenameEvent(res, newName, oldName);
		Residence.getServ().getPluginManager().callEvent(resevent);
		removeChunkList(oldName);
		res.setName(newName);

		residences.put(newName.toLowerCase(), res);
		residences.remove(oldName.toLowerCase());

		Residence.getPlayerManager().renameResidence(player.getName(), res.getName(), newName);

		calculateChunks(newName);
		if (Residence.getConfigManager().useLeases()) {
		    Residence.getLeaseManager().updateLeaseName(oldName, newName);
		}

		Residence.getSignUtil().updateSignResName(res);

		Residence.msg(player, lm.Residence_Rename, oldName, newName);

		return true;
	    }
	    String[] oldname = oldName.split("\\.");
	    ClaimedResidence parent = res.getParent();

	    boolean feed = parent.renameSubzone(player, oldname[oldname.length - 1], newName, resadmin);

	    Residence.getSignUtil().updateSignResName(res);

	    return feed;
	}

	Residence.msg(player, lm.General_NoPermission);

	return false;
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin) {
	ClaimedResidence res = getByName(residence);
	if (res == null) {
	    Residence.msg(reqPlayer, lm.Invalid_Residence);
	    return;
	}

	residence = res.getName();

	if (!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin) {
	    Residence.msg(reqPlayer, lm.General_NoPermission);
	    return;
	}
	Player giveplayer = Residence.getServ().getPlayer(targPlayer);
	if (giveplayer == null || !giveplayer.isOnline()) {
	    Residence.msg(reqPlayer, lm.General_NotOnline);
	    return;
	}
	CuboidArea[] areas = res.getAreaArray();

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(giveplayer);
	PermissionGroup group = rPlayer.getGroup();

	if (areas.length > group.getMaxPhysicalPerResidence() && !resadmin) {
	    Residence.msg(reqPlayer, lm.Residence_GiveLimits);
	    return;
	}
	if (!hasMaxZones(giveplayer.getName(), rPlayer.getMaxRes()) && !resadmin) {
	    Residence.msg(reqPlayer, lm.Residence_GiveLimits);
	    return;
	}
	if (!resadmin) {
	    for (CuboidArea area : areas) {
		if (!res.isSubzone() && !res.isSmallerThanMax(giveplayer, area, resadmin) || res.isSubzone() && !res.isSmallerThanMaxSubzone(giveplayer, area,
		    resadmin)) {
		    Residence.msg(reqPlayer, lm.Residence_GiveLimits);
		    return;
		}
	    }
	}

	Residence.getPlayerManager().removeResFromPlayer(reqPlayer, residence);
	Residence.getPlayerManager().addResidence(targPlayer, res);

	res.getPermissions().setOwner(giveplayer.getName(), true);
	// Fix phrases here
	Residence.msg(reqPlayer, lm.Residence_Give, residence, giveplayer.getName());
	Residence.msg(giveplayer, lm.Residence_Recieve, residence, reqPlayer.getName());
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

	Residence.getPlayerManager().fillList();
    }

    public int getResidenceCount() {
	return residences.size();
    }

    public Map<String, ClaimedResidence> getResidences() {
	return residences;
    }

    public void removeChunkList(String name) {
	if (name == null)
	    return;

	name = name.toLowerCase();
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
	ClaimedResidence res = null;

	if (name == null)
	    return;
	name = name.toLowerCase();
	res = residences.get(name);

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

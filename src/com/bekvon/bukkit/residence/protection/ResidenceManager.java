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
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.GetTime;

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

    public ClaimedResidence getByName(String name) {
	return getByName(name, false);
    }

    public ClaimedResidence getByName(String name, boolean tp) {
	if (name == null) {
	    return null;
	}
	String[] split = name.split("\\.");
	if (Residence.getConfigManager().isResCreateCaseSensitive() && !tp ||
	    tp && Residence.getConfigManager().isResTpCaseSensitive()) {
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
	} else {
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
		return check.getValue().getResidenceName();
	    }
	    String n = check.getValue().getSubzoneNameByRes(res);
	    if (n != null) {
		return check.getValue().getResidenceName() + "." + n;
	    }
	}
	return null;
    }

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

    public void addShop(String res) {
	shops.add(res);
    }

    public void removeShop(ClaimedResidence res) {
	removeShop(res.getName());
    }

    public void removeShop(String resName) {
	shops.remove(resName);
    }

    public List<String> getShops() {
	return shops;
    }

    public boolean addResidence(String name, Location loc1, Location loc2) {
	return this.addResidence(name, Residence.getServerLandname(), loc1, loc2);
    }

    public boolean addResidence(String name, String owner, Location loc1, Location loc2) {
	return this.addResidence(null, owner, name, loc1, loc2, true);
    }

    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin) {
	return this.addResidence(player, player.getName(), name, loc1, loc2, resadmin);
    }

    public boolean addResidence(Player player, String owner, String name, Location loc1, Location loc2, boolean resadmin) {
	if (!Residence.validName(name)) {
	    if (player != null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.NameCharacters"));
	    }
	    return false;
	}
	if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
	    if (player != null) {
		player.sendMessage(Residence.getLM().getMessage("Select.Points"));
	    }
	    return false;
	}
	PermissionGroup group = Residence.getPermissionManager().getGroup(owner, loc1.getWorld().getName());
	boolean createpermission = group.canCreateResidences() || (player == null ? true : player.hasPermission("residence.create"));
	if (!createpermission && !resadmin) {
	    if (player != null) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    }
	    return false;
	}
	if (player != null) {
	    if (!hasMaxZones(player.getName(), group.getMaxZones(player.getName())) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Residence.TooMany"));
		return false;
	    }
	}
	CuboidArea newArea = new CuboidArea(loc1, loc2);
	ClaimedResidence newRes = new ClaimedResidence(owner, loc1.getWorld().getName(), plugin);
	newRes.getPermissions().applyDefaultFlags();
	newRes.setEnterMessage(group.getDefaultEnterMessage());
	newRes.setLeaveMessage(group.getDefaultLeaveMessage());
	newRes.setName(name);

	if (Residence.getConfigManager().isResCreateCaseSensitive()) {
	    if (residences.containsKey(name)) {
		if (player != null) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyExists", name));
		}
		return false;
	    }
	} else {
	    if (residences.containsKey(name.toLowerCase())) {
		if (player != null) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyExists", name));
		}
		return false;
	    }
	}

	newRes.BlockSellPrice = group.getSellPerBlock();

	if (!newRes.addArea(player, newArea, "main", resadmin, false))
	    return false;

	ResidenceCreationEvent resevent = new ResidenceCreationEvent(player, name, newRes, newArea);
	Residence.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return false;

	if (!newRes.isSubzone() && Residence.getConfigManager().enableEconomy() && !resadmin) {
	    int chargeamount = (int) Math.ceil((double) newArea.getSize() * group.getCostPerBlock());
	    if (!TransactionManager.chargeEconomyMoney(player, chargeamount))
		return false;
	}

	if (Residence.getConfigManager().isResCreateCaseSensitive())
	    residences.put(name, newRes);
	else
	    residences.put(name.toLowerCase(), newRes);

	calculateChunks(name);
	Residence.getLeaseManager().removeExpireTime(name);
	Residence.getPlayerManager().addResidence(newRes.getOwner(), newRes);

	if (player != null) {
	    Residence.getSelectionManager().NewMakeBorders(player, newArea.getHighLoc(), newArea.getLowLoc(), false);
	    Residence.getAutoSelectionManager().getList().remove(player.getName().toLowerCase());
	    player.sendMessage(Residence.getLM().getMessage("Area.Create", "main"));
	    player.sendMessage(Residence.getLM().getMessage("Residence.Create", name));
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

    public void listResidences(CommandSender sender, int page) {
	this.listResidences(sender, sender.getName(), page);
    }

    public void listResidences(CommandSender sender, String targetplayer, boolean showhidden) {
	this.listResidences(sender, targetplayer, 1, showhidden, false);
    }

    public void listResidences(CommandSender sender, String targetplayer, int page) {
	this.listResidences(sender, targetplayer, page, false, false);
    }

    public void listResidences(CommandSender sender, int page, boolean showhidden) {
	this.listResidences(sender, sender.getName(), page, showhidden, false);
    }

    public void listResidences(CommandSender sender, int page, boolean showhidden, boolean onlyHidden) {
	this.listResidences(sender, sender.getName(), page, showhidden, onlyHidden);
    }

    public void listResidences(CommandSender sender, String string, int page, boolean showhidden) {
	this.listResidences(sender, string, page, showhidden, false);
    }

    public void listResidences(final CommandSender sender, final String targetplayer, final int page, boolean showhidden, final boolean onlyHidden) {
	if (showhidden && !Residence.isResAdminOn(sender) && !sender.getName().equalsIgnoreCase(targetplayer)) {
	    showhidden = false;
	} else if (sender.getName().equalsIgnoreCase(targetplayer))
	    showhidden = true;
	final boolean hidden = showhidden;
	ArrayList<ClaimedResidence> ownedResidences = Residence.getPlayerManager().getResidences(targetplayer, hidden, onlyHidden);
	ownedResidences.addAll(Residence.getRentManager().getRents(targetplayer, onlyHidden));
	InformationPager.printListInfo(sender, targetplayer, ownedResidences, page, showhidden);
    }

    public void listAllResidences(CommandSender sender, int page) {
	this.listAllResidences(sender, page, false);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden) {
	this.listAllResidences(sender, page, showhidden, false, false);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden, boolean showsubzones, boolean onlyHidden) {

	int perPage = 20;
	if (sender instanceof Player)
	    perPage = 6;

	int start = (page - 1) * perPage;
	int end = start + perPage;

	List<ClaimedResidence> list = getFromAllResidences(showhidden, onlyHidden, start, end);

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
		    temp.put(res.getValue().getName(), res.getValue());
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

    public ArrayList<ClaimedResidence> getFromAllResidences(boolean showhidden, boolean onlyHidden, int start, int end) {
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
		    list.add(Residence.getLM().getMessage("Residence.List", parentzone, resname, res.getWorld()) +
			(hidden ? Residence.getLM().getMessage("Residence.Hidden") : ""));
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
	    if (player != null)
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	name = res.getName();

	if (player != null && !resadmin) {
	    if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
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

	    if (Residence.getConfigManager().isResCreateCaseSensitive())
		residences.remove(name);
	    else
		residences.remove(name.toLowerCase());

	    if (Residence.getConfigManager().isUseClean() && Residence.getConfigManager().getCleanWorlds().contains(res.getWorld())) {
		CuboidArea area = res.getAreaArray()[0];

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

	    if (player != null)
		player.sendMessage(Residence.getLM().getMessage("Residence.Remove", name));

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
	    int chargeamount = (int) Math.ceil((double) res.getAreaArray()[0].getSize() * res.getBlockSellPrice());
	    TransactionManager.giveEconomyMoney(player, chargeamount);
	}
    }

    public void removeAllByOwner(String owner) {
	this.removeAllByOwner(null, owner, residences);
    }

    public void removeAllByOwner(CommandSender sender, String owner) {
	this.removeAllByOwner(sender, owner, residences);
    }

    private void removeAllByOwner(CommandSender sender, String owner, Map<String, ClaimedResidence> resholder) {
	ArrayList<String> list = Residence.getPlayerManager().getResidenceList(owner);
	for (String oneRes : list) {
	    removeResidence(null, oneRes, true);
	}
    }

    public int getOwnedZoneCount(String player) {
	return Residence.getPlayerManager().getResidenceList(player).size();
    }

    public boolean hasMaxZones(String player, int target) {
	int count = getOwnedZoneCount(player);
	if (count >= target)
	    return false;
	return true;
    }

    public void printAreaInfo(String areaname, CommandSender sender) {
	printAreaInfo(areaname, sender, false);
    }

    public void printAreaInfo(String areaname, CommandSender sender, boolean resadmin) {
	ClaimedResidence res = this.getByName(areaname);
	if (res == null) {
	    sender.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	areaname = res.getName();

	sender.sendMessage(Residence.getLM().getMessage("General.Separator"));

	ResidencePermissions perms = res.getPermissions();
	Language lm = Residence.getLM();

	String resNameOwner = "&e" + lm.getMessage("Residence.Line", areaname);
	resNameOwner += lm.getMessage("General.Owner", perms.getOwner());
	if (Residence.getConfigManager().enableEconomy()) {
	    if (res.isOwner(sender.getName()) || !(sender instanceof Player) || resadmin)
		resNameOwner += lm.getMessage("Bank.Name", res.getBank().getStoredMoney());
	}
	resNameOwner = ChatColor.translateAlternateColorCodes('&', resNameOwner);

	String worldInfo = lm.getMessage("General.World", perms.getWorld());

	if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
	    worldInfo += "&6 (&3";
	    CuboidArea area = res.getAreaArray()[0];
	    worldInfo += lm.getMessage("General.CoordsTop", area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
	    worldInfo += "&6; &3";
	    worldInfo += lm.getMessage("General.CoordsBottom", area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
	    worldInfo += "&6)";
	    worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
	}

	worldInfo += "\n" + Residence.getLM().getMessage("General.CreatedOn", GetTime.getTime(res.createTime));

	String ResFlagList = perms.listFlags(5);
	if (!(sender instanceof Player))
	    ResFlagList = perms.listFlags();
	String ResFlagMsg = lm.getMessage("General.ResidenceFlags", ResFlagList);

	if (perms.getFlags().size() > 2 && sender instanceof Player) {
	    ResFlagMsg = lm.getMessage("General.ResidenceFlags", perms.listFlags(5, 3)) + "...";
	}

	if (sender instanceof Player) {
	    String raw = convertToRaw(null, resNameOwner, worldInfo);
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + raw);

	    raw = convertToRaw(null, ResFlagMsg, ResFlagList);
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + raw);
	} else {
	    sender.sendMessage(resNameOwner);
	    sender.sendMessage(worldInfo);
	    sender.sendMessage(ResFlagMsg);
	}

//	sender.sendMessage(lm.getMessage("General.Flags", perms.listFlags()));

	if (!Residence.getConfigManager().isShortInfoUse() || !(sender instanceof Player))
	    sender.sendMessage(lm.getMessage("General.PlayersFlags", perms.listPlayersFlags()));
	else if (Residence.getConfigManager().isShortInfoUse() || sender instanceof Player) {
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + perms.listPlayersFlagsRaw(sender.getName(), lm.getMessage(
		"General.PlayersFlags")));
	}

	String groupFlags = perms.listGroupFlags();
	if (groupFlags.length() > 0)
	    sender.sendMessage(lm.getMessage("General.GroupFlags", groupFlags));

//	if (!Residence.getConfigManager().isShortInfoUse() || !(sender instanceof Player)) {
//	    String othersFlags = perms.listOtherPlayersFlags(sender.getName());
//	    if (!othersFlags.equalsIgnoreCase(""))
//		sender.sendMessage(lm.getMessage("General.OthersFlags", othersFlags));
//	} else {
//	    String othersFlags = perms.listOtherPlayersFlags(sender.getName());
//	    if (!othersFlags.equalsIgnoreCase(""))
//		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + perms.listOtherPlayersFlagsRaw(lm.getMessage(
//		    "General.OthersFlags", ""), sender.getName()));
//	}

	String msg = "";
	msg += lm.getMessage("General.TotalSize", res.getTotalSize());

	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

	if (Residence.getEconomyManager() != null) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(res.getOwner(), res.getWorld());
	    sender.sendMessage(lm.getMessage("General.TotalWorth", (int) ((res.getTotalSize() * group.getCostPerBlock())
		* 100) / 100.0, (int) ((res.getTotalSize() * res.getBlockSellPrice()) * 100) / 100.0));
	}
	if (Residence.getConfigManager().useLeases() && Residence.getLeaseManager().leaseExpires(areaname)) {
	    String time = Residence.getLeaseManager().getExpireTime(areaname);
	    if (time != null)
		sender.sendMessage(lm.getMessage("Economy.LeaseExpire", time));
	}

	if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isForRent(areaname) && !Residence.getRentManager().isRented(areaname)) {
	    String forRentMsg = lm.getMessage("Rent.isForRent");

	    RentableLand rentable = Residence.getRentManager().getRentableLand(areaname);
	    StringBuilder rentableString = new StringBuilder();
	    if (rentable != null) {
		rentableString.append(Residence.getLM().getMessage("General.Cost", rentable.cost, rentable.days) + "\n");
		rentableString.append(Residence.getLM().getMessage("Rentable.AllowRenewing", rentable.AllowRenewing) + "\n");
		rentableString.append(Residence.getLM().getMessage("Rentable.StayInMarket", rentable.StayInMarket) + "\n");
		rentableString.append(Residence.getLM().getMessage("Rentable.AllowAutoPay", rentable.AllowAutoPay));
	    }
	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + convertToRaw(null, forRentMsg, rentableString.toString()));
	    else
		sender.sendMessage(forRentMsg);
	} else if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isRented(areaname)) {
	    String RentedMsg = lm.getMessage("Residence.RentedBy", Residence.getRentManager().getRentingPlayer(areaname));

	    RentableLand rentable = Residence.getRentManager().getRentableLand(areaname);
	    RentedLand rented = Residence.getRentManager().getRentedLand(areaname);

	    StringBuilder rentableString = new StringBuilder();
	    if (rented != null) {
		rentableString.append(Residence.getLM().getMessage("Rent.Expire", GetTime.getTime(rented.endTime)) + "\n");
		if (rented.player.equals(sender.getName()) || resadmin || res.isOwner(sender.getName()))
		    rentableString.append((rented.AutoPay ? Residence.getLM().getMessage("Rent.AutoPayTurnedOn") : Residence.getLM().getMessage("Rent.AutoPayTurnedOff"))
			+ "\n");
	    }

	    if (rentable != null) {
		rentableString.append(Residence.getLM().getMessage("General.Cost", rentable.cost, rentable.days) + "\n");
		rentableString.append(Residence.getLM().getMessage("Rentable.AllowRenewing", rentable.AllowRenewing) + "\n");
		rentableString.append(Residence.getLM().getMessage("Rentable.StayInMarket", rentable.StayInMarket) + "\n");
		rentableString.append(Residence.getLM().getMessage("Rentable.AllowAutoPay", rentable.AllowAutoPay));
	    }

	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + convertToRaw(null, RentedMsg, rentableString.toString()));
	    else
		sender.sendMessage(RentedMsg);
	} else if (Residence.getTransactionManager().isForSale(areaname)) {
	    int amount = Residence.getTransactionManager().getSaleAmount(areaname);
	    String SellMsg = lm.getMessage("Economy.LandForSale") + " " + amount;
	    sender.sendMessage(SellMsg);
	}

	sender.sendMessage(Residence.getLM().getMessage("General.Separator"));
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
	    reqPlayer.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}
	if (!resadmin) {
	    if (!reciever.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
		reqPlayer.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
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
		    resmap.put(res.getValue().getShortName(), res.getValue().save());
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
		String resName = res.getKey();

		if (residence.getResidenceName() == null)
		    residence.setName(res.getKey());

		if (!Residence.getConfigManager().isResCreateCaseSensitive())
		    resName = resName.toLowerCase();

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
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return false;
	}

	if (!Residence.validName(newName)) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.NameCharacters"));
	    return false;
	}
	ClaimedResidence res = this.getByName(oldName);
	if (res == null) {
	    if (player != null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    }
	    return false;
	}

	oldName = res.getName();

	if (res.getPermissions().hasResidencePermission(player, true) || resadmin) {
	    if (res.getParent() == null) {
		if (Residence.getConfigManager().isResCreateCaseSensitive()) {
		    if (residences.containsKey(newName)) {
			if (player != null) {
			    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyExists", newName));
			}
			return false;
		    }
		} else {
		    if (residences.containsKey(newName.toLowerCase())) {
			if (player != null) {
			    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyExists", newName));
			}
			return false;
		    }
		}

		ResidenceRenameEvent resevent = new ResidenceRenameEvent(res, newName, oldName);
		Residence.getServ().getPluginManager().callEvent(resevent);
		removeChunkList(oldName);
		res.setName(newName);
		if (Residence.getConfigManager().isResCreateCaseSensitive())
		    residences.put(newName, res);
		else
		    residences.put(newName.toLowerCase(), res);

		residences.remove(oldName);

		Residence.getPlayerManager().renameResidence(player.getName(), res.getName(), newName);

		calculateChunks(newName);
		if (Residence.getConfigManager().useLeases()) {
		    Residence.getLeaseManager().updateLeaseName(oldName, newName);
		}

		Residence.getSignUtil().updateSignResName(res);

		if (player != null) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.Rename", oldName, newName));
		}
		return true;
	    } else {

		String[] oldname = oldName.split("\\.");
		ClaimedResidence parent = res.getParent();

		boolean feed = parent.renameSubzone(player, oldname[oldname.length - 1], newName, resadmin);

		Residence.getSignUtil().updateSignResName(res);

		return feed;
	    }
	} else {
	    if (player != null) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    }
	    return false;
	}
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin) {
	ClaimedResidence res = getByName(residence);
	if (res == null) {
	    reqPlayer.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	residence = res.getName();

	if (!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin) {
	    reqPlayer.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return;
	}
	Player giveplayer = Residence.getServ().getPlayer(targPlayer);
	if (giveplayer == null || !giveplayer.isOnline()) {
	    reqPlayer.sendMessage(Residence.getLM().getMessage("General.NotOnline"));
	    return;
	}
	CuboidArea[] areas = res.getAreaArray();
	PermissionGroup g = Residence.getPermissionManager().getGroup(giveplayer);
	if (areas.length > g.getMaxPhysicalPerResidence() && !resadmin) {
	    reqPlayer.sendMessage(Residence.getLM().getMessage("Residence.GiveLimits"));
	    return;
	}
	if (!hasMaxZones(giveplayer.getName(), g.getMaxZones(giveplayer.getName())) && !resadmin) {
	    reqPlayer.sendMessage(Residence.getLM().getMessage("Residence.GiveLimits"));
	    return;
	}
	if (!resadmin) {
	    for (CuboidArea area : areas) {
		if (!g.inLimits(area)) {
		    reqPlayer.sendMessage(Residence.getLM().getMessage("Residence.GiveLimits"));
		    return;
		}
	    }
	}

	Residence.getPlayerManager().removeResFromPlayer(reqPlayer, residence);
	Residence.getPlayerManager().addResidence(targPlayer, res);

	res.getPermissions().setOwner(giveplayer.getName(), true);
	// Fix phrases here
	reqPlayer.sendMessage(Residence.getLM().getMessage("Residence.Give", residence, giveplayer.getName()));
	giveplayer.sendMessage(Residence.getLM().getMessage("Residence.Recieve", residence, reqPlayer.getName()));
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
	if (!Residence.getConfigManager().isResCreateCaseSensitive())
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
	if (!Residence.getConfigManager().isResCreateCaseSensitive())
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

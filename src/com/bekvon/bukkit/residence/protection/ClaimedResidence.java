package com.bekvon.bukkit.residence.protection;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.itemlist.ItemList.ListType;
import com.bekvon.bukkit.residence.itemlist.ResidenceItemList;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.Debug;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ClaimedResidence {

    protected ClaimedResidence parent;
    protected Map<String, CuboidArea> areas;
    protected Map<String, ClaimedResidence> subzones;
    protected ResidencePermissions perms;
    protected ResidenceBank bank;
    protected Double BlockSellPrice = 0.0;
    protected Location tpLoc;
    protected String enterMessage;
    protected String leaveMessage;
    protected String ShopDesc = null;
    protected String ChatPrefix = "";
    protected ChatColor ChannelColor = ChatColor.WHITE;
    protected ResidenceItemList ignorelist;
    protected ResidenceItemList blacklist;
    private Residence plugin;

    private ClaimedResidence(Residence plugin) {
	subzones = new HashMap<>();
	areas = new HashMap<>();
	bank = new ResidenceBank(this);
	blacklist = new ResidenceItemList(this, ListType.BLACKLIST);
	ignorelist = new ResidenceItemList(this, ListType.IGNORELIST);
	this.plugin = plugin;
    }

    public boolean isSubzone() {
	return parent == null ? false : true;
    }

    public ClaimedResidence(String creationWorld, Residence plugin) {
	this(Residence.getServerLandname(), creationWorld, plugin);
    }

    public ClaimedResidence(String creator, String creationWorld, Residence plugin) {
	this(plugin);
	perms = new ResidencePermissions(this, creator, creationWorld);
    }

    public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence, Residence plugin) {
	this(creator, creationWorld, plugin);
	parent = parentResidence;
    }

    public boolean addArea(CuboidArea area, String name) {
	return addArea(null, area, name, true);
    }

    public static boolean CheckAreaSize(Player player, CuboidArea area, boolean resadmin) {
	if (!resadmin && area.getSize() < Residence.getConfigManager().getMinimalResSize()) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaToSmallTotal", String.valueOf(Residence.getConfigManager()
		    .getMinimalResSize())));
	    }
	    return false;
	}

	if (!resadmin && area.getXSize() < Residence.getConfigManager().getMinimalResX()) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaToSmallX", String.valueOf(area.getXSize()) + "|" + String.valueOf(Residence
		    .getConfigManager().getMinimalResX())));
	    }
	    return false;
	}
	if (!resadmin && area.getYSize() < Residence.getConfigManager().getMinimalResY()) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaToSmallY", String.valueOf(area.getYSize()) + "|" + String.valueOf(Residence
		    .getConfigManager().getMinimalResY())));
	    }
	    return false;
	}
	if (!resadmin && area.getZSize() < Residence.getConfigManager().getMinimalResZ()) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaToSmallZ", String.valueOf(area.getZSize()) + "|" + String.valueOf(Residence
		    .getConfigManager().getMinimalResZ())));
	    }
	    return false;
	}
	return true;
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin) {
	if (!Residence.validName(name)) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
	    }
	    return false;
	}
	if (areas.containsKey(name)) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaExists"));
	    }
	    return false;
	}

	if (!CheckAreaSize(player, area, resadmin))
	    return false;

	if (!resadmin && Residence.getConfigManager().getEnforceAreaInsideArea() && this.getParent() == null) {
	    boolean inside = false;
	    for (CuboidArea are : areas.values()) {
		if (are.isAreaWithinArea(area)) {
		    inside = true;
		}
	    }
	    if (!inside) {
		return false;
	    }
	}
	if (!area.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaDiffWorld"));
	    }
	    return false;
	}
	if (parent == null) {
	    String collideResidence = Residence.getResidenceManager().checkAreaCollision(area, this);
	    if (collideResidence != null) {
		if (player != null) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaCollision", ChatColor.YELLOW + collideResidence));
		    CuboidArea oldArea = Residence.getResidenceManager().getByName(collideResidence).getAreaArray()[0];
		    if (oldArea != null) {
			Residence.getSelectionManager().NewMakeBorders(player, oldArea.lowPoints, oldArea.highPoints, true);

			Residence.getSelectionManager().NewMakeBorders(player, area.lowPoints, area.highPoints, false);
		    }
		}
		return false;
	    }
	} else {
	    String[] szs = parent.listSubzones();
	    for (String sz : szs) {
		ClaimedResidence res = parent.getSubzone(sz);
		if (res != null && res != this) {
		    if (res.checkCollision(area)) {
			if (player != null) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaSubzoneCollision", ChatColor.YELLOW + sz));
			}
			return false;
		    }
		}
	    }
	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return false;
	    }
	    if (parent != null) {
		if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNotWithinParent"));
		    return false;
		}
		if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ParentNoPermission"));
		    return false;
		}
	    }
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    if (!this.isSubzone() && !group.canCreateResidences() && !player.hasPermission("residence.create") ||
		this.isSubzone() && !group.canCreateResidences() && !player.hasPermission("residence.create.subzone")) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return false;
	    }

	    if (areas.size() >= group.getMaxPhysicalPerResidence()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaMaxPhysical"));
		return false;
	    }
	    if (!group.inLimits(area)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaSizeLimit"));
		return false;
	    }
	    if (group.getMinHeight() > area.getLowLoc().getBlockY()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaLowLimit", ChatColor.YELLOW + String.format("%d", group.getMinHeight())));
		return false;
	    }
	    if (group.getMaxHeight() < area.getHighLoc().getBlockY()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaHighLimit", ChatColor.YELLOW + String.format("%d", group.getMaxHeight())));
		return false;
	    }
	    if (parent == null && Residence.getConfigManager().enableEconomy()) {
		int chargeamount = (int) Math.ceil((double) area.getSize() * group.getCostPerBlock());
		if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
		    return false;
		}
	    }
	}
	Residence.getResidenceManager().removeChunkList(getName());
	areas.put(name, area);
	Residence.getResidenceManager().calculateChunks(getName());
	if (player != null) {
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("AreaCreate", ChatColor.YELLOW + name));
	}
	return true;
    }

    public boolean replaceArea(CuboidArea neware, String name) {
	return this.replaceArea(null, neware, name, true);
    }

    public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {
	if (!areas.containsKey(name)) {
	    if (player != null)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
	    return false;
	}
	CuboidArea oldarea = areas.get(name);
	if (!newarea.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
	    if (player != null)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaDiffWorld"));
	    return false;
	}
	if (parent == null) {
	    String collideResidence = Residence.getResidenceManager().checkAreaCollision(newarea, this);
	    if (collideResidence != null && player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaCollision", ChatColor.YELLOW + collideResidence));
		CuboidArea area = Residence.getResidenceManager().getByName(collideResidence).getAreaArray()[0];
		Residence.getSelectionManager().NewMakeBorders(player, area.getLowLoc(), area.highPoints, true);
		return false;
	    }
	} else {
	    String[] szs = parent.listSubzones();
	    for (String sz : szs) {
		ClaimedResidence res = parent.getSubzone(sz);
		if (res != null && res != this) {
		    if (res.checkCollision(newarea)) {
			if (player != null) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaSubzoneCollision", ChatColor.YELLOW + sz));
			    Residence.getSelectionManager().NewMakeBorders(player, res.getAreaArray()[0].lowPoints, res.getAreaArray()[0].highPoints, true);
			}
			return false;
		    }
		}
	    }
	}
	// Remove subzones that are not in the area anymore
	String[] szs = listSubzones();
	for (String sz : szs) {
	    ClaimedResidence res = getSubzone(sz);
	    if (res == null || res == this)
		continue;
	    String[] szareas = res.getAreaList();
	    for (String area : szareas) {
		if (newarea.isAreaWithinArea(res.getArea(area)))
		    continue;

		boolean good = false;
		for (CuboidArea arae : getAreaArray()) {
		    if (arae != oldarea && arae.isAreaWithinArea(res.getArea(area))) {
			good = true;
		    }
		}
		if (!good) {
		    res.removeArea(area);
		}

	    }
	    if (res.getAreaArray().length == 0) {
		removeSubzone(sz);
	    }

	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		Debug.D("1");
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		Debug.D("2");
		return false;
	    }
	    if (parent != null) {
		if (!parent.containsLoc(newarea.getHighLoc()) || !parent.containsLoc(newarea.getLowLoc())) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNotWithinParent"));
		    return false;
		}
		if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ParentNoPermission"));
		    return false;
		}
	    }
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    if (!group.canCreateResidences() && !player.hasPermission("residence.resize")) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return false;
	    }
	    if (!group.inLimits(newarea)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaSizeLimit"));
		return false;
	    }
	    if (group.getMinHeight() > newarea.getLowLoc().getBlockY()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaLowLimit", ChatColor.YELLOW + String.format("%d", group.getMinHeight())));
		return false;
	    }
	    if (group.getMaxHeight() < newarea.getHighLoc().getBlockY()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaHighLimit", ChatColor.YELLOW + String.format("%d", group.getMaxHeight())));
		return false;
	    }
	    if (parent == null && Residence.getConfigManager().enableEconomy()) {
		int chargeamount = (int) Math.ceil((double) (newarea.getSize() - oldarea.getSize()) * group.getCostPerBlock());
		if (chargeamount > 0) {
		    if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
			return false;
		    }
		}
	    }

	}
	Residence.getResidenceManager().removeChunkList(getName());
	areas.remove(name);
	areas.put(name, newarea);
	Residence.getResidenceManager().calculateChunks(getName());
	if (player != null)
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("AreaUpdate"));
	return true;
    }

    public boolean addSubzone(String name, Location loc1, Location loc2) {
	return this.addSubzone(null, loc1, loc2, name, true);
    }

    public boolean addSubzone(Player player, Location loc1, Location loc2, String name, boolean resadmin) {
	if (player == null) {
	    return this.addSubzone(null, Residence.getServerLandname(), loc1, loc2, name, resadmin);
	} else {
	    return this.addSubzone(player, player.getName(), loc1, loc2, name, resadmin);
	}
    }

    public boolean addSubzone(Player player, String owner, Location loc1, Location loc2, String name, boolean resadmin) {
	if (!Residence.validName(name)) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
	    }
	    return false;
	}
	if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SubzoneSelectInside"));
	    }
	    return false;
	}
	if (subzones.containsKey(name)) {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SubzoneExists", ChatColor.YELLOW + name));
	    }
	    return false;
	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		if (!this.perms.playerHas(player.getName(), "subzone", this.perms.playerHas(player.getName(), "admin", false))) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return false;
		}
	    }
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    if (this.getZoneDepth() >= group.getMaxSubzoneDepth(owner)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SubzoneMaxDepth"));
		return false;
	    }
	}
	CuboidArea newArea = new CuboidArea(loc1, loc2);
	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	for (Entry<String, ClaimedResidence> resEntry : set) {
	    ClaimedResidence res = resEntry.getValue();
	    if (res.checkCollision(newArea)) {
		if (player != null) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SubzoneCollide", ChatColor.YELLOW + resEntry.getKey()));
		    if (res.getAreaArray().length > 0) {
			CuboidArea oldArea = res.getAreaArray()[0];
			Residence.getSelectionManager().NewMakeBorders(player, oldArea.lowPoints, oldArea.highPoints, true);
			Residence.getSelectionManager().NewMakeBorders(player, newArea.lowPoints, newArea.highPoints, false);
		    }

		}
		return false;
	    }
	}
	ClaimedResidence newres;
	if (player != null) {
	    newres = new ClaimedResidence(owner, perms.getWorld(), this, plugin);
	    newres.addArea(player, newArea, name, resadmin);
	} else {
	    newres = new ClaimedResidence(owner, perms.getWorld(), this, plugin);
	    newres.addArea(newArea, name);
	}
	if (newres.getAreaCount() != 0) {
	    newres.getPermissions().applyDefaultFlags();
	    if (player != null) {
		PermissionGroup group = Residence.getPermissionManager().getGroup(player);
		newres.setEnterMessage(group.getDefaultEnterMessage());
		newres.setLeaveMessage(group.getDefaultLeaveMessage());
	    }
	    if (Residence.getConfigManager().flagsInherit()) {
		newres.getPermissions().setParent(perms);
	    }
	    subzones.put(name, newres);
	    if (player != null)
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SubzoneCreate", ChatColor.YELLOW + name));
	    return true;
	} else {
	    if (player != null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SubzoneCreateFail", ChatColor.YELLOW + name));
	    }
	    return false;
	}
    }

    public String getSubzoneNameByLoc(Location loc) {
	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	ClaimedResidence res = null;
	String key = null;
	for (Entry<String, ClaimedResidence> entry : set) {
	    if (entry.getValue().containsLoc(loc)) {
		key = entry.getKey();
		res = entry.getValue();
		break;
	    }
	}
	if (key == null || res == null)
	    return null;

	String subname = res.getSubzoneNameByLoc(loc);
	if (subname != null) {
	    return key + "." + subname;
	}
	return key;
    }

    public ClaimedResidence getSubzoneByLoc(Location loc) {
	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	ClaimedResidence res = null;
	for (Entry<String, ClaimedResidence> entry : set) {
	    if (entry.getValue().containsLoc(loc)) {
		res = entry.getValue();
		break;
	    }
	}
	if (res == null)
	    return null;

	ClaimedResidence subrez = res.getSubzoneByLoc(loc);
	if (subrez == null) {
	    return res;
	}
	return subrez;
    }

    public ClaimedResidence getSubzone(String subzonename) {
	if (!subzonename.contains(".")) {
	    return subzones.get(subzonename);
	}
	String split[] = subzonename.split("\\.");
	ClaimedResidence get = subzones.get(split[0]);
	for (int i = 1; i < split.length; i++) {
	    if (get == null) {
		return null;
	    }
	    get = get.getSubzone(split[i]);
	}
	return get;
    }

    public ClaimedResidence getSubzoneNoCase(String subzonename) {
	if (!subzonename.contains(".")) {
	    for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
		if (one.getKey().equalsIgnoreCase(subzonename))
		    return one.getValue();
	    }
	}
	String split[] = subzonename.split("\\.");

	ClaimedResidence get = null;
	for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
	    if (one.getKey().equalsIgnoreCase(split[0]))
		get = one.getValue();
	}

	for (int i = 1; i < split.length; i++) {
	    if (get == null) {
		return null;
	    }
	    get = get.getSubzoneNoCase(split[i]);
	}
	return get;
    }

    public String getSubzoneNameByRes(ClaimedResidence res) {
	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	for (Entry<String, ClaimedResidence> entry : set) {
	    if (entry.getValue() == res) {
		return entry.getKey();
	    }
	    String n = entry.getValue().getSubzoneNameByRes(res);
	    if (n != null) {
		return entry.getKey() + "." + n;
	    }
	}
	return null;
    }

    public String[] getSubzoneList() {
	ArrayList<String> zones = new ArrayList<>();
	Set<String> set = subzones.keySet();
	for (String key : set) {
	    if (key != null) {
		zones.add(key);
	    }
	}
	return zones.toArray(new String[zones.size()]);
    }

    public boolean checkCollision(CuboidArea area) {
	Set<String> set = areas.keySet();
	for (String key : set) {
	    CuboidArea checkarea = areas.get(key);
	    if (checkarea != null) {
		if (checkarea.checkCollision(area)) {
		    return true;
		}
	    }
	}
	return false;
    }

    public boolean containsLoc(Location loc) {
	Collection<CuboidArea> keys = areas.values();
	for (CuboidArea key : keys) {
	    if (key.containsLoc(loc)) {
		if (parent != null)
		    return parent.containsLoc(loc);
		return true;
	    }
	}
	return false;
    }

    public ClaimedResidence getParent() {
	return parent;
    }

    public ClaimedResidence getTopParent() {
	if (parent == null)
	    return this;
	return parent.getTopParent();
    }

    public boolean removeSubzone(String name) {
	return this.removeSubzone(null, name, true);
    }

    public boolean removeSubzone(Player player, String name, boolean resadmin) {
	ClaimedResidence res = subzones.get(name);
	if (player != null && !res.perms.hasResidencePermission(player, true) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return false;
	}
	subzones.remove(name);
	if (player != null) {
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SubzoneRemove", ChatColor.YELLOW + name + ChatColor.GREEN));
	}
	return true;
    }

    public long getTotalSize() {
	Collection<CuboidArea> set = areas.values();
	long size = 0;
	if (!Residence.getConfigManager().isNoCostForYBlocks())
	    for (CuboidArea entry : set) {
		size = size + entry.getSize();
	    }
	else
	    for (CuboidArea entry : set) {
		size = size + (entry.getXSize() * entry.getZSize());
	    }
	return size;
    }

    public CuboidArea[] getAreaArray() {
	CuboidArea[] temp = new CuboidArea[areas.size()];
	int i = 0;
	for (CuboidArea area : areas.values()) {
	    temp[i] = area;
	    i++;
	}
	return temp;
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

    public String getShopDesc() {
	return ShopDesc;
    }

    public void setEnterMessage(String message) {
	enterMessage = message;
    }

    public void setLeaveMessage(String message) {
	leaveMessage = message;
    }

    public void setShopDesc(String message) {
	ShopDesc = message;
    }

    public void setEnterLeaveMessage(Player player, String message, boolean enter, boolean resadmin) {
	// if(message!=null &&
	// Residence.getConfigManager().getResidenceNameRegex() != null) {
	// Removed pending further action
	// player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidCharacters"));
	// return;
	// }
	if (message != null) {
	    if (message.equals("")) {
		message = null;
	    }
	}
	PermissionGroup group = Residence.getPermissionManager().getGroup(perms.getOwner(), perms.getWorld());
	if (!group.canSetEnterLeaveMessages() && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("OwnerNoPermission"));
	    return;
	}
	if (!perms.hasResidencePermission(player, false) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return;
	}
	if (enter) {
	    this.setEnterMessage(message);
	} else {
	    this.setLeaveMessage(message);
	}
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("MessageChange"));
    }

    public Location getMiddleFreeLoc(Location insideLoc) {
	CuboidArea area = this.getAreaByLoc(insideLoc);
	if (area == null) {
	    return insideLoc;
	}

	int y = area.getHighLoc().getBlockY();

	int x = area.getLowLoc().getBlockX() + (int) (area.getXSize() / 2);
	int z = area.getLowLoc().getBlockZ() + (int) (area.getZSize() / 2);

	Location newLoc = new Location(area.getWorld(), x + 0.5, y, z + 0.5);
	boolean found = false;
	int it = 0;
	int maxIt = area.getWorld().getMaxHeight() - 63;
	while (it < maxIt) {
	    it++;
	    newLoc.setY(newLoc.getY() - 1);

	    if (newLoc.getBlockY() < 63)
		break;

	    Block block = newLoc.getBlock();
	    Block block2 = newLoc.clone().add(0, 1, 0).getBlock();
	    Block block3 = newLoc.clone().add(0, -1, 0).getBlock();
	    if (Residence.getNms().isEmptyBlock(block) && Residence.getNms().isEmptyBlock(block2) && !Residence.getNms().isEmptyBlock(block3)) {
		found = true;
		break;
	    }
	}
	if (found) {
	    return newLoc;
	} else {
	    return getOutsideFreeLoc(insideLoc);
	}
    }

    @SuppressWarnings("deprecation")
    public Location getOutsideFreeLoc(Location insideLoc) {
	int maxIt = 100;
	CuboidArea area = this.getAreaByLoc(insideLoc);
	if (area == null) {
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
	    lowLoc = new Location(newLoc.getWorld(), newLoc.getBlockX(), 254, newLoc.getBlockZ());
	    newLoc.setY(255);
	    while ((newLoc.getBlock().getTypeId() != 0 || lowLoc.getBlock().getTypeId() == 0) && lowLoc.getBlockY() > -126) {
		newLoc.setY(newLoc.getY() - 1);
		lowLoc.setY(lowLoc.getY() - 1);
	    }
	    if (newLoc.getBlock().getTypeId() == 0 && lowLoc.getBlock().getTypeId() != 0) {
		found = true;
	    }
	}
	if (found) {
	    return newLoc;
	} else {
	    World world = Residence.getServ().getWorld(perms.getWorld());
	    if (world != null) {
		return world.getSpawnLocation();
	    }
	    return insideLoc;
	}
    }

    protected CuboidArea getAreaByLoc(Location loc) {
	for (CuboidArea thisarea : areas.values()) {
	    if (thisarea.containsLoc(loc)) {
		return thisarea;
	    }
	}
	return null;
    }

    public String[] listSubzones() {
	String list[] = new String[subzones.size()];
	int i = 0;
	for (String res : subzones.keySet()) {
	    list[i] = res;
	    i++;
	}
	return list;
    }

    public void printSubzoneList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
	    temp.add(ChatColor.GREEN + sz.getKey() + ChatColor.YELLOW + " - " + Residence.getLanguage().getPhrase("Owner") + ": " + sz.getValue().getOwner());
	}
	InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Subzones"), temp, page);
    }

    public void printAreaList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (String area : areas.keySet()) {
	    temp.add(area);
	}
	InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
    }

    public void printAdvancedAreaList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (Entry<String, CuboidArea> entry : areas.entrySet()) {
	    CuboidArea a = entry.getValue();
	    Location h = a.getHighLoc();
	    Location l = a.getLowLoc();
	    temp.add(ChatColor.GREEN + "{" + ChatColor.YELLOW + "ID:" + ChatColor.RED + entry.getKey() + " " + ChatColor.YELLOW + "P1:" + ChatColor.RED + "(" + h
		.getBlockX() + "," + h.getBlockY() + "," + h.getBlockZ() + ") " + ChatColor.YELLOW + "P2:" + ChatColor.RED + "(" + l.getBlockX() + "," + l.getBlockY()
		+ "," + l.getBlockZ() + ") " + ChatColor.YELLOW + "(Size:" + ChatColor.RED + a.getSize() + ChatColor.YELLOW + ")" + ChatColor.GREEN + "} ");
	}
	InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
    }

    public String[] getAreaList() {
	String arealist[] = new String[areas.size()];
	int i = 0;
	for (Entry<String, CuboidArea> entry : areas.entrySet()) {
	    arealist[i] = entry.getKey();
	    i++;
	}
	return arealist;
    }

    public int getZoneDepth() {
	int count = 0;
	ClaimedResidence res = parent;
	while (res != null) {
	    count++;
	    res = res.getParent();
	}
	return count;
    }

    public void setTpLoc(Player player, boolean resadmin) {
	if (!this.perms.hasResidencePermission(player, false) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return;
	}
	if (!this.containsLoc(player.getLocation())) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotInResidence"));
	    return;
	}
	tpLoc = player.getLocation();
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SetTeleportLocation"));
    }

    public int isSafeTp(Player player) {
	if (player.getAllowFlight())
	    return 0;

	if (player.getGameMode() == GameMode.CREATIVE)
	    return 0;

	if (Residence.getNms().isSpectator(player.getGameMode()))
	    return 0;

	if (tpLoc == null)
	    return 0;

	Location tempLoc = new Location(tpLoc.getWorld(), tpLoc.getX(), tpLoc.getY(), tpLoc.getZ());

	int from = (int) tempLoc.getY();

	int fallDistance = 0;
	for (int i = 0; i < 255; i++) {
	    tempLoc.setY(from - i);
	    Block block = tempLoc.getBlock();
	    if (Residence.getNms().isEmptyBlock(block)) {
		fallDistance++;
	    } else {
		break;
	    }
	}
	return fallDistance;
    }

    public void tpToResidence(Player reqPlayer, final Player targetPlayer, boolean resadmin) {
	boolean isAdmin = Residence.isResAdminOn(reqPlayer);
	if (!resadmin && !isAdmin && !reqPlayer.hasPermission("residence.tpbypass")) {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(reqPlayer);
	    if (!group.hasTpAccess()) {
		reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("TeleportDeny"));
		return;
	    }
	    if (!reqPlayer.equals(targetPlayer)) {
		reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return;
	    }
	    if (!this.perms.playerHas(reqPlayer.getName(), "tp", true)) {
		reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("TeleportNoFlag"));
		return;
	    }
	    if (!this.perms.playerHas(reqPlayer.getName(), "move", true)) {
		reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceMoveDeny", this.getName()));
		return;
	    }
	}

	if (!ResidenceCommandListener.getTeleportMap().containsKey(targetPlayer.getName()) && !isAdmin) {
	    int distance = isSafeTp(reqPlayer);
	    if (distance > 6) {
		reqPlayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("TeleportConfirm", String.valueOf(distance)));
		ResidenceCommandListener.getTeleportMap().put(reqPlayer.getName(), this);
		return;
	    }
	}

	if (Residence.getConfigManager().getTeleportDelay() > 0 && !isAdmin && !resadmin) {
	    reqPlayer.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("TeleportStarted", this.getName() + "|" + Residence.getConfigManager()
		.getTeleportDelay()));
	    ResidenceCommandListener.getTeleportDelayMap().add(reqPlayer.getName());
	}

	if (tpLoc != null) {
	    if (Residence.getConfigManager().getTeleportDelay() > 0 && !isAdmin)
		performDelaydTp(tpLoc, targetPlayer, reqPlayer, true);
	    else
		performInstantTp(tpLoc, targetPlayer, reqPlayer, true);
	} else {
	    CuboidArea area = areas.values().iterator().next();
	    if (area == null) {
		reqPlayer.sendMessage(ChatColor.RED + "Could not find area to teleport to...");
		ResidenceCommandListener.getTeleportDelayMap().remove(targetPlayer.getName());
		return;
	    }
	    final Location targloc = this.getMiddleFreeLoc(area.getHighLoc());
	    if (Residence.getConfigManager().getTeleportDelay() > 0 && !isAdmin)
		performDelaydTp(targloc, targetPlayer, reqPlayer, true);
	    else
		performInstantTp(targloc, targetPlayer, reqPlayer, true);

	}
    }

    public void performDelaydTp(final Location targloc, final Player targetPlayer, Player reqPlayer, final boolean near) {
	ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
	Residence.getServ().getPluginManager().callEvent(tpevent);
	if (!tpevent.isCancelled()) {
	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		public void run() {
		    if (!ResidenceCommandListener.getTeleportDelayMap().contains(targetPlayer.getName()) && Residence.getConfigManager().getTeleportDelay() > 0)
			return;
		    else if (ResidenceCommandListener.getTeleportDelayMap().contains(targetPlayer.getName()))
			ResidenceCommandListener.getTeleportDelayMap().remove(targetPlayer.getName());
		    targetPlayer.teleport(targloc);
		    if (near)
			targetPlayer.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("TeleportNear"));
		    else
			targetPlayer.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("TeleportSuccess"));
		    return;
		}
	    }, Residence.getConfigManager().getTeleportDelay() * 20L);
	}
    }

    private void performInstantTp(final Location targloc, final Player targetPlayer, Player reqPlayer, final boolean near) {
	ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
	Residence.getServ().getPluginManager().callEvent(tpevent);
	if (!tpevent.isCancelled()) {
	    targetPlayer.teleport(targloc);
	    if (near)
		targetPlayer.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("TeleportNear"));
	    else
		targetPlayer.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("TeleportSuccess"));

	}
    }

    public String getAreaIDbyLoc(Location loc) {
	for (Entry<String, CuboidArea> area : areas.entrySet()) {
	    if (area.getValue().containsLoc(loc))
		return area.getKey();
	}
	return null;
    }

    public CuboidArea getCuboidAreabyName(String name) {
	for (Entry<String, CuboidArea> area : areas.entrySet()) {
	    if (area.getKey().equals(name))
		return area.getValue();
	}
	return null;
    }

    public void removeArea(String id) {
	Residence.getResidenceManager().removeChunkList(getName());
	areas.remove(id);
	Residence.getResidenceManager().calculateChunks(getName());
    }

    public void removeArea(Player player, String id, boolean resadmin) {
	if (this.getPermissions().hasResidencePermission(player, true) || resadmin) {
	    if (!areas.containsKey(id)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
		return;
	    }
	    if (areas.size() == 1 && !Residence.getConfigManager().allowEmptyResidences()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaRemoveLast"));
		return;
	    }
	    removeArea(id);
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("AreaRemove"));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	}
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new HashMap<>();
	Map<String, Object> areamap = new HashMap<>();
	root.put("EnterMessage", enterMessage);
	root.put("LeaveMessage", leaveMessage);
	root.put("ShopDescription", ShopDesc);
	root.put("StoredMoney", bank.getStoredMoney());
	root.put("BlockSellPrice", BlockSellPrice);
	root.put("ChatPrefix", ChatPrefix);
	root.put("ChannelColor", ChannelColor.name());
	root.put("BlackList", blacklist.save());
	root.put("IgnoreList", ignorelist.save());
	for (Entry<String, CuboidArea> entry : areas.entrySet()) {
	    areamap.put(entry.getKey(), entry.getValue().save());
	}
	root.put("Areas", areamap);
	Map<String, Object> subzonemap = new HashMap<>();
	for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
	    subzonemap.put(sz.getKey(), sz.getValue().save());
	}
	root.put("Subzones", subzonemap);
	root.put("Permissions", perms.save());
	DecimalFormat formatter = new DecimalFormat("#0.00");
	if (tpLoc != null) {
	    Map<String, Object> tpmap = new HashMap<>();
	    tpmap.put("X", Double.valueOf(formatter.format(tpLoc.getX())));
	    tpmap.put("Y", Double.valueOf(formatter.format(tpLoc.getY())));
	    tpmap.put("Z", Double.valueOf(formatter.format(tpLoc.getZ())));
	    tpmap.put("Pitch", Double.valueOf(formatter.format(tpLoc.getPitch())));
	    tpmap.put("Yaw", Double.valueOf(formatter.format(tpLoc.getYaw())));
	    root.put("TPLoc", tpmap);
	}
	return root;
    }

    @SuppressWarnings("unchecked")
    public static ClaimedResidence load(Map<String, Object> root, ClaimedResidence parent, Residence plugin) throws Exception {
	ClaimedResidence res = new ClaimedResidence(plugin);
	if (root == null)
	    throw new Exception("Null residence!");

	res.enterMessage = (String) root.get("EnterMessage");
	res.leaveMessage = (String) root.get("LeaveMessage");

	if (root.containsKey("ShopDescription"))
	    res.setShopDesc((String) root.get("ShopDescription"));

	if (root.containsKey("StoredMoney"))
	    res.bank.setStoredMoney((Integer) root.get("StoredMoney"));

	if (root.containsKey("BlackList"))
	    res.blacklist = ResidenceItemList.load(res, (Map<String, Object>) root.get("BlackList"));
	if (root.containsKey("IgnoreList"))
	    res.ignorelist = ResidenceItemList.load(res, (Map<String, Object>) root.get("IgnoreList"));

	Map<String, Object> areamap = (Map<String, Object>) root.get("Areas");
	res.perms = ResidencePermissions.load(res, (Map<String, Object>) root.get("Permissions"));

	if (root.containsKey("BlockSellPrice"))
	    res.BlockSellPrice = (Double) root.get("BlockSellPrice");
	else {
	    PermissionGroup group = Residence.getPermissionManager().getGroup(res.getOwner(), res.getWorld());
	    res.BlockSellPrice = group.getSellPerBlock();
	}

	World world = Residence.getServ().getWorld(res.perms.getWorld());
	if (world == null)
	    throw new Exception("Cant Find World: " + res.perms.getWorld());
	for (Entry<String, Object> map : areamap.entrySet()) {
	    res.areas.put(map.getKey(), CuboidArea.load((Map<String, Object>) map.getValue(), world));
	}

	Map<String, Object> subzonemap = (Map<String, Object>) root.get("Subzones");
	for (Entry<String, Object> map : subzonemap.entrySet()) {
	    ClaimedResidence subres = ClaimedResidence.load((Map<String, Object>) map.getValue(), res, plugin);
	    if (Residence.getConfigManager().flagsInherit())
		subres.getPermissions().setParent(res.getPermissions());
	    res.subzones.put(map.getKey(), subres);
	}

	res.parent = parent;
	Map<String, Object> tploc = (Map<String, Object>) root.get("TPLoc");
	if (tploc != null) {
	    double pitch = 0.0;
	    double yaw = 0.0;

	    if (tploc.containsKey("Yaw"))
		yaw = Double.valueOf(tploc.get("Yaw").toString());

	    if (tploc.containsKey("Pitch"))
		pitch = Double.valueOf(tploc.get("Pitch").toString());

	    res.tpLoc = new Location(world, Double.valueOf(tploc.get("X").toString()), Double.valueOf(tploc.get("Y").toString()), Double.valueOf(tploc.get("Z")
		.toString()));
	    res.tpLoc.setPitch((float) pitch);
	    res.tpLoc.setYaw((float) yaw);
	}

	if (root.containsKey("ChatPrefix"))
	    res.ChatPrefix = (String) root.get("ChatPrefix");

	if (root.containsKey("ChannelColor"))
	    res.ChannelColor = ChatColor.valueOf((String) root.get("ChannelColor"));
	else {
	    res.ChannelColor = Residence.getConfigManager().getChatColor();
	}

	return res;
    }

    public int getAreaCount() {
	return areas.size();
    }

    public boolean renameSubzone(String oldName, String newName) {
	return this.renameSubzone(null, oldName, newName, true);
    }

    public boolean renameSubzone(Player player, String oldName, String newName, boolean resadmin) {
	if (!Residence.validName(newName)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
	    return false;
	}
	ClaimedResidence res = subzones.get(oldName);
	if (res == null) {
	    if (player != null)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidSubzone"));
	    return false;
	}
	if (player != null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return false;
	}
	if (subzones.containsKey(newName)) {
	    if (player != null)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SubzoneExists", ChatColor.YELLOW + newName));
	    return false;
	}
	subzones.put(newName, res);
	subzones.remove(oldName);
	if (player != null)
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SubzoneRename", oldName + "|" + newName));
	return true;
    }

    public boolean renameArea(String oldName, String newName) {
	return this.renameArea(null, oldName, newName, true);
    }

    public boolean renameArea(Player player, String oldName, String newName, boolean resadmin) {
	if (!Residence.validName(newName)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
	    return false;
	}
	if (player == null || perms.hasResidencePermission(player, true) || resadmin) {
	    if (areas.containsKey(newName)) {
		if (player != null)
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaExists"));
		return false;
	    }
	    CuboidArea area = areas.get(oldName);
	    if (area == null) {
		if (player != null)
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaInvalidName"));
		return false;
	    }
	    areas.put(newName, area);
	    areas.remove(oldName);
	    if (player != null)
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("AreaRename", oldName + "|" + newName));
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return false;
	}
    }

    public CuboidArea getArea(String name) {
	return areas.get(name);
    }

    public String getName() {
	return Residence.getResidenceManager().getNameByRes(this);
    }

    public void remove() {
	String name = getName();
	if (name != null) {
	    Residence.getResidenceManager().removeResidence(name);
	    Residence.getResidenceManager().removeChunkList(name);
	}
    }

    public ResidenceBank getBank() {
	return bank;
    }

    public String getWorld() {
	return perms.getWorld();
    }

    public String getOwner() {
	return perms.getOwner();
    }

    public boolean isOwner(String name) {
	return perms.getOwner().equals(name);
    }

    public boolean isOwner(Player p) {
	if (Residence.getConfigManager().isOfflineMode())
	    return isOwner(p.getName());
	return perms.getOwnerUUID().equals(p.getUniqueId());
    }

    public void setChatPrefix(String ChatPrefix) {
	this.ChatPrefix = ChatPrefix;
    }

    public String getChatPrefix() {
	return this.ChatPrefix == null ? "" : this.ChatPrefix;
    }

    public void setChannelColor(ChatColor ChannelColor) {
	this.ChannelColor = ChannelColor;
    }

    public ChatColor getChannelColor() {
	return ChannelColor;
    }

    public UUID getOwnerUUID() {
	return perms.getOwnerUUID();
    }

    public ResidenceItemList getItemBlacklist() {
	return blacklist;
    }

    public ResidenceItemList getItemIgnoreList() {
	return ignorelist;
    }

    public Double getBlockSellPrice() {
	return BlockSellPrice;
    }

    public ArrayList<Player> getPlayersInResidence() {
	ArrayList<Player> within = new ArrayList<>();
	for (Player player : Bukkit.getServer().getOnlinePlayers()) {
	    if (this.containsLoc(player.getLocation())) {
		within.add(player);
	    }
	}
	return within;
    }
}

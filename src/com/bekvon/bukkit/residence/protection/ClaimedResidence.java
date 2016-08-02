package com.bekvon.bukkit.residence.protection;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.containers.RandomLoc;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceAreaAddEvent;
import com.bekvon.bukkit.residence.event.ResidenceAreaDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceSizeChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceSubzoneCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.itemlist.ItemList.ListType;
import com.bekvon.bukkit.residence.itemlist.ResidenceItemList;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimedResidence {

    private String resName;
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
    protected boolean mainRes = false;
    protected long createTime = 0L;

    protected List<String> cmdWhiteList = new ArrayList<String>();
    protected List<String> cmdBlackList = new ArrayList<String>();

    protected RentableLand rentableland = null;
    protected RentedLand rentedland = null;

    protected Integer sellPrice = -1;

    private Residence plugin;

    public String getResidenceName() {
	return resName;
    }

    public void setName(String name) {
	if (name.contains("."))
	    resName = name.split("\\.")[name.split("\\.").length - 1];
	else
	    resName = name;
    }

    public void setCreateTime() {
	createTime = System.currentTimeMillis();
    }

    public long getCreateTime() {
	return createTime;
    }

    public Integer getSellPrice() {
	return sellPrice;
    }

    public void setSellPrice(Integer amount) {
	sellPrice = amount;
    }

    public boolean isForSell() {
	return Residence.getTransactionManager().isForSale(this.getName());
    }

    public boolean isForRent() {
	return Residence.getRentManager().isForRent(this);
    }

    public boolean isSubzoneForRent() {
	for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
	    if (one.getValue().isForRent())
		return true;
	    if (one.getValue().isSubzoneForRent())
		return true;
	}
	return false;
    }

    public boolean isSubzoneRented() {
	for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
	    if (one.getValue().isRented())
		return true;
	    if (one.getValue().isSubzoneRented())
		return true;
	}
	return false;
    }

    public ClaimedResidence getRentedSubzone() {
	for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
	    if (one.getValue().isRented())
		return one.getValue();
	    if (one.getValue().getRentedSubzone() != null)
		return one.getValue().getRentedSubzone();
	}
	return null;
    }

    public boolean isParentForRent() {
	if (this.getParent() != null)
	    return this.getParent().isForRent() ? true : this.getParent().isParentForRent();
	return false;
    }

    public boolean isParentForSell() {
	if (this.getParent() != null)
	    return this.getParent().isForSell() ? true : this.getParent().isParentForSell();
	return false;
    }

    public boolean isRented() {
	return Residence.getRentManager().isRented(this);
    }

    public void setRentable(RentableLand rl) {
	this.rentableland = rl;
    }

    public RentableLand getRentable() {
	return this.rentableland;
    }

    public void setRented(RentedLand rl) {
	this.rentedland = rl;
    }

    public RentedLand getRentedLand() {
	return this.rentedland;
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

    public ClaimedResidence(Residence plugin) {
	subzones = new HashMap<>();
	areas = new HashMap<>();
	bank = new ResidenceBank(this);
	blacklist = new ResidenceItemList(this, ListType.BLACKLIST);
	ignorelist = new ResidenceItemList(this, ListType.IGNORELIST);
	this.plugin = plugin;
    }

    public boolean isMainResidence() {
	return mainRes;
    }

    public void setMainResidence(boolean state) {
	mainRes = state;
    }

    public boolean isSubzone() {
	return parent == null ? false : true;
    }

    public int getSubzoneDeep() {
	return getSubzoneDeep(0);
    }

    public int getSubzoneDeep(int deep) {
	deep++;
	if (parent != null) {
	    return parent.getSubzoneDeep(deep);
	}
	return deep;
    }

    public boolean isBiggerThanMin(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() < group.getMinX()) {
	    Residence.msg(player, lm.Area_ToSmallX, area.getXSize(), group.getMinX());
	    return false;
	}
	if (area.getYSize() < group.getMinY()) {
	    Residence.msg(player, lm.Area_ToSmallY, area.getYSize(), group.getMinY());
	    return false;
	}
	if (area.getZSize() < group.getMinZ()) {
	    Residence.msg(player, lm.Area_ToSmallZ, area.getZSize(), group.getMinZ());
	    return false;
	}
	return true;
    }

    public boolean isBiggerThanMinSubzone(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() < group.getSubzoneMinX()) {
	    Residence.msg(player, lm.Area_ToSmallX, area.getXSize(), group.getSubzoneMinX());
	    return false;
	}
	if (area.getYSize() < group.getSubzoneMinY()) {
	    Residence.msg(player, lm.Area_ToSmallY, area.getYSize(), group.getSubzoneMinY());
	    return false;
	}
	if (area.getZSize() < group.getSubzoneMinZ()) {
	    Residence.msg(player, lm.Area_ToSmallZ, area.getZSize(), group.getSubzoneMinZ());
	    return false;
	}
	return true;
    }

    public boolean isSmallerThanMax(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() > group.getMaxX()) {
	    Residence.msg(player, lm.Area_ToBigX, area.getXSize(), group.getMaxX());
	    return false;
	}
	if (area.getYSize() > group.getMaxY()) {
	    Residence.msg(player, lm.Area_ToBigY, area.getYSize(), group.getMaxY());
	    return false;
	}
	if (area.getZSize() > group.getMaxZ()) {
	    Residence.msg(player, lm.Area_ToBigZ, area.getZSize(), group.getMaxZ());
	    return false;
	}
	return true;
    }

    public boolean isSmallerThanMaxSubzone(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() > group.getSubzoneMaxX()) {
	    Residence.msg(player, lm.Area_ToBigX, area.getXSize(), group.getSubzoneMaxX());
	    return false;
	}
	if (area.getYSize() > group.getSubzoneMaxY()) {
	    Residence.msg(player, lm.Area_ToBigY, area.getYSize(), group.getSubzoneMaxY());
	    return false;
	}
	if (area.getZSize() > group.getSubzoneMaxZ()) {
	    Residence.msg(player, lm.Area_ToBigZ, area.getZSize(), group.getSubzoneMaxZ());
	    return false;
	}
	return true;
    }

    public boolean addArea(CuboidArea area, String name) {
	return addArea(null, area, name, true);
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin) {
	return addArea(player, area, name, resadmin, true);
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin, boolean chargeMoney) {
	if (!Residence.validName(name)) {
	    if (player != null) {
		Residence.msg(player, lm.Invalid_NameCharacters);
	    }
	    return false;
	}

	String NName = name;
	name = name.toLowerCase();

	if (areas.containsKey(NName)) {
	    if (player != null) {
		Residence.msg(player, lm.Area_Exists);
	    }
	    return false;
	}

	if (this.isSubzone() && !isBiggerThanMinSubzone(player, area, resadmin) || !this.isSubzone() && !isBiggerThanMin(player, area, resadmin))
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
		Residence.msg(player, lm.Area_DiffWorld);
	    }
	    return false;
	}
	if (parent == null) {
	    String collideResidence = Residence.getResidenceManager().checkAreaCollision(area, this);
	    if (collideResidence != null) {
		if (player != null) {
		    Residence.msg(player, lm.Area_Collision, collideResidence);
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
			    Residence.msg(player, lm.Area_SubzoneCollision, sz);
			}
			return false;
		    }
		}
	    }
	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		Residence.msg(player, lm.General_NoPermission);
		return false;
	    }
	    if (parent != null) {
		if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
		    Residence.msg(player, lm.Area_NotWithinParent);
		    return false;
		}
		if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
		    Residence.msg(player, lm.Residence_ParentNoPermission);
		    return false;
		}
	    }

	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);

	    PermissionGroup group = rPlayer.getGroup();
	    if (!this.isSubzone() && !group.canCreateResidences() && !player.hasPermission("residence.create") ||
		this.isSubzone() && !group.canCreateResidences() && !player.hasPermission("residence.create.subzone")) {
		Residence.msg(player, lm.General_NoPermission);
		return false;
	    }

	    if (areas.size() >= group.getMaxPhysicalPerResidence()) {
		Residence.msg(player, lm.Area_MaxPhysical);
		return false;
	    }
	    if (!this.isSubzone() && !isSmallerThanMax(player, area, resadmin) || this.isSubzone() && !isSmallerThanMaxSubzone(player, area, resadmin)) {
		Residence.msg(player, lm.Area_SizeLimit);
		return false;
	    }
	    if (group.getMinHeight() > area.getLowLoc().getBlockY()) {
		Residence.msg(player, lm.Area_LowLimit, String.format("%d", group.getMinHeight()));
		return false;
	    }
	    if (group.getMaxHeight() < area.getHighLoc().getBlockY()) {
		Residence.msg(player, lm.Area_HighLimit, String.format("%d", group.getMaxHeight()));
		return false;
	    }
	    if (chargeMoney && parent == null && Residence.getConfigManager().enableEconomy() && !resadmin) {
		int chargeamount = (int) Math.ceil(area.getSize() * group.getCostPerBlock());
		if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
		    return false;
		}
	    }
	}

	ResidenceAreaAddEvent resevent = new ResidenceAreaAddEvent(player, NName, this, area);
	Residence.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return false;

	Residence.getResidenceManager().removeChunkList(getName());
	areas.put(name, area);
	Residence.getResidenceManager().calculateChunks(getName());
	return true;
    }

    public boolean replaceArea(CuboidArea neware, String name) {
	return this.replaceArea(null, neware, name, true);
    }

    public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {
	if (!areas.containsKey(name)) {
	    if (player != null)
		Residence.msg(player, lm.Area_NonExist);
	    return false;
	}
	CuboidArea oldarea = areas.get(name);
	if (!newarea.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
	    if (player != null)
		Residence.msg(player, lm.Area_DiffWorld);
	    return false;
	}
	if (parent == null) {
	    String collideResidence = Residence.getResidenceManager().checkAreaCollision(newarea, this);
	    if (collideResidence != null && player != null) {
		Residence.msg(player, lm.Area_Collision, collideResidence);
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
			    Residence.msg(player, lm.Area_SubzoneCollision, sz);
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
		Residence.msg(player, lm.General_NoPermission);
		return false;
	    }
	    if (parent != null) {
		if (!parent.containsLoc(newarea.getHighLoc()) || !parent.containsLoc(newarea.getLowLoc())) {
		    Residence.msg(player, lm.Area_NotWithinParent);
		    return false;
		}
		if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
		    Residence.msg(player, lm.Residence_ParentNoPermission);
		    return false;
		}
	    }
	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    PermissionGroup group = rPlayer.getGroup();
	    if (!group.canCreateResidences() && !player.hasPermission("residence.resize")) {
		Residence.msg(player, lm.General_NoPermission);
		return false;
	    }
	    if (oldarea.getSize() < newarea.getSize() && (!this.isSubzone() && !isSmallerThanMax(player, newarea, resadmin) || this.isSubzone()
		&& !isSmallerThanMaxSubzone(player, newarea, resadmin))) {
		Residence.msg(player, lm.Area_SizeLimit);
		return false;
	    }
	    if (group.getMinHeight() > newarea.getLowLoc().getBlockY()) {
		Residence.msg(player, lm.Area_LowLimit, String.format("%d", group.getMinHeight()));
		return false;
	    }
	    if (group.getMaxHeight() < newarea.getHighLoc().getBlockY()) {
		Residence.msg(player, lm.Area_HighLimit, String.format("%d", group.getMaxHeight()));
		return false;
	    }
	    if (parent == null && Residence.getConfigManager().enableEconomy() && !resadmin) {
		int chargeamount = (int) Math.ceil((newarea.getSize() - oldarea.getSize()) * group.getCostPerBlock());
		if (chargeamount > 0) {
		    if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
			return false;
		    }
		}
	    }
	}

	ResidenceSizeChangeEvent resevent = new ResidenceSizeChangeEvent(player, this, oldarea, newarea);
	Residence.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return false;

	Residence.getResidenceManager().removeChunkList(getName());
	areas.remove(name);
	areas.put(name, newarea);
	Residence.getResidenceManager().calculateChunks(getName());
	if (player != null)
	    Residence.msg(player, lm.Area_Update);
	return true;
    }

    public boolean addSubzone(String name, Location loc1, Location loc2) {
	return this.addSubzone(null, loc1, loc2, name, true);
    }

    public boolean addSubzone(Player player, Location loc1, Location loc2, String name, boolean resadmin) {
	if (player == null) {
	    return this.addSubzone(null, Residence.getServerLandname(), loc1, loc2, name, resadmin);
	}
	return this.addSubzone(player, player.getName(), loc1, loc2, name, resadmin);
    }

    public boolean addSubzone(Player player, String owner, Location loc1, Location loc2, String name, boolean resadmin) {
	if (!Residence.validName(name)) {
	    if (player != null) {
		Residence.msg(player, lm.Invalid_NameCharacters);
	    }
	    return false;
	}
	if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
	    if (player != null) {
		Residence.msg(player, lm.Subzone_SelectInside);
	    }
	    return false;
	}

	String NName = name;
	name = name.toLowerCase();

	if (subzones.containsKey(name)) {
	    if (player != null) {
		Residence.msg(player, lm.Subzone_Exists, NName);
	    }
	    return false;
	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		if (!this.perms.playerHas(player.getName(), "subzone", this.perms.playerHas(player.getName(), "admin", false))) {
		    Residence.msg(player, lm.General_NoPermission);
		    return false;
		}
	    }
	    if (this.getZoneDepth() >= Residence.getPlayerManager().getResidencePlayer(owner).getMaxSubzones()) {
		Residence.msg(player, lm.Subzone_MaxDepth);
		return false;
	    }
	}

	CuboidArea newArea = new CuboidArea(loc1, loc2);

	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	for (Entry<String, ClaimedResidence> resEntry : set) {
	    ClaimedResidence res = resEntry.getValue();
	    if (res.checkCollision(newArea)) {
		if (player != null) {
		    Residence.msg(player, lm.Subzone_Collide, resEntry.getKey());
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
	    newres.addArea(player, newArea, NName, resadmin);
	} else {
	    newres = new ClaimedResidence(owner, perms.getWorld(), this, plugin);
	    newres.addArea(newArea, NName);
	}

	if (newres.getAreaCount() != 0) {
	    newres.getPermissions().applyDefaultFlags();
	    if (player != null) {
		ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
		PermissionGroup group = rPlayer.getGroup();
		newres.setEnterMessage(group.getDefaultEnterMessage());
		newres.setLeaveMessage(group.getDefaultLeaveMessage());
	    }
	    if (Residence.getConfigManager().flagsInherit()) {
		newres.getPermissions().setParent(perms);
	    }

	    newres.resName = name;

	    newres.setCreateTime();

	    ResidenceSubzoneCreationEvent resevent = new ResidenceSubzoneCreationEvent(player, name, newres, newArea);
	    Residence.getServ().getPluginManager().callEvent(resevent);
	    if (resevent.isCancelled())
		return false;

	    subzones.put(name, newres);
	    if (player != null) {
		Residence.msg(player, lm.Area_Create, name);
		Residence.msg(player, lm.Subzone_Create, name);
	    }
	    return true;
	}
	if (player != null) {
	    Residence.msg(player, lm.Subzone_CreateFail, name);
	}
	return false;
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
	subzonename = subzonename.toLowerCase();

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

    public String getSubzoneNameByRes(ClaimedResidence res) {
	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	for (Entry<String, ClaimedResidence> entry : set) {
	    if (entry.getValue() == res) {
		return entry.getValue().getResidenceName();
	    }
	    String n = entry.getValue().getSubzoneNameByRes(res);
	    if (n != null) {
		return entry.getValue().getResidenceName() + "." + n;
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

    public String getTopParentName() {
	return this.getTopParent().getName();
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
	if (name == null)
	    return false;
	name = name.toLowerCase();
	ClaimedResidence res = subzones.get(name);
	if (player != null && !res.perms.hasResidencePermission(player, true) && !resadmin) {
	    Residence.msg(player, lm.General_NoPermission);
	    return false;
	}
	subzones.remove(name);
	if (player != null) {
	    Residence.msg(player, lm.Subzone_Remove, name);
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

    public long getXZSize() {
	Collection<CuboidArea> set = areas.values();
	long size = 0;
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

    public Map<String, CuboidArea> getAreaMap() {
	return areas;
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
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (!group.canSetEnterLeaveMessages() && !resadmin) {
	    Residence.msg(player, lm.Residence_OwnerNoPermission);
	    return;
	}
	if (!perms.hasResidencePermission(player, false) && !resadmin) {
	    Residence.msg(player, lm.General_NoPermission);
	    return;
	}
	if (enter) {
	    this.setEnterMessage(message);
	} else {
	    this.setLeaveMessage(message);
	}
	Residence.msg(player, lm.Residence_MessageChange);
    }

    public Location getMiddleFreeLoc(Location insideLoc, Player player) {
	CuboidArea area = this.getAreaByLoc(insideLoc);
	if (area == null) {
	    return insideLoc;
	}

	int y = area.getHighLoc().getBlockY();

	int x = area.getLowLoc().getBlockX() + area.getXSize() / 2;
	int z = area.getLowLoc().getBlockZ() + area.getZSize() / 2;

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
	}
	return getOutsideFreeLoc(insideLoc, player);
    }

    public Location getOutsideFreeLoc(Location insideLoc, Player player) {
	CuboidArea area = this.getAreaByLoc(insideLoc);
	if (area == null) {
	    return insideLoc;
	}

	List<RandomLoc> randomLocList = new ArrayList<RandomLoc>();

	for (int z = -1; z < area.getZSize() + 1; z++) {
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX(), 0, area.getLowLoc().getZ() + z));
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX() + area.getXSize(), 0, area.getLowLoc().getZ() + z));
	}

	for (int x = -1; x < area.getXSize() + 1; x++) {
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX() + x, 0, area.getLowLoc().getZ()));
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX() + x, 0, area.getLowLoc().getZ() + area.getZSize()));
	}

	Location loc = insideLoc.clone();

	boolean found = false;
	int it = 0;
	int maxIt = 30;
	while (!found && it < maxIt) {
	    it++;

	    Random ran = new Random(System.currentTimeMillis());
	    if (randomLocList.isEmpty())
		break;
	    int check = ran.nextInt(randomLocList.size());
	    RandomLoc place = randomLocList.get(check);
	    randomLocList.remove(check);
	    double x = place.getX();
	    double z = place.getZ();

	    loc.setX(x);
	    loc.setZ(z);
	    loc.setY(area.getHighLoc().getBlockY());

	    int max = area.getHighLoc().getBlockY();
	    max = loc.getWorld().getEnvironment() == Environment.NETHER ? 100 : max;

	    for (int i = max; i > area.getLowLoc().getY(); i--) {
		loc.setY(i);
		Block block = loc.getBlock();
		Block block2 = loc.clone().add(0, 1, 0).getBlock();
		Block block3 = loc.clone().add(0, -1, 0).getBlock();
		if (!Residence.getNms().isEmptyBlock(block3) && Residence.getNms().isEmptyBlock(block) && Residence.getNms().isEmptyBlock(block2)) {
		    break;
		}
	    }

	    if (!Residence.getNms().isEmptyBlock(loc.getBlock()))
		continue;

	    if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.LAVA || loc.clone().add(0, -1, 0).getBlock().getState()
		.getType() == Material.STATIONARY_LAVA)
		continue;

	    if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.WATER || loc.clone().add(0, -1, 0).getBlock().getState()
		.getType() == Material.STATIONARY_WATER)
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
	    if (res != null && player != null && !res.getPermissions().playerHas(player.getName(), "tp", true) && !player.hasPermission("residence.admin.tp"))
		continue;

	    found = true;
	    loc.setY(loc.getY() + 2);
	    loc.add(0.5, 0, 0.5);
	    break;
	}

	if (!found && Residence.getConfigManager().getKickLocation() != null)
	    return Residence.getConfigManager().getKickLocation();

	return loc;
    }

    public CuboidArea getAreaByLoc(Location loc) {
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

    public List<ClaimedResidence> getSubzones() {
	List<ClaimedResidence> list = new ArrayList<ClaimedResidence>();
	for (Entry<String, ClaimedResidence> res : subzones.entrySet()) {
	    list.add(res.getValue());
	}
	return list;
    }

    public void printSubzoneList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
	    temp.add(ChatColor.GREEN + sz.getKey() + ChatColor.YELLOW + " - " + Residence.msg(lm.General_Owner, sz.getValue().getOwner()));
	}
	InformationPager.printInfo(player, Residence.msg(lm.General_Subzones), temp, page);
    }

    public void printAreaList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (String area : areas.keySet()) {
	    temp.add(area);
	}
	InformationPager.printInfo(player, Residence.msg(lm.General_PhysicalAreas), temp, page);
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
	InformationPager.printInfo(player, Residence.msg(lm.General_PhysicalAreas), temp, page);
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
	    Residence.msg(player, lm.General_NoPermission);
	    return;
	}
	if (!this.containsLoc(player.getLocation())) {
	    Residence.msg(player, lm.Residence_NotIn);
	    return;
	}
	tpLoc = player.getLocation();
	Residence.msg(player, lm.Residence_SetTeleportLocation);
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
	if (!resadmin && !isAdmin && !reqPlayer.hasPermission("residence.tpbypass") && !this.isOwner(targetPlayer)) {
	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(reqPlayer);
	    PermissionGroup group = rPlayer.getGroup();
	    if (!group.hasTpAccess()) {
		Residence.msg(reqPlayer, lm.General_TeleportDeny);
		return;
	    }
	    if (!reqPlayer.equals(targetPlayer)) {
		Residence.msg(reqPlayer, lm.General_NoPermission);
		return;
	    }
	    if (!this.perms.playerHas(reqPlayer.getName(), "tp", true)) {
		Residence.msg(reqPlayer, lm.Residence_TeleportNoFlag);
		return;
	    }
	    if (!this.perms.playerHas(reqPlayer.getName(), "move", true)) {
		Residence.msg(reqPlayer, lm.Residence_MoveDeny, this.getName());
		return;
	    }
	}

	if (!Residence.getTeleportMap().containsKey(targetPlayer.getName()) && !isAdmin) {
	    int distance = isSafeTp(reqPlayer);
	    if (distance > 6) {
		Residence.msg(reqPlayer, lm.General_TeleportConfirm, distance);
		Residence.getTeleportMap().put(reqPlayer.getName(), this);
		return;
	    }
	}

	if (Residence.getConfigManager().getTeleportDelay() > 0 && !isAdmin && !resadmin) {
	    Residence.msg(reqPlayer, lm.General_TeleportStarted, this.getName(), Residence.getConfigManager().getTeleportDelay());
	    if (Residence.getConfigManager().isTeleportTitleMessage())
		TpTimer(reqPlayer, Residence.getConfigManager().getTeleportDelay());
	    Residence.getTeleportDelayMap().add(reqPlayer.getName());
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
		Residence.getTeleportDelayMap().remove(targetPlayer.getName());
		return;
	    }
	    final Location targloc = this.getMiddleFreeLoc(area.getHighLoc(), targetPlayer);
	    if (Residence.getConfigManager().getTeleportDelay() > 0 && !isAdmin)
		performDelaydTp(targloc, targetPlayer, reqPlayer, true);
	    else
		performInstantTp(targloc, targetPlayer, reqPlayer, true);
	}
    }

    public void TpTimer(final Player player, final int t) {
	Residence.getAB().sendTitle(player, Residence.msg(lm.General_TeleportTitle), Residence.msg(lm.General_TeleportTitleTime, t));
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		if (!Residence.getTeleportDelayMap().contains(player.getName()))
		    return;
		if (t > 1)
		    TpTimer(player, t - 1);
	    }
	}, 20L);
    }

    public void performDelaydTp(final Location targloc, final Player targetPlayer, Player reqPlayer, final boolean near) {
	ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
	Residence.getServ().getPluginManager().callEvent(tpevent);
	if (tpevent.isCancelled())
	    return;

	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		if (!Residence.getTeleportDelayMap().contains(targetPlayer.getName()) && Residence.getConfigManager().getTeleportDelay() > 0)
		    return;
		else if (Residence.getTeleportDelayMap().contains(targetPlayer.getName()))
		    Residence.getTeleportDelayMap().remove(targetPlayer.getName());
		targetPlayer.teleport(targloc);
		if (near)
		    Residence.msg(targetPlayer, lm.Residence_TeleportNear);
		else
		    Residence.msg(targetPlayer, lm.General_TeleportSuccess);
		return;
	    }
	}, Residence.getConfigManager().getTeleportDelay() * 20L);
    }

    private void performInstantTp(final Location targloc, final Player targetPlayer, Player reqPlayer, final boolean near) {
	ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
	Residence.getServ().getPluginManager().callEvent(tpevent);
	if (!tpevent.isCancelled()) {
	    targetPlayer.teleport(targloc);
	    if (near)
		Residence.msg(targetPlayer, lm.Residence_TeleportNear);
	    else
		Residence.msg(targetPlayer, lm.General_TeleportSuccess);

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
		Residence.msg(player, lm.Area_NonExist);
		return;
	    }
	    if (areas.size() == 1 && !Residence.getConfigManager().allowEmptyResidences()) {
		Residence.msg(player, lm.Area_RemoveLast);
		return;
	    }

	    ResidenceAreaDeleteEvent resevent = new ResidenceAreaDeleteEvent(player, this, player == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
	    Residence.getServ().getPluginManager().callEvent(resevent);
	    if (resevent.isCancelled())
		return;

	    removeArea(id);
	    if (player != null)
		Residence.msg(player, lm.Area_Remove);
	} else {
	    if (player != null)
		Residence.msg(player, lm.General_NoPermission);
	}
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new HashMap<>();
	Map<String, Object> areamap = new HashMap<>();

	root.put("CapitalizedName", resName);
	if (mainRes)
	    root.put("MainResidence", mainRes);
	if (createTime != 0L)
	    root.put("CreatedOn", createTime);
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

	if (!this.cmdBlackList.isEmpty())
	    root.put("cmdBlackList", this.cmdBlackList);
	if (!this.cmdWhiteList.isEmpty())
	    root.put("cmdWhiteList", this.cmdWhiteList);

	if (tpLoc != null) {
	    Map<String, Object> tpmap = new HashMap<>();
	    tpmap.put("X", convertDouble(tpLoc.getX()));
	    tpmap.put("Y", convertDouble(tpLoc.getY()));
	    tpmap.put("Z", convertDouble(tpLoc.getZ()));
	    tpmap.put("Pitch", convertDouble(tpLoc.getPitch()));
	    tpmap.put("Yaw", convertDouble(tpLoc.getYaw()));
	    root.put("TPLoc", tpmap);
	}
	return root;
    }

    // Converting double with comman to dots format and striping to 2 numbers after dot
    private static double convertDouble(double d) {
	return convertDouble(String.valueOf(d));
    }

    private static double convertDouble(String dString) {
	DecimalFormat formatter = new DecimalFormat("#0.00");
	formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.getDefault()));
	dString = dString.replace(",", ".");
	Double d = 0D;
	try {
	    d = Double.valueOf(dString);
	    d = Double.valueOf(formatter.format(d));
	} catch (Exception e) {
	}
	return d;
    }

    @SuppressWarnings("unchecked")
    public static ClaimedResidence load(Map<String, Object> root, ClaimedResidence parent, Residence plugin) throws Exception {
	ClaimedResidence res = new ClaimedResidence(plugin);
	if (root == null)
	    throw new Exception("Null residence!");

	if (root.containsKey("CapitalizedName"))
	    res.resName = ((String) root.get("CapitalizedName"));

	if (root.containsKey("CreatedOn"))
	    res.createTime = ((Long) root.get("CreatedOn"));
	else
	    res.createTime = System.currentTimeMillis();

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

	if (res.getPermissions().ownerLastKnownName == null)
	    return null;

	if (root.containsKey("MainResidence"))
	    res.mainRes = (Boolean) root.get("MainResidence");

	if (root.containsKey("BlockSellPrice"))
	    res.BlockSellPrice = (Double) root.get("BlockSellPrice");
	else {
	    res.BlockSellPrice = res.getOwnerGroup().getSellPerBlock();
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

	    if (subres == null)
		continue;

	    if (subres.getResidenceName() == null)
		subres.setName(map.getKey());

	    if (Residence.getConfigManager().flagsInherit())
		subres.getPermissions().setParent(res.getPermissions());

	    res.subzones.put(map.getKey().toLowerCase(), subres);
	}

	res.parent = parent;
	Map<String, Object> tploc = (Map<String, Object>) root.get("TPLoc");
	if (tploc != null) {
	    double pitch = 0.0;
	    double yaw = 0.0;

	    if (tploc.containsKey("Yaw"))
		yaw = convertDouble(tploc.get("Yaw").toString());

	    if (tploc.containsKey("Pitch"))
		pitch = convertDouble(tploc.get("Pitch").toString());

	    res.tpLoc = new Location(world, convertDouble(tploc.get("X").toString()), convertDouble(tploc.get("Y").toString()), convertDouble(tploc.get("Z")
		.toString()));
	    res.tpLoc.setPitch((float) pitch);
	    res.tpLoc.setYaw((float) yaw);
	}

	if (root.containsKey("cmdBlackList"))
	    res.cmdBlackList = (List<String>) root.get("cmdBlackList");
	if (root.containsKey("cmdWhiteList"))
	    res.cmdWhiteList = (List<String>) root.get("cmdWhiteList");

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
	    Residence.msg(player, lm.Invalid_NameCharacters);
	    return false;
	}
	if (oldName == null)
	    return false;
	if (newName == null)
	    return false;
	String newN = newName;
	oldName = oldName.toLowerCase();
	newName = newName.toLowerCase();

	ClaimedResidence res = subzones.get(oldName);
	if (res == null) {
	    if (player != null)
		Residence.msg(player, lm.Invalid_Subzone);
	    return false;
	}
	if (player != null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    Residence.msg(player, lm.General_NoPermission);
	    return false;
	}
	if (subzones.containsKey(newName)) {
	    if (player != null)
		Residence.msg(player, lm.Subzone_Exists, newName);
	    return false;
	}
	res.setName(newN);
	subzones.put(newName, res);
	subzones.remove(oldName);
	if (player != null)
	    Residence.msg(player, lm.Subzone_Rename, oldName, newName);
	return true;
    }

    public boolean renameArea(String oldName, String newName) {
	return this.renameArea(null, oldName, newName, true);
    }

    public boolean renameArea(Player player, String oldName, String newName, boolean resadmin) {
	if (!Residence.validName(newName)) {
	    Residence.msg(player, lm.Invalid_NameCharacters);
	    return false;
	}
	if (player == null || perms.hasResidencePermission(player, true) || resadmin) {
	    if (areas.containsKey(newName)) {
		if (player != null)
		    Residence.msg(player, lm.Area_Exists);
		return false;
	    }
	    CuboidArea area = areas.get(oldName);
	    if (area == null) {
		if (player != null)
		    Residence.msg(player, lm.Area_InvalidName);
		return false;
	    }
	    areas.put(newName, area);
	    areas.remove(oldName);
	    if (player != null)
		Residence.msg(player, lm.Area_Rename, oldName, newName);
	    return true;
	}
	Residence.msg(player, lm.General_NoPermission);
	return false;
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

    public ResidencePlayer getRPlayer() {
	return Residence.getPlayerManager().getResidencePlayer(this.getPermissions().getOwner());
    }

    public PermissionGroup getOwnerGroup() {
	return getRPlayer().getGroup(getPermissions().getWorld());
    }

    public String getOwner() {
	return perms.getOwner();
    }

    public boolean isOwner(String name) {
	return perms.getOwner().equalsIgnoreCase(name);
    }

    public boolean isOwner(Player p) {
	if (Residence.getConfigManager().isOfflineMode())
	    return perms.getOwner().equals(p.getName());
	return perms.getOwnerUUID().equals(p.getUniqueId());
    }

    public boolean isOwner(CommandSender sender) {
	if (Residence.getConfigManager().isOfflineMode())
	    return perms.getOwner().equals(sender.getName());
	if (sender instanceof Player)
	    return perms.getOwnerUUID().equals(((Player) sender).getUniqueId());
	return true;
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

    public ChatChannel getChatChannel() {
	return Residence.getChatManager().getChannel(this.getName());
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

    public List<String> getCmdBlackList() {
	return this.cmdBlackList;
    }

    public List<String> getCmdWhiteList() {
	return this.cmdWhiteList;
    }

    public boolean addCmdBlackList(String cmd) {
	if (cmd.contains("/"))
	    cmd = cmd.replace("/", "");
	if (!this.cmdBlackList.contains(cmd.toLowerCase())) {
	    this.cmdBlackList.add(cmd.toLowerCase());
	    return true;
	}
	this.cmdBlackList.remove(cmd.toLowerCase());
	return false;
    }

    public boolean addCmdWhiteList(String cmd) {
	if (cmd.contains("/"))
	    cmd = cmd.replace("/", "");
	if (!this.cmdWhiteList.contains(cmd.toLowerCase())) {
	    this.cmdWhiteList.add(cmd.toLowerCase());
	    return true;
	}
	this.cmdWhiteList.remove(cmd.toLowerCase());
	return false;
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

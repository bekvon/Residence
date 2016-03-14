package com.bekvon.bukkit.residence.economy.rent;

import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketRentInterface;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent.RentEventType;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.utils.GetTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;

public class RentManager implements MarketRentInterface {
    protected Map<String, RentedLand> rentedLand;
    protected Map<String, RentableLand> rentableLand;

    public RentManager() {
	rentedLand = new HashMap<>();
	rentableLand = new HashMap<>();
    }

    public RentedLand getRentedLand(String name) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && name != null)
	    name = name.toLowerCase();
	return rentedLand.containsKey(name) ? rentedLand.get(name) : null;
    }

    public List<String> getRentedLands(String playername) {
	List<String> rentedLands = new ArrayList<String>();
	for (Entry<String, RentedLand> oneland : rentedLand.entrySet()) {
	    if (oneland.getValue().player.equals(playername)) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(oneland.getKey());
		String world = " ";
		if (res != null) {
		    res = res.getTopParent();
		    world = res.getWorld();
		}
		rentedLands.add(Residence.getLM().getMessage("Residence.List", "", oneland.getKey(), world)
		    + Residence.getLM().getMessage("Rent.Rented"));
	    }
	}
	return rentedLands;
    }

    public void setForRent(Player player, String landName, int amount, int days, boolean repeatable, boolean resadmin) {
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return;
	}
	if (Residence.getTransactionManager().isForSale(landName)) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.SellRentFail"));
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	landName = res.getName();

	if (!resadmin) {
	    if (!res.getPermissions().hasResidencePermission(player, true)) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return;
	    }
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    if (this.getRentableCount(player.getName()) >= group.getMaxRentables()) {
		player.sendMessage(Residence.getLM().getMessage("Residence.MaxRent"));
		return;
	    }
	}

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	if (!rentableLand.containsKey(landName)) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENTABLE);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;
	    RentableLand newrent = new RentableLand();
	    newrent.days = days;
	    newrent.cost = amount;
	    newrent.repeatable = repeatable;
	    rentableLand.put(landName, newrent);
	    String[] split = landName.split("\\.");
	    if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Residence.ForRentSuccess", split[split.length - 1], amount, days));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyRent"));
	}
    }

    @SuppressWarnings("deprecation")
    public void rent(Player player, String landName, boolean repeat, boolean resadmin) {
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.Disabled"));
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res != null) {
	    if (res.isOwner(player)) {
		player.sendMessage(Residence.getLM().getMessage("Economy.OwnerRentFail"));
		return;
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	landName = res.getName();

	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!resadmin && this.getRentCount(player.getName()) >= group.getMaxRents(player.getName())) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.MaxRent"));
	    return;
	}
	if (!this.isForRent(landName)) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	    return;
	}
	if (this.isRented(landName)) {
	    String[] split = landName.split("\\.");
	    if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyRented", split[split.length - 1], this.getRentingPlayer(landName)));
	    return;
	}
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

	RentableLand land = rentableLand.get(landName);
	if (Residence.getEconomyManager().canAfford(player.getName(), land.cost)) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENT);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;
	    if (Residence.getEconomyManager().transfer(player.getName(), res.getPermissions().getOwner(), land.cost)) {
		RentedLand newrent = new RentedLand();
		newrent.player = player.getName();
		newrent.startTime = System.currentTimeMillis();
		newrent.endTime = System.currentTimeMillis() + daysToMs(land.days);
		newrent.autoRefresh = repeat;
		rentedLand.put(landName, newrent);

		Residence.getSignUtil().CheckSign(res);

		CuboidArea area = res.getAreaArray()[0];
		Residence.getSelectionManager().NewMakeBorders(player, area.getHighLoc(), area.getLowLoc(), false);

		res.getPermissions().copyUserPermissions(res.getPermissions().getOwner(), player.getName());
		res.getPermissions().clearPlayersFlags(res.getPermissions().getOwner());
		res.getPermissions().setPlayerFlag(player.getName(), "admin", FlagState.TRUE);
		player.sendMessage(Residence.getLM().getMessage("Residence.RentSuccess", landName, land.days));
	    } else {
		player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	}
    }

    public void removeFromForRent(Player player, String landName, boolean resadmin) {
	RentedLand rent = rentedLand.get(landName);
	if (rent == null) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotRented"));
	    return;
	}
	if (resadmin || rent.player.equals(player.getName())) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(Residence.getResidenceManager().getByName(landName), player, RentEventType.UNRENTABLE);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;
	    rentedLand.remove(landName);
	    if (!rentableLand.get(landName).repeatable) {
		rentableLand.remove(landName);
	    }
	    ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	    if (res != null) {
		res.getPermissions().applyDefaultFlags();
		Residence.getSignUtil().CheckSign(res);
	    }
	    player.sendMessage(Residence.getLM().getMessage("Residence.Unrent", landName));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	}
    }

    private long daysToMs(int days) {
	return (((long) days) * 24L * 60L * 60L * 1000L);
    }

    @SuppressWarnings("unused")
    private int msToDays(long ms) {
	return (int) Math.ceil(((((double) ms / 1000D) / 60D) / 60D) / 24D);
    }

    public void unrent(Player player, String landName, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	landName = res.getName();

	if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return;
	}
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	if (rentedLand.containsKey(landName) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyRented", landName, rentedLand.get(landName).player));
	    return;
	}
	if (rentableLand.containsKey(landName)) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.UNRENT);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;
	    rentableLand.remove(landName);
	    if (rentedLand.containsKey(landName)) {
		rentedLand.remove(landName);
		if (res != null) {
		    res.getPermissions().applyDefaultFlags();
		    Residence.getSignUtil().CheckSign(res);
		}
	    }
	    player.sendMessage(Residence.getLM().getMessage("Residence.RemoveRentable", landName));

	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	}
    }

    public void removeFromRent(String landName) {
	rentedLand.remove(landName);
    }

    public void removeRentable(String landName) {
	removeFromRent(landName);
	rentableLand.remove(landName);

	Residence.getSignUtil().removeSign(landName);
    }

    public boolean isForRent(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentableLand.containsKey(landName);
    }

    public boolean isRented(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentedLand.containsKey(landName);
    }

    public String getRentingPlayer(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentedLand.containsKey(landName) ? rentedLand.get(landName).player : null;
    }

    public int getCostOfRent(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentableLand.containsKey(landName) ? rentableLand.get(landName).cost : 0;
    }

    public boolean getRentableRepeatable(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentableLand.containsKey(landName) ? rentableLand.get(landName).repeatable : false;
    }

    public boolean getRentedAutoRepeats(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return getRentableRepeatable(landName) ? (rentedLand.containsKey(landName) ? rentedLand.get(landName).autoRefresh : false) : false;
    }

    public int getRentDays(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentableLand.containsKey(landName) ? rentableLand.get(landName).days : 0;
    }

    public void checkCurrentRents() {
	Iterator<Entry<String, RentedLand>> it = rentedLand.entrySet().iterator();
	while (it.hasNext()) {
	    Entry<String, RentedLand> next = it.next();
	    RentedLand land = next.getValue();
	    if (land.endTime <= System.currentTimeMillis()) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(next.getKey());
		if (Residence.getConfigManager().debugEnabled())
		    System.out.println("Rent Check: " + next.getKey());
		if (res != null) {
		    ResidenceRentEvent revent = new ResidenceRentEvent(res, null, RentEventType.RENT_EXPIRE);
		    Residence.getServ().getPluginManager().callEvent(revent);
		    if (!revent.isCancelled()) {
			RentableLand rentable = rentableLand.get(next.getKey());
			if (!rentable.repeatable) {
			    rentableLand.remove(next.getKey());
			    it.remove();
			    res.getPermissions().applyDefaultFlags();
			} else if (land.autoRefresh) {
			    if (!Residence.getEconomyManager().canAfford(land.player, rentable.cost)) {
				it.remove();
				res.getPermissions().applyDefaultFlags();
			    } else {
				if (!Residence.getEconomyManager().transfer(land.player, res.getPermissions().getOwner(), rentable.cost)) {
				    it.remove();
				    res.getPermissions().applyDefaultFlags();
				} else {
				    land.endTime = System.currentTimeMillis() + this.daysToMs(rentable.days);
				}
			    }
			} else {
			    res.getPermissions().applyDefaultFlags();
			    it.remove();
			}
		    }
		} else {
		    rentableLand.remove(next.getKey());
		    it.remove();
		}
	    }
	}
    }

    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin) {
	String[] split = landName.split("\\.");
	RentableLand land = rentableLand.get(landName);
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (land != null && res != null && (res.isOwner(player) || resadmin)) {

	    landName = res.getName();

	    land.repeatable = value;
	    if (!value && this.isRented(landName))
		rentedLand.get(landName).autoRefresh = false;
	    if (value && split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rentable.EnableRenew", split[split.length - 1]));
	    else if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rentable.DisableRenew", split[split.length - 1]));
	}
    }

    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin) {
	String[] split = landName.split("\\.");
	RentedLand land = rentedLand.get(landName);
	if (land != null && (land.player.equals(player.getName()) || resadmin)) {
	    land.autoRefresh = value;
	    if (value && split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rent.EnableRenew", split[split.length - 1]));
	    else if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rent.DisableRenew", split[split.length - 1]));
	}
    }

    public void printRentInfo(Player player, String landName) {
	RentableLand rentable = rentableLand.get(landName);
	RentedLand rented = rentedLand.get(landName);
	if (rentable != null) {
	    player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	    player.sendMessage(Residence.getLM().getMessage("General.Land", landName));
	    player.sendMessage(Residence.getLM().getMessage("General.Cost", rentable.cost, rentable.days));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.AutoRenew", rentable.repeatable));
	    if (rented != null) {
		player.sendMessage(Residence.getLM().getMessage("Residence.RentedBy", rented.player));
		player.sendMessage(Residence.getLM().getMessage("Rent.Expire", GetTime.getTime(rented.endTime)));
	    } else {
		player.sendMessage(Residence.getLM().getMessage("General.Status", Residence.getLM().getMessage("General.Available")));
	    }
	    player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	    player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	}
    }

    @SuppressWarnings("unchecked")
    public static RentManager load(Map<String, Object> root) {
	RentManager rentManager = new RentManager();
	if (root != null) {
	    Map<String, Object> rentables = (Map<String, Object>) root.get("Rentables");
	    for (Entry<String, Object> rent : rentables.entrySet()) {
		rentManager.rentableLand.put(rent.getKey(), RentableLand.load((Map<String, Object>) rent.getValue()));
	    }
	    Map<String, Object> rented = (Map<String, Object>) root.get("Rented");
	    for (Entry<String, Object> rent : rented.entrySet()) {
		rentManager.rentedLand.put(rent.getKey(), RentedLand.load((Map<String, Object>) rent.getValue()));
	    }
	}
	return rentManager;
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new HashMap<String, Object>();
	Map<String, Object> rentables = new HashMap<String, Object>();
	for (Entry<String, RentableLand> rent : rentableLand.entrySet()) {
	    rentables.put(rent.getKey(), rent.getValue().save());
	}
	Map<String, Object> rented = new HashMap<String, Object>();
	for (Entry<String, RentedLand> rent : rentedLand.entrySet()) {
	    rented.put(rent.getKey(), rent.getValue().save());
	}
	root.put("Rentables", rentables);
	root.put("Rented", rented);
	return root;
    }

    public void updateRentableName(String oldName, String newName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && oldName != null && newName != null) {
	    oldName = oldName.toLowerCase();
	    newName = newName.toLowerCase();
	}
	if (rentableLand.containsKey(oldName)) {
	    rentableLand.put(newName, rentableLand.get(oldName));
	    rentableLand.remove(oldName);
	}
	if (rentedLand.containsKey(oldName)) {
	    rentedLand.put(newName, rentedLand.get(oldName));
	    rentedLand.remove(oldName);
	}
    }

    public void printRentableResidences(Player player) {
	Set<Entry<String, RentableLand>> set = rentableLand.entrySet();
	player.sendMessage(Residence.getLM().getMessage("Rentable.Land"));
	StringBuilder sbuild = new StringBuilder();
	sbuild.append(ChatColor.GREEN);
	boolean firstadd = true;
	for (Entry<String, RentableLand> land : set) {
	    if (!this.isRented(land.getKey())) {
		if (!firstadd)
		    sbuild.append(", ");
		else
		    firstadd = false;
		sbuild.append(land.getKey());
	    }
	}
	player.sendMessage(sbuild.toString());
    }

    public int getRentCount(String player) {
	Set<Entry<String, RentedLand>> set = rentedLand.entrySet();
	int count = 0;
	for (Entry<String, RentedLand> land : set) {
	    if (land.getValue().player.equalsIgnoreCase(player))
		count++;
	}
	return count;
    }

    public int getRentableCount(String player) {
	Set<String> set = rentableLand.keySet();
	int count = 0;
	for (String land : set) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(land);
	    if (res != null)
		if (res.isOwner(player))
		    count++;
	}
	return count;
    }

    public Map<String, RentableLand> getRentableResidences() {
	return rentableLand;
    }

    public Map<String, RentableLand> getCurrentlyRentedResidences() {
	return rentableLand;
    }
}

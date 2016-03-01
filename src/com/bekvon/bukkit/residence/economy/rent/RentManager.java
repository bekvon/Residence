package com.bekvon.bukkit.residence.economy.rent;

import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
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

/**
 *
 * @author Administrator
 */
public class RentManager {
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
		rentedLands.add(Residence.getLanguage().getPhrase("ResidenceList", "|"
		    + oneland.getKey() + "|" + Residence.getLanguage().getPhrase("World") + "|"
		    + world)
		    + Residence.getLanguage().getPhrase("Rented"));
	    }
	}
	return rentedLands;
    }

    public void setForRent(Player player, String landName, int amount, int days, boolean repeatable, boolean resadmin) {
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
	    return;
	}
	if (Residence.getTransactionManager().isForSale(landName)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SellRentFail"));
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	if (!resadmin) {
	    if (!res.getPermissions().hasResidencePermission(player, true)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return;
	    }
	    PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	    if (this.getRentableCount(player.getName()) >= group.getMaxRentables()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceMaxRent"));
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
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceForRentSuccess", ChatColor.YELLOW + split[split.length - 1]
		    + ChatColor.GREEN + "|" + ChatColor.YELLOW + amount + ChatColor.GREEN + "|" + ChatColor.YELLOW + days + ChatColor.GREEN));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceAlreadyRent"));
	}
    }

    @SuppressWarnings("deprecation")
    public void rent(Player player, String landName, boolean repeat, boolean resadmin) {
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentDisabled"));
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res != null) {
	    if (res.isOwner(player)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("OwnerRentFail"));
		return;
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!resadmin && this.getRentCount(player.getName()) >= group.getMaxRents(player.getName())) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceMaxRent"));
	    return;
	}
	if (!this.isForRent(landName)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceNotForRent"));
	    return;
	}
	if (this.isRented(landName)) {
	    String[] split = landName.split("\\.");
	    if (split.length != 0)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceAlreadyRented", ChatColor.YELLOW + split[split.length - 1] + ChatColor.RED
		    + "|" + ChatColor.YELLOW + this.getRentingPlayer(landName)));
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
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceRentSuccess", ChatColor.YELLOW + landName
		    + ChatColor.GREEN + "|" + ChatColor.YELLOW + land.days + ChatColor.GREEN));
	    } else {
		player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotEnoughMoney"));
	}
    }

    public void removeFromForRent(Player player, String landName, boolean resadmin) {
	RentedLand rent = rentedLand.get(landName);
	if (rent == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceNotRented"));
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
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceUnrent", ChatColor.YELLOW + landName + ChatColor.GREEN));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
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
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return;
	}
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	if (rentedLand.containsKey(landName) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceAlreadyRented", ChatColor.YELLOW + landName + ChatColor.RED
		+ "|" + ChatColor.YELLOW + rentedLand.get(landName).player) + ChatColor.YELLOW);
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
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceRemoveRentable", ChatColor.YELLOW + landName
		+ ChatColor.RED));

	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceNotForRent"));
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
	    land.repeatable = value;
	    if (!value && this.isRented(landName))
		rentedLand.get(landName).autoRefresh = false;
	    if (value && split.length != 0)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentableEnableRenew", ChatColor.YELLOW + split[split.length - 1] + ChatColor.RED));
	    else if (split.length != 0)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentableDisableRenew", ChatColor.YELLOW + split[split.length - 1] + ChatColor.RED));
	}
    }

    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin) {
	String[] split = landName.split("\\.");
	RentedLand land = rentedLand.get(landName);
	if (land != null && (land.player.equals(player.getName()) || resadmin)) {
	    land.autoRefresh = value;
	    if (value && split.length != 0)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentEnableRenew", ChatColor.YELLOW + split[split.length - 1] + ChatColor.RED));
	    else if (split.length != 0)
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentDisableRenew", ChatColor.YELLOW + split[split.length - 1] + ChatColor.RED));
	}
    }

    public void printRentInfo(Player player, String landName) {
	RentableLand rentable = rentableLand.get(landName);
	RentedLand rented = rentedLand.get(landName);
	if (rentable != null) {
	    player.sendMessage(ChatColor.GOLD + Residence.getLanguage().getPhrase("Land") + ":" + ChatColor.DARK_GREEN + landName);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Cost") + ": " + ChatColor.DARK_AQUA + rentable.cost + " per " + rentable.days
		+ " days");
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("RentableAutoRenew") + ":" + ChatColor.DARK_AQUA + rentable.repeatable);
	    if (rented != null) {
		player.sendMessage(ChatColor.GOLD + Residence.getLanguage().getPhrase("Status") + ":" + ChatColor.YELLOW + " " + Residence.getLanguage().getPhrase(
		    "ResidenceRentedBy", ChatColor.RED + rented.player + ChatColor.YELLOW));
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("RentExpire") + ":" + ChatColor.GREEN + GetTime.getTime(rented.endTime));

		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("RentAutoRenew") + ":" + ChatColor.DARK_AQUA + rented.autoRefresh);
	    } else {
		player.sendMessage(ChatColor.GOLD + Residence.getLanguage().getPhrase("Status") + ":" + ChatColor.GREEN + " " + Residence.getLanguage().getPhrase(
		    "Available"));
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceNotForRent"));
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
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("RentableLand") + ":");
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
}

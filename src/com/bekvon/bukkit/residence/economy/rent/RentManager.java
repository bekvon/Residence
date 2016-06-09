package com.bekvon.bukkit.residence.economy.rent;

import org.bukkit.Bukkit;
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
    private Map<String, RentedLand> rentedLand;
    private Map<String, RentableLand> rentableLand;

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
	return getRentedLands(playername, false);
    }

    public List<String> getRentedLands(String playername, boolean onlyHidden) {
	List<String> rentedLands = new ArrayList<String>();
	for (Entry<String, RentedLand> oneland : rentedLand.entrySet()) {
	    if (!oneland.getValue().player.equals(playername))
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByName(oneland.getKey());
	    String world = " ";
	    if (res == null)
		continue;

	    res = res.getTopParent();
	    world = res.getWorld();

	    boolean hidden = res.getPermissions().has("hidden", false);

	    if (onlyHidden && !hidden)
		continue;

	    rentedLands.add(Residence.getLM().getMessage("Residence.List", "", oneland.getKey(), world)
		+ Residence.getLM().getMessage("Rent.Rented"));

	}
	return rentedLands;
    }

    public List<String> getRentedLandsList(String playername) {
	List<String> rentedLands = new ArrayList<String>();
	for (Entry<String, RentedLand> oneland : rentedLand.entrySet()) {
	    if (!oneland.getValue().player.equals(playername))
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByName(oneland.getKey());
	    if (res != null)
		res = res.getTopParent();

	    rentedLands.add(oneland.getKey());
	}
	return rentedLands;
    }

    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean resadmin) {
	setForRent(player, landName, amount, days, AllowRenewing, Residence.getConfigManager().isRentStayInMarket(), resadmin);
    }

    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean resadmin) {
	setForRent(player, landName, amount, days, AllowRenewing, StayInMarket, Residence.getConfigManager().isRentAllowAutoPay(), resadmin);
    }

    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean AllowAutoPay, boolean resadmin) {
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
	    newrent.AllowRenewing = AllowRenewing;
	    newrent.StayInMarket = StayInMarket;
	    newrent.AllowAutoPay = AllowAutoPay;
	    rentableLand.put(landName, newrent);
	    String[] split = landName.split("\\.");
	    if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Residence.ForRentSuccess", split[split.length - 1], amount, days));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyRent"));
	}
    }

    @SuppressWarnings("deprecation")
    public void rent(Player player, String landName, boolean AutoPay, boolean resadmin) {
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
	    if (split.length != 0) {
//		player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyRented", split[split.length - 1], this.getRentingPlayer(landName)));
		printRentInfo(player, landName);
//		Bukkit.dispatchCommand(player, "res market info " + landName);
	    }
	    return;
	}
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

	RentableLand land = rentableLand.get(landName);

	if (!land.AllowAutoPay && AutoPay) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.CantAutoPay"));
	    AutoPay = false;
	}

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
		newrent.AutoPay = AutoPay;
		rentedLand.put(landName, newrent);

		Residence.getSignUtil().CheckSign(res);

		CuboidArea area = res.getAreaArray()[0];
		Residence.getSelectionManager().NewMakeBorders(player, area.getHighLoc(), area.getLowLoc(), false);

		res.getPermissions().copyUserPermissions(res.getPermissions().getOwner(), player.getName());
		res.getPermissions().clearPlayersFlags(res.getPermissions().getOwner());
		res.getPermissions().setPlayerFlag(player.getName(), "admin", FlagState.TRUE);
		player.sendMessage(Residence.getLM().getMessage("Residence.RentSuccess", landName, land.days));

		if (Residence.getSchematicManager() != null &&
		    Residence.getConfigManager().RestoreAfterRentEnds &&
		    !Residence.getConfigManager().SchematicsSaveOnFlagChange &&
		    res.getPermissions().has("backup", true)) {
		    Residence.getSchematicManager().save(res);
		}

	    } else {
		player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	}
    }

    public void payRent(Player player, String landName, boolean resadmin) {
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.Disabled"));
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	landName = res.getName();

	if (!this.isForRent(landName)) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	    return;
	}

	if (this.isRented(landName) && !this.getRentingPlayer(landName).equals(player.getName()) && !resadmin) {
	    String[] split = landName.split("\\.");
	    if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rent.NotByYou"));
	    return;
	}

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

	RentableLand land = rentableLand.get(landName);
	RentedLand rentedLand = this.getRentedLand(landName);

	if (rentedLand == null) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotRented"));
	    return;
	}

	if (!land.AllowRenewing) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.OneTime"));
	    return;
	}

	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!resadmin && group.getMaxRentDays() != -1 &&
	    this.msToDays((rentedLand.endTime - System.currentTimeMillis()) + daysToMs(land.days)) >= group.getMaxRentDays()) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.MaxRentDays", group.getMaxRentDays()));
	    return;
	}

	if (Residence.getEconomyManager().canAfford(player.getName(), land.cost)) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENT);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;
	    if (Residence.getEconomyManager().transfer(player.getName(), res.getPermissions().getOwner(), land.cost)) {
		rentedLand.endTime = rentedLand.endTime + daysToMs(land.days);
		Residence.getSignUtil().CheckSign(res);
		CuboidArea area = res.getAreaArray()[0];
		Residence.getSelectionManager().NewMakeBorders(player, area.getHighLoc(), area.getLowLoc(), false);
		player.sendMessage(Residence.getLM().getMessage("Rent.Extended", land.days, landName));
		player.sendMessage(Residence.getLM().getMessage("Rent.Expire", GetTime.getTime(rentedLand.endTime)));

	    } else {
		player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	}
    }

    public void unrent(Player player, String landName, boolean resadmin) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

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
	    if (!rentableLand.get(landName).AllowRenewing && !rentableLand.get(landName).StayInMarket) {
		rentableLand.remove(landName);
	    }
	    ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	    if (res != null) {
		boolean backup = res.getPermissions().has("backup", false);

		res.getPermissions().applyDefaultFlags();

		if (Residence.getSchematicManager() != null && Residence.getConfigManager().RestoreAfterRentEnds && backup) {
		    Residence.getSchematicManager().load(res);
		    // set true if its already exists
		    res.getPermissions().setFlag("backup", FlagState.TRUE);
		}
		Residence.getSignUtil().CheckSign(res);
	    }
	    player.sendMessage(Residence.getLM().getMessage("Residence.Unrent", landName));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	}
    }

    private long daysToMs(int days) {
//	return (((long) days) * 1000L);
	return (((long) days) * 24L * 60L * 60L * 1000L);
    }

    private int msToDays(long ms) {
	return (int) Math.ceil(((((double) ms / 1000D) / 60D) / 60D) / 24D);
    }

    public void removeFromForRent(Player player, String landName, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	landName = res.getName();
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

	if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
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
	    }
	    if (res != null) {
		res.getPermissions().applyDefaultFlags();
		Residence.getSignUtil().CheckSign(res);
	    }
	    player.sendMessage(Residence.getLM().getMessage("Residence.RemoveRentable", landName));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	}
    }

    public void removeFromRent(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	rentedLand.remove(landName);
    }

    public void removeRentable(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	removeFromRent(landName);
	rentableLand.remove(landName);
	Residence.getSignUtil().removeSign(landName);
    }

    public boolean isForRent(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentableLand.containsKey(landName);
    }

    public RentableLand getRentableLand(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return rentableLand.get(landName);
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
	return rentableLand.containsKey(landName) ? rentableLand.get(landName).AllowRenewing : false;
    }

    public boolean getRentedAutoRepeats(String landName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	return getRentableRepeatable(landName) ? (rentedLand.containsKey(landName) ? rentedLand.get(landName).AutoPay : false) : false;
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
	    if (land.endTime > System.currentTimeMillis())
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByName(next.getKey());
	    if (Residence.getConfigManager().debugEnabled())
		System.out.println("Rent Check: " + next.getKey());
	    if (res != null) {
		ResidenceRentEvent revent = new ResidenceRentEvent(res, null, RentEventType.RENT_EXPIRE);
		Residence.getServ().getPluginManager().callEvent(revent);
		if (revent.isCancelled())
		    continue;

		RentableLand rentable = rentableLand.get(next.getKey());
		if (!rentable.AllowRenewing) {
		    if (!rentable.StayInMarket)
			rentableLand.remove(next.getKey());
		    it.remove();
		    res.getPermissions().applyDefaultFlags();
		    Residence.getSignUtil().CheckSign(res);
		    continue;
		}
		if (land.AutoPay && rentable.AllowAutoPay) {
		    if (!Residence.getEconomyManager().canAfford(land.player, rentable.cost)) {
			if (!rentable.StayInMarket)
			    rentableLand.remove(next.getKey());
			it.remove();
			res.getPermissions().applyDefaultFlags();
		    } else {
			if (!Residence.getEconomyManager().transfer(land.player, res.getPermissions().getOwner(), rentable.cost)) {
			    if (!rentable.StayInMarket)
				rentableLand.remove(next.getKey());
			    it.remove();
			    res.getPermissions().applyDefaultFlags();
			} else {
			    land.endTime = System.currentTimeMillis() + this.daysToMs(rentable.days);
			}
		    }

		    Residence.getSignUtil().CheckSign(res);
		    continue;
		}
		if (!rentable.StayInMarket)
		    rentableLand.remove(next.getKey());
		it.remove();

		boolean backup = res.getPermissions().has("backup", false);

		res.getPermissions().applyDefaultFlags();

		if (Residence.getSchematicManager() != null && Residence.getConfigManager().RestoreAfterRentEnds && backup) {
		    Residence.getSchematicManager().load(res);
		    Residence.getSignUtil().CheckSign(res);
		    // set true if its already exists
		    res.getPermissions().setFlag("backup", FlagState.TRUE);
		    // To avoid lag spikes on multiple residence restores at once, will limit to one residence at time
		    break;
		}
		Residence.getSignUtil().CheckSign(res);
	    } else {
		rentableLand.remove(next.getKey());
		it.remove();
	    }
	}
    }

    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin) {

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();
	String[] split = landName.split("\\.");
	RentableLand land = rentableLand.get(landName);
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (!res.isOwner(player) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return;
	}

	if (land != null && res != null && (res.isOwner(player) || resadmin)) {

	    landName = res.getName();

	    land.AllowRenewing = value;
	    if (!value && this.isRented(landName))
		rentedLand.get(landName).AutoPay = false;
	    if (value && split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rentable.EnableRenew", split[split.length - 1]));
	    else if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rentable.DisableRenew", split[split.length - 1]));
	}
    }

    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin) {

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

	String[] split = landName.split("\\.");
	RentedLand land = rentedLand.get(landName);

	if (land == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (!this.getRentableLand(landName).AllowAutoPay && value) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.CantAutoPay"));
	    return;
	}

	if (!land.player.equals(player.getName()) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return;
	}

	if (land != null && (land.player.equals(player.getName()) || resadmin)) {
	    land.AutoPay = value;
	    if (value && split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rent.EnableRenew", split[split.length - 1]));
	    else if (split.length != 0)
		player.sendMessage(Residence.getLM().getMessage("Rent.DisableRenew", split[split.length - 1]));

	    ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	    if (res != null)
		Residence.getSignUtil().CheckSign(res);
	}
    }

    public void printRentInfo(Player player, String landName) {

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && landName != null)
	    landName = landName.toLowerCase();

	RentableLand rentable = rentableLand.get(landName);
	RentedLand rented = rentedLand.get(landName);
	if (rentable != null) {
	    player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	    player.sendMessage(Residence.getLM().getMessage("General.Land", landName));
	    player.sendMessage(Residence.getLM().getMessage("General.Cost", rentable.cost, rentable.days));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.AllowRenewing", rentable.AllowRenewing));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.StayInMarket", rentable.StayInMarket));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.AllowAutoPay", rentable.AllowAutoPay));
	    if (rented != null) {
		player.sendMessage(Residence.getLM().getMessage("Residence.RentedBy", rented.player));

		ClaimedResidence res = Residence.getResidenceManager().getByName(landName);

		if (rented.player.equals(player.getName()) || res != null && res.isOwner(player) || Residence.isResAdminOn(player))
		    player.sendMessage((rented.AutoPay ? Residence.getLM().getMessage("Rent.AutoPayTurnedOn") : Residence.getLM().getMessage("Rent.AutoPayTurnedOff"))
			+ "\n");

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
	HashMap<String, RentableLand> toAddRentable = new HashMap<String, RentableLand>();
	for (Iterator<Entry<String, RentableLand>> it = rentableLand.entrySet().iterator(); it.hasNext();) {
	    Entry<String, RentableLand> entry = it.next();
	    String n = entry.getKey();
	    if (!Residence.getConfigManager().isResCreateCaseSensitive())
		n = n.toLowerCase();

	    if (n.contains(".") && n.startsWith(oldName + ".") || n.equals(oldName)) {
		RentableLand land = new RentableLand();
		land.AllowAutoPay = entry.getValue().AllowAutoPay;
		land.AllowRenewing = entry.getValue().AllowRenewing;
		land.cost = entry.getValue().cost;
		land.days = entry.getValue().days;
		land.StayInMarket = entry.getValue().StayInMarket;

		String[] split = n.split(oldName);
		String subname = "";
		if (split.length > 1)
		    subname = n.split(oldName)[1];
		String name = newName + subname;
//		rentableLand.remove(entry);
		toAddRentable.put(name, land);

		it.remove();
	    }
	}
	rentableLand.putAll(toAddRentable);

	HashMap<String, RentedLand> toAddRented = new HashMap<String, RentedLand>();
	for (Iterator<Entry<String, RentedLand>> it = rentedLand.entrySet().iterator(); it.hasNext();) {
	    Entry<String, RentedLand> entry = it.next();
	    String n = entry.getKey();
	    if (!Residence.getConfigManager().isResCreateCaseSensitive())
		n = n.toLowerCase();
	    if (n.contains(".") && n.startsWith(oldName + ".") || n.equals(oldName)) {
		RentedLand rented = entry.getValue();
		String[] split = n.split(oldName);
		String subname = "";
		if (split.length > 1)
		    subname = n.split(oldName)[1];
		String name = newName + subname;
//		rentedLand.remove(entry);
//		rentedLand.put(name, rented);
		toAddRented.put(name, rented);
		it.remove();
	    }
	}
	rentedLand.putAll(toAddRented);

//	if (rentableLand.containsKey(oldName)) {
//	    rentableLand.put(newName, rentableLand.get(oldName));
//	    rentableLand.remove(oldName);
//	}
//	if (rentedLand.containsKey(oldName)) {
//	    rentedLand.put(newName, rentedLand.get(oldName));
//	    rentedLand.remove(oldName);
//	}
    }

    public void printRentableResidences(Player player, int page) {
	Set<Entry<String, RentableLand>> set = rentableLand.entrySet();
	player.sendMessage(Residence.getLM().getMessage("Rentable.Land"));
	StringBuilder sbuild = new StringBuilder();
	sbuild.append(ChatColor.GREEN);

	int perpage = 10;

	int pagecount = (int) Math.ceil((double) set.size() / (double) perpage);

	if (page < 1)
	    page = 1;

	int z = 0;

	for (Entry<String, RentableLand> land : set) {

	    z++;
	    if (z <= (page - 1) * perpage)
		continue;
	    if (z > (page - 1) * perpage + perpage)
		break;

	    boolean rented = isRented(land.getKey());

	    if (!land.getValue().AllowRenewing && rented)
		continue;

	    ClaimedResidence res = Residence.getResidenceManager().getByName(land.getKey());

	    String rentedBy = "";

	    String hover = "";
	    if (rented) {
		RentedLand rent = rentedLand.get(land.getKey());
		rentedBy = Residence.getLM().getMessage("Residence.RentedBy", rent.player);
		hover = GetTime.getTime(rent.endTime);
	    }
	    if (res == null)
		continue;

	    String msg = Residence.getLM().getMessage("Rent.RentList", z, land.getKey(), land.getValue().cost, land.getValue().days, land
		.getValue().AllowRenewing, res.getOwner(), rentedBy);

	    if (!hover.equalsIgnoreCase(""))
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " {\"text\":\"\",\"extra\":[{\"text\":\"" + msg
		    + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§2" + hover + "\"}}]}");
	    else
		player.sendMessage(msg);

	}

	String separator = ChatColor.GOLD + "";
	String simbol = "\u25AC";
	for (int i = 0; i < 10; i++) {
	    separator += simbol;
	}

	if (pagecount == 1)
	    return;

	int NextPage = page + 1;
	NextPage = page < pagecount ? NextPage : page;
	int Prevpage = page - 1;
	Prevpage = page > 1 ? Prevpage : page;

	String prevCmd = "/res market list sell " + Prevpage;
	String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.getLM().getMessage("General.PrevInfoPage")
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = "/res market list sell " + NextPage;
	String next = " {\"text\":\"" + Residence.getLM().getMessage("General.NextInfoPage") + " " + separator
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
	    + nextCmd + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " " + prev + "," + next);
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

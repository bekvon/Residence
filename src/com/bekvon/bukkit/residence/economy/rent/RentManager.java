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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;

public class RentManager implements MarketRentInterface {
    private Set<ClaimedResidence> rentedLand;
    private Set<ClaimedResidence> rentableLand;

    public RentManager() {
	rentedLand = new HashSet<ClaimedResidence>();
	rentableLand = new HashSet<ClaimedResidence>();
    }

    public RentedLand getRentedLand(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getRentedLand(res);
    }

    public RentedLand getRentedLand(ClaimedResidence res) {
	if (res == null)
	    return null;
	return res.isRented() ? res.getRentedLand() : null;
    }

    public List<String> getRentedLands(String playername) {
	return getRentedLands(playername, false);
    }

    public List<String> getRentedLands(String playername, boolean onlyHidden) {
	List<String> rentedLands = new ArrayList<String>();
	for (ClaimedResidence res : rentedLand) {
	    if (res == null)
		continue;

	    if (!res.isRented())
		continue;

	    if (!res.getRentedLand().player.equals(playername))
		continue;

	    String world = " ";
	    ClaimedResidence topres = res.getTopParent();
	    world = topres.getWorld();

	    boolean hidden = topres.getPermissions().has("hidden", false);

	    if (onlyHidden && !hidden)
		continue;

	    rentedLands.add(Residence.getLM().getMessage("Residence.List", "", res.getName(), world)
		+ Residence.getLM().getMessage("Rent.Rented"));
	}
	return rentedLands;
    }

    public List<String> getRentedLandsList(Player player) {
	return getRentedLandsList(player.getName());
    }

    public List<String> getRentedLandsList(String playername) {
	List<String> rentedLands = new ArrayList<String>();
	for (ClaimedResidence res : rentedLand) {
	    if (res == null)
		continue;
	    if (!res.isRented())
		continue;
	    if (!res.getRentedLand().player.equals(playername))
		continue;
	    rentedLands.add(res.getName());
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
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	setForRent(player, res, amount, days, AllowRenewing, StayInMarket, AllowAutoPay, resadmin);
    }

    public void setForRent(Player player, ClaimedResidence res, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean AllowAutoPay,
	boolean resadmin) {

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return;
	}

	if (res.isForSell()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.SellRentFail"));
	    return;
	}

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

	if (!rentableLand.contains(res)) {
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
	    res.setRentable(newrent);
	    rentableLand.add(res);
	    player.sendMessage(Residence.getLM().getMessage("Residence.ForRentSuccess", res.getShortName(), amount, days));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.AlreadyRent"));
	}
    }

    public void rent(Player player, String landName, boolean AutoPay, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	rent(player, res, AutoPay, resadmin);
    }

    @SuppressWarnings("deprecation")
    public void rent(Player player, ClaimedResidence res, boolean AutoPay, boolean resadmin) {
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.Disabled"));
	    return;
	}

	if (res.isOwner(player)) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.OwnerRentFail"));
	    return;
	}

	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!resadmin && this.getRentCount(player.getName()) >= group.getMaxRents(player.getName())) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.MaxRent"));
	    return;
	}
	if (!res.isForRent()) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	    return;
	}
	if (res.isRented()) {
	    printRentInfo(player, res.getName());
	    return;
	}

	RentableLand land = res.getRentable();

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
		res.setRented(newrent);
		rentedLand.add(res);

		Residence.getSignUtil().CheckSign(res);

		CuboidArea area = res.getAreaArray()[0];
		Residence.getSelectionManager().NewMakeBorders(player, area.getHighLoc(), area.getLowLoc(), false);

		res.getPermissions().copyUserPermissions(res.getPermissions().getOwner(), player.getName());
		res.getPermissions().clearPlayersFlags(res.getPermissions().getOwner());
		res.getPermissions().setPlayerFlag(player.getName(), "admin", FlagState.TRUE);
		player.sendMessage(Residence.getLM().getMessage("Residence.RentSuccess", res.getName(), land.days));

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
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	payRent(player, res, resadmin);
    }

    public void payRent(Player player, ClaimedResidence res, boolean resadmin) {
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.Disabled"));
	    return;
	}

	if (!res.isForRent()) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	    return;
	}

	if (res.isRented() && !getRentingPlayer(res).equals(player.getName()) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.NotByYou"));
	    return;
	}

	RentableLand land = res.getRentable();
	RentedLand rentedLand = res.getRentedLand();

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
		player.sendMessage(Residence.getLM().getMessage("Rent.Extended", land.days, res.getName()));
		player.sendMessage(Residence.getLM().getMessage("Rent.Expire", GetTime.getTime(rentedLand.endTime)));
	    } else {
		player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	}
    }

    public void unrent(Player player, String landName, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	unrent(player, res, resadmin);
    }

    public void unrent(Player player, ClaimedResidence res, boolean resadmin) {
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	RentedLand rent = res.getRentedLand();
	if (rent == null) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotRented"));
	    return;
	}

	if (resadmin || rent.player.equals(player.getName())) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.UNRENTABLE);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;

	    rentedLand.remove(res);
	    res.setRented(null);
	    if (!res.getRentable().AllowRenewing && !res.getRentable().StayInMarket) {
		rentableLand.remove(res);
		res.setRentable(null);
	    }

	    boolean backup = res.getPermissions().has("backup", false);

	    res.getPermissions().applyDefaultFlags();

	    if (Residence.getSchematicManager() != null && Residence.getConfigManager().RestoreAfterRentEnds && backup) {
		Residence.getSchematicManager().load(res);
		// set true if its already exists
		res.getPermissions().setFlag("backup", FlagState.TRUE);
	    }
	    Residence.getSignUtil().CheckSign(res);

	    player.sendMessage(Residence.getLM().getMessage("Residence.Unrent", res.getName()));
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
	removeFromForRent(player, res, resadmin);
    }

    public void removeFromForRent(Player player, ClaimedResidence res, boolean resadmin) {
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return;
	}

	if (rentableLand.contains(res)) {
	    ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.UNRENT);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		return;
	    rentableLand.remove(res);
	    res.setRentable(null);
	    res.getPermissions().applyDefaultFlags();
	    Residence.getSignUtil().CheckSign(res);
	    player.sendMessage(Residence.getLM().getMessage("Residence.RemoveRentable", res.getShortName()));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForRent"));
	}
    }

    public void removeFromRent(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	removeFromRent(res);
    }

    public void removeFromRent(ClaimedResidence res) {
	rentedLand.remove(res);
    }

    public void removeRentable(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	removeRentable(res);
    }

    public void removeRentable(ClaimedResidence res) {
	if (res == null)
	    return;
	removeFromRent(res);
	rentableLand.remove(res);
	Residence.getSignUtil().removeSign(res.getName());
    }

    public boolean isForRent(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return isForRent(res);
    }

    public boolean isForRent(ClaimedResidence res) {
	if (res == null)
	    return false;
	return rentableLand.contains(res);
    }

    public RentableLand getRentableLand(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getRentableLand(res);
    }

    public RentableLand getRentableLand(ClaimedResidence res) {
	if (res == null)
	    return null;
	if (res.isForRent())
	    return res.getRentable();
	return null;
    }

    public boolean isRented(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return isRented(res);
    }

    public boolean isRented(ClaimedResidence res) {
	if (res == null)
	    return false;
	return rentedLand.contains(res);
    }

    public String getRentingPlayer(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getRentingPlayer(res);
    }

    public String getRentingPlayer(ClaimedResidence res) {
	if (res == null)
	    return null;
	return res.isRented() ? res.getRentedLand().player : null;
    }

    public int getCostOfRent(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getCostOfRent(res);
    }

    public int getCostOfRent(ClaimedResidence res) {
	if (res == null)
	    return 0;
	return res.isForRent() ? res.getRentable().cost : 0;
    }

    public boolean getRentableRepeatable(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getRentableRepeatable(res);
    }

    public boolean getRentableRepeatable(ClaimedResidence res) {
	if (res == null)
	    return false;
	return res.isForRent() ? res.getRentable().AllowRenewing : false;
    }

    public boolean getRentedAutoRepeats(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getRentedAutoRepeats(res);
    }

    public boolean getRentedAutoRepeats(ClaimedResidence res) {
	if (res == null)
	    return false;
	return getRentableRepeatable(res) ? (rentedLand.contains(res) ? res.getRentedLand().AutoPay : false) : false;
    }

    public int getRentDays(String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	return getRentDays(res);
    }

    public int getRentDays(ClaimedResidence res) {
	if (res == null)
	    return 0;
	return res.isForRent() ? res.getRentable().days : 0;
    }

    public void checkCurrentRents() {
	for (ClaimedResidence res : rentedLand) {

	    if (res == null)
		continue;

	    RentedLand land = res.getRentedLand();
	    if (land == null)
		continue;

	    if (land.endTime > System.currentTimeMillis())
		continue;

	    if (Residence.getConfigManager().debugEnabled())
		System.out.println("Rent Check: " + res.getName());

	    ResidenceRentEvent revent = new ResidenceRentEvent(res, null, RentEventType.RENT_EXPIRE);
	    Residence.getServ().getPluginManager().callEvent(revent);
	    if (revent.isCancelled())
		continue;

	    RentableLand rentable = res.getRentable();
	    if (!rentable.AllowRenewing) {
		if (!rentable.StayInMarket) {
		    rentableLand.remove(res);
		    res.setRentable(null);
		}
		rentedLand.remove(res);
		res.setRented(null);
		res.getPermissions().applyDefaultFlags();
		Residence.getSignUtil().CheckSign(res);
		continue;
	    }
	    if (land.AutoPay && rentable.AllowAutoPay) {
		if (!Residence.getEconomyManager().canAfford(land.player, rentable.cost)) {
		    if (!rentable.StayInMarket) {
			rentableLand.remove(res);
			res.setRentable(null);
		    }
		    rentedLand.remove(res);
		    res.setRented(null);
		    res.getPermissions().applyDefaultFlags();
		} else {
		    if (!Residence.getEconomyManager().transfer(land.player, res.getPermissions().getOwner(), rentable.cost)) {
			if (!rentable.StayInMarket) {
			    rentableLand.remove(res);
			    res.setRentable(null);
			}
			rentedLand.remove(res);
			res.setRented(null);
			res.getPermissions().applyDefaultFlags();
		    } else {
			land.endTime = System.currentTimeMillis() + this.daysToMs(rentable.days);
		    }
		}

		Residence.getSignUtil().CheckSign(res);
		continue;
	    }
	    if (!rentable.StayInMarket) {
		rentableLand.remove(res);
		res.setRentable(null);
	    }
	    rentedLand.remove(res);
	    res.setRented(null);

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
	}
    }

    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	setRentRepeatable(player, res, value, resadmin);
    }

    public void setRentRepeatable(Player player, ClaimedResidence res, boolean value, boolean resadmin) {

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	RentableLand land = res.getRentable();

	if (!res.isOwner(player) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return;
	}

	if (land == null || res == null || !res.isOwner(player) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return;
	}

	land.AllowRenewing = value;
	if (!value && this.isRented(res))
	    res.getRentedLand().AutoPay = false;

	if (value)
	    player.sendMessage(Residence.getLM().getMessage("Rentable.EnableRenew", res.getShortName()));
	else
	    player.sendMessage(Residence.getLM().getMessage("Rentable.DisableRenew", res.getShortName()));

    }

    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	setRentedRepeatable(player, res, value, resadmin);
    }

    public void setRentedRepeatable(Player player, ClaimedResidence res, boolean value, boolean resadmin) {
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	RentedLand land = res.getRentedLand();

	if (land == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (!res.getRentable().AllowAutoPay && value) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.CantAutoPay"));
	    return;
	}

	if (!land.player.equals(player.getName()) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return;
	}

	if (!land.player.equals(player.getName()) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
	    return;
	}

	land.AutoPay = value;
	if (value)
	    player.sendMessage(Residence.getLM().getMessage("Rent.EnableRenew", res.getShortName()));
	else
	    player.sendMessage(Residence.getLM().getMessage("Rent.DisableRenew", res.getShortName()));

	if (res != null)
	    Residence.getSignUtil().CheckSign(res);
    }

    public void printRentInfo(Player player, String landName) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(landName);
	printRentInfo(player, res);
    }

    public void printRentInfo(Player player, ClaimedResidence res) {

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	RentableLand rentable = res.getRentable();
	RentedLand rented = res.getRentedLand();
	if (rentable != null) {
	    player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	    player.sendMessage(Residence.getLM().getMessage("General.Land", res.getName()));
	    player.sendMessage(Residence.getLM().getMessage("General.Cost", rentable.cost, rentable.days));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.AllowRenewing", rentable.AllowRenewing));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.StayInMarket", rentable.StayInMarket));
	    player.sendMessage(Residence.getLM().getMessage("Rentable.AllowAutoPay", rentable.AllowAutoPay));
	    if (rented != null) {
		player.sendMessage(Residence.getLM().getMessage("Residence.RentedBy", rented.player));

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

    public void printRentableResidences(Player player, int page) {
	player.sendMessage(Residence.getLM().getMessage("Rentable.Land"));
	StringBuilder sbuild = new StringBuilder();
	sbuild.append(ChatColor.GREEN);

	int perpage = 10;

	int pagecount = (int) Math.ceil((double) rentableLand.size() / (double) perpage);

	if (page < 1)
	    page = 1;

	int z = 0;

	for (ClaimedResidence res : rentableLand) {
	    if (res == null)
		continue;

	    z++;
	    if (z <= (page - 1) * perpage)
		continue;
	    if (z > (page - 1) * perpage + perpage)
		break;

	    boolean rented = res.isRented();

	    if (!res.getRentable().AllowRenewing && rented)
		continue;

	    String rentedBy = "";
	    String hover = "";
	    if (rented) {
		RentedLand rent = res.getRentedLand();
		rentedBy = Residence.getLM().getMessage("Residence.RentedBy", rent.player);
		hover = GetTime.getTime(rent.endTime);
	    }

	    String msg = Residence.getLM().getMessage("Rent.RentList", z, res.getName(), res.getRentable().cost, res.getRentable().days, res.getRentable().AllowRenewing,
		res.getOwner(), rentedBy);

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
	int count = 0;
	for (ClaimedResidence res : rentedLand) {
	    if (res.getRentedLand().player.equalsIgnoreCase(player))
		count++;
	}
	return count;
    }

    public int getRentableCount(String player) {
	int count = 0;
	for (ClaimedResidence res : rentableLand) {
	    if (res != null)
		if (res.isOwner(player))
		    count++;
	}
	return count;
    }

    public Set<ClaimedResidence> getRentableResidences() {
	return rentableLand;
    }

    public Set<ClaimedResidence> getCurrentlyRentedResidences() {
	return rentedLand;
    }

    @SuppressWarnings("unchecked")
    public void load(Map<String, Object> root) {
	if (root == null)
	    return;
	this.rentableLand.clear();

	Map<String, Object> rentables = (Map<String, Object>) root.get("Rentables");
	for (Entry<String, Object> rent : rentables.entrySet()) {
	    RentableLand one = loadRentable((Map<String, Object>) rent.getValue());
	    ClaimedResidence res = Residence.getResidenceManager().getByName(rent.getKey());
	    if (res != null) {
		res.setRentable(one);
		this.rentableLand.add(res);
	    }
	}
	Map<String, Object> rented = (Map<String, Object>) root.get("Rented");
	for (Entry<String, Object> rent : rented.entrySet()) {
	    RentedLand one = loadRented((Map<String, Object>) rent.getValue());
	    ClaimedResidence res = Residence.getResidenceManager().getByName(rent.getKey());
	    if (res != null) {
		res.setRented(one);
		this.rentedLand.add(res);
	    }
	}
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new HashMap<String, Object>();
	Map<String, Object> rentables = new HashMap<String, Object>();
	for (ClaimedResidence res : rentableLand) {
	    if (res == null || res.getRentable() == null)
		continue;
	    rentables.put(res.getName(), res.getRentable().save());
	}
	Map<String, Object> rented = new HashMap<String, Object>();
	for (ClaimedResidence res : rentedLand) {
	    if (res == null || res.getRentedLand() == null)
		continue;
	    rented.put(res.getName(), res.getRentedLand().save());
	}
	root.put("Rentables", rentables);
	root.put("Rented", rented);
	return root;
    }

    private RentableLand loadRentable(Map<String, Object> map) {
	RentableLand newland = new RentableLand();
	newland.cost = (Integer) map.get("Cost");
	newland.days = (Integer) map.get("Days");
	newland.AllowRenewing = (Boolean) map.get("Repeatable");
	if (map.containsKey("StayInMarket"))
	    newland.StayInMarket = (Boolean) map.get("StayInMarket");
	if (map.containsKey("AllowAutoPay"))
	    newland.AllowAutoPay = (Boolean) map.get("AllowAutoPay");
	return newland;
    }

    private RentedLand loadRented(Map<String, Object> map) {
	RentedLand newland = new RentedLand();
	newland.player = (String) map.get("Player");
	newland.startTime = (Long) map.get("StartTime");
	newland.endTime = (Long) map.get("EndTime");
	newland.AutoPay = (Boolean) map.get("AutoRefresh");
	return newland;
    }

}

package com.bekvon.bukkit.residence.economy;

import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 * 
 * @author Administrator
 */
public class TransactionManager {
    ResidenceManager manager;
    private Map<String, Integer> sellAmount;
    PermissionManager gm;

    public static boolean chargeEconomyMoney(Player player, int amount) {
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
	    return false;
	}
	if (!econ.canAfford(player.getName(), amount)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotEnoughMoney"));
	    return false;
	}
	econ.subtract(player.getName(), amount);
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("MoneyCharged", ChatColor.YELLOW + String.format("%d", amount) + ChatColor.GREEN + "|"
	    + ChatColor.YELLOW + econ.getName() + ChatColor.GREEN));
	return true;
    }

    public static boolean giveEconomyMoney(Player player, int amount) {
	if (player == null)
	    return false;
	if (amount == 0)
	    return true;
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
	    return false;
	}

	econ.add(player.getName(), amount);
	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("MoneyAdded", ChatColor.YELLOW + String.format("%d", amount) + ChatColor.GREEN + "|"
	    + ChatColor.YELLOW + econ.getName() + ChatColor.GREEN));
	return true;
    }

    public TransactionManager(ResidenceManager m, PermissionManager g) {
	gm = g;
	manager = m;
	sellAmount = Collections.synchronizedMap(new HashMap<String, Integer>());
    }

    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
	if (Residence.getConfigManager().enabledRentSystem()) {
	    if (Residence.getRentManager().isForRent(areaname)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentSellFail"));
		return;
	    }
	}
	if (!resadmin) {
	    if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
		return;
	    }
	    boolean cansell = Residence.getPermissionManager().getGroup(player).canSellLand() || player.hasPermission("residence.sell");
	    if (!cansell && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return;
	    }
	    if (amount <= 0) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
		return;
	    }
	}
	ClaimedResidence area = manager.getByName(areaname);
	if (area == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return;
	}
	if (!area.isOwner(player) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return;
	}
	if (sellAmount.containsKey(areaname)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AlreadySellFail"));
	    return;
	}
	sellAmount.put(areaname, amount);

	Residence.getSignUtil().CheckSign(area);

	player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceForSale", ChatColor.YELLOW + areaname + ChatColor.GREEN + "|" + ChatColor.YELLOW
	    + amount + ChatColor.GREEN));
    }

    public boolean putForSale(String areaname, int amount) {
	if (Residence.getConfigManager().enabledRentSystem()) {
	    if (Residence.getRentManager().isForRent(areaname)) {
		return false;
	    }
	}
	ClaimedResidence area = manager.getByName(areaname);
	if (area == null) {
	    return false;
	}
	if (sellAmount.containsKey(areaname)) {
	    return false;
	}
	sellAmount.put(areaname, amount);
	return true;
    }

    public void buyPlot(String areaname, Player player, boolean resadmin) {
	PermissionGroup group = gm.getGroup(player);
	if (!resadmin) {
	    if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
		return;
	    }
	    boolean canbuy = group.canBuyLand() || player.hasPermission("residence.buy");
	    if (!canbuy && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return;
	    }
	}
	if (isForSale(areaname)) {
	    ClaimedResidence res = manager.getByName(areaname);
	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidArea"));
		sellAmount.remove(areaname);
		return;
	    }
	    if (res.getPermissions().getOwner().equals(player.getName())) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("OwnerBuyFail"));
		return;
	    }
	    if (Residence.getResidenceManager().getOwnedZoneCount(player.getName()) >= group.getMaxZones(player.getName()) && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceTooMany"));
		return;
	    }
	    Server serv = Residence.getServ();
	    int amount = sellAmount.get(areaname);
	    if (!resadmin) {
		if (!group.buyLandIgnoreLimits()) {
		    CuboidArea[] areas = res.getAreaArray();
		    for (CuboidArea thisarea : areas) {
			if (!group.inLimits(thisarea)) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceBuyTooBig"));
			    return;
			}
		    }
		}
	    }
	    EconomyInterface econ = Residence.getEconomyManager();
	    if (econ == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
		return;
	    }
	    String buyerName = player.getName();
	    String sellerName = res.getPermissions().getOwner();
	    Player sellerNameFix = Residence.getServ().getPlayer(sellerName);
	    if (sellerNameFix != null) {
		sellerName = sellerNameFix.getName();
	    }
	    if (econ.canAfford(buyerName, amount)) {
		if (!econ.transfer(buyerName, sellerName, amount)) {
		    player.sendMessage(ChatColor.RED + "Error, could not transfer " + amount + " from " + buyerName + " to " + sellerName);
		    return;
		}
		res.getPermissions().setOwner(player.getName(), true);
		res.getPermissions().applyDefaultFlags();
		this.removeFromSale(areaname);

		Residence.getSignUtil().CheckSign(res);

		CuboidArea area = res.getAreaArray()[0];
		Residence.getSelectionManager().NewMakeBorders(player, area.getHighLoc(), area.getLowLoc(), false);

		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("MoneyCharged", ChatColor.YELLOW + String.format("%d", amount) + ChatColor.GREEN
		    + "|" + ChatColor.YELLOW + econ.getName() + ChatColor.GREEN));
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceBought", ChatColor.GREEN + areaname + ChatColor.YELLOW));
		Player seller = serv.getPlayer(sellerName);
		if (seller != null && seller.isOnline()) {
		    seller.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceBuy", ChatColor.YELLOW + player.getName() + ChatColor.GREEN + "|"
			+ ChatColor.YELLOW + areaname + ChatColor.GREEN));
		    seller.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("MoneyCredit", ChatColor.YELLOW + String.format("%d", amount) + ChatColor.GREEN
			+ "|" + ChatColor.YELLOW + econ.getName() + ChatColor.GREEN));
		}
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotEnoughMoney"));
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	}
    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
	ClaimedResidence area = manager.getByName(areaname);
	if (area != null) {
	    if (!isForSale(areaname)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceNotForSale"));
		return;
	    }
	    if (area.isOwner(player)|| resadmin) {
		removeFromSale(areaname);
		Residence.getSignUtil().CheckSign(area);
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceStopSelling"));
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidArea"));
	}
    }

    public void removeFromSale(String areaname) {
	sellAmount.remove(areaname);
	Residence.getSignUtil().removeSign(areaname);
    }

    public boolean isForSale(String areaname) {
	return sellAmount.containsKey(areaname);
    }

    public void viewSaleInfo(String areaname, Player player) {
	if (sellAmount.containsKey(areaname)) {
	    player.sendMessage("------------------------");
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("AreaName") + ":" + ChatColor.DARK_GREEN + " " + areaname);
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("SellAmount") + ":" + ChatColor.RED + " " + sellAmount.get(areaname));
	    if (Residence.getConfigManager().useLeases()) {
		String etime = Residence.getLeaseManager().getExpireTime(areaname);
		if (etime != null) {
		    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LeaseExpire") + ":" + ChatColor.GREEN + " " + etime.toString());
		}
	    }
	    player.sendMessage("------------------------");
	}
    }

    public void printForSaleResidences(Player player) {
	Set<Entry<String, Integer>> set = sellAmount.entrySet();
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LandForSale") + ":");
	StringBuilder sbuild = new StringBuilder();
	sbuild.append(ChatColor.GREEN);
	boolean firstadd = true;
	for (Entry<String, Integer> land : set) {
	    if (!firstadd) {
		sbuild.append(", ");
	    } else {
		firstadd = false;
	    }
	    sbuild.append(land.getKey());
	}
	player.sendMessage(sbuild.toString());
    }

    public void clearSales() {
	sellAmount.clear();
	System.out.println("[Residence] - ReInit land selling.");
    }

    public int getSaleAmount(String name) {
	return sellAmount.get(name);
    }

    public Map<String, Integer> save() {
	return sellAmount;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static TransactionManager load(Map root, PermissionManager p, ResidenceManager r) {
	TransactionManager tman = new TransactionManager(r, p);
	if (root != null) {
	    tman.sellAmount = root;
	}
	return tman;
    }
}

package com.bekvon.bukkit.residence.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketBuyInterface;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class TransactionManager implements MarketBuyInterface {
    ResidenceManager manager;
    private Map<String, Integer> sellAmount;
    PermissionManager gm;

    public static boolean chargeEconomyMoney(Player player, int amount) {
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return false;
	}
	if (!econ.canAfford(player.getName(), amount)) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	    return false;
	}
	econ.subtract(player.getName(), amount);
	player.sendMessage(Residence.getLM().getMessage("Economy.MoneyCharged", String.format("%d", amount), econ.getName()));
	return true;
    }

    public void updateRentableName(String oldName, String newName) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && oldName != null && newName != null) {
	    oldName = oldName.toLowerCase();
	    newName = newName.toLowerCase();
	}
	if (sellAmount.containsKey(oldName)) {
	    sellAmount.put(newName, sellAmount.get(oldName));
	    sellAmount.remove(oldName);
	}
    }

    public static boolean giveEconomyMoney(Player player, int amount) {
	if (player == null)
	    return false;
	if (amount == 0)
	    return true;
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return false;
	}

	econ.add(player.getName(), amount);
	player.sendMessage(Residence.getLM().getMessage("Economy.MoneyAdded", String.format("%d", amount), econ.getName()));
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
		player.sendMessage(Residence.getLM().getMessage("Economy.RentSellFail"));
		return;
	    }
	}
	if (!resadmin) {
	    if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
		player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
		return;
	    }
	    boolean cansell = Residence.getPermissionManager().getGroup(player).canSellLand() || player.hasPermission("residence.sell");
	    if (!cansell && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return;
	    }
	    if (amount <= 0) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
		return;
	    }
	}
	ClaimedResidence area = manager.getByName(areaname);
	if (area == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	areaname = area.getName();

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
	    areaname = areaname.toLowerCase();

	if (!area.isOwner(player) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return;
	}
	if (sellAmount.containsKey(areaname)) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.AlreadySellFail"));
	    return;
	}
	sellAmount.put(areaname, amount);

	Residence.getSignUtil().CheckSign(area);

	player.sendMessage(Residence.getLM().getMessage("Residence.ForSale", areaname, amount));
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

	areaname = area.getName();

	if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
	    areaname = areaname.toLowerCase();

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
		player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
		return;
	    }
	    boolean canbuy = group.canBuyLand() || player.hasPermission("residence.buy");
	    if (!canbuy && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return;
	    }
	}
	if (isForSale(areaname)) {
	    ClaimedResidence res = manager.getByName(areaname);
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Area"));
		sellAmount.remove(areaname);
		return;
	    }

	    areaname = res.getName();

	    if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
		areaname = areaname.toLowerCase();

	    if (res.getPermissions().getOwner().equals(player.getName())) {
		player.sendMessage(Residence.getLM().getMessage("Economy.OwnerBuyFail"));
		return;
	    }
	    if (Residence.getResidenceManager().getOwnedZoneCount(player.getName()) >= group.getMaxZones(player.getName()) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Residence.TooMany"));
		return;
	    }
	    Server serv = Residence.getServ();
	    int amount = sellAmount.get(areaname);
	    if (!resadmin) {
		if (!group.buyLandIgnoreLimits()) {
		    CuboidArea[] areas = res.getAreaArray();
		    for (CuboidArea thisarea : areas) {
			if (!group.inLimits(thisarea)) {
			    player.sendMessage(Residence.getLM().getMessage("Residence.BuyTooBig"));
			    return;
			}
		    }
		}
	    }
	    EconomyInterface econ = Residence.getEconomyManager();
	    if (econ == null) {
		player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
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

		player.sendMessage(Residence.getLM().getMessage("Economy.MoneyCharged", String.format("%d", amount), econ.getName()));
		player.sendMessage(Residence.getLM().getMessage("Residence.Bought", areaname));
		Player seller = serv.getPlayer(sellerName);
		if (seller != null && seller.isOnline()) {
		    seller.sendMessage(Residence.getLM().getMessage("Residence.Buy", player.getName(), areaname));
		    seller.sendMessage(Residence.getLM().getMessage("Economy.MoneyCredit", String.format("%d", amount), econ.getName()));
		}
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	}
    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
	ClaimedResidence area = manager.getByName(areaname);
	if (area != null) {

	    areaname = area.getName();

	    if (!isForSale(areaname)) {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotForSale"));
		return;
	    }
	    if (area.isOwner(player) || resadmin) {
		removeFromSale(areaname);
		Residence.getSignUtil().CheckSign(area);
		player.sendMessage(Residence.getLM().getMessage("Residence.StopSelling"));
	    } else {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Area"));
	}
    }

    public void removeFromSale(String areaname) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
	    areaname = areaname.toLowerCase();
	sellAmount.remove(areaname);
	Residence.getSignUtil().removeSign(areaname);
    }

    public boolean isForSale(String areaname) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
	    areaname = areaname.toLowerCase();
	return sellAmount.containsKey(areaname);
    }

    public boolean viewSaleInfo(String areaname, Player player) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
	    areaname = areaname.toLowerCase();
	if (!sellAmount.containsKey(areaname))
	    return false;

	player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	player.sendMessage(Residence.getLM().getMessage("Area.Name", areaname));
	player.sendMessage(Residence.getLM().getMessage("Economy.SellAmount", sellAmount.get(areaname)));
	if (Residence.getConfigManager().useLeases()) {
	    String etime = Residence.getLeaseManager().getExpireTime(areaname);
	    if (etime != null) {
		player.sendMessage(Residence.getLM().getMessage("Economy.LeaseExpire", etime));
	    }
	}
	player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	return true;
    }

    public void printForSaleResidences(Player player, int page) {
	Set<Entry<String, Integer>> set = sellAmount.entrySet();
	List<String> toRemove = new ArrayList<String>();
	player.sendMessage(Residence.getLM().getMessage("Economy.LandForSale"));
	StringBuilder sbuild = new StringBuilder();
	sbuild.append(ChatColor.GREEN);

	int perpage = 10;

	int pagecount = (int) Math.ceil((double) set.size() / (double) perpage);

	if (page < 1)
	    page = 1;

	int z = 0;
	for (Entry<String, Integer> land : set) {
	    z++;
	    if (z <= (page - 1) * perpage)
		continue;
	    if (z > (page - 1) * perpage + perpage)
		break;

	    ClaimedResidence res = Residence.getResidenceManager().getByName(land.getKey());

	    if (res == null) {
		z--;
		toRemove.add(land.getKey());
		continue;
	    }

	    player.sendMessage(Residence.getLM().getMessage("Economy.SellList", z, land.getKey(), land.getValue(), res.getOwner()));
	}

	for (String one : toRemove) {
	    sellAmount.remove(one);
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

    public void clearSales() {
	sellAmount.clear();
	System.out.println("[Residence] - ReInit land selling.");
    }

    public int getSaleAmount(String areaname) {
	if (!Residence.getConfigManager().isResCreateCaseSensitive() && areaname != null)
	    areaname = areaname.toLowerCase();
	return sellAmount.get(areaname);
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

    public Map<String, Integer> getBuyableResidences() {
	return sellAmount;
    }
}

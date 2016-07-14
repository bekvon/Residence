package com.bekvon.bukkit.residence.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketBuyInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class TransactionManager implements MarketBuyInterface {
    private Set<ClaimedResidence> sellAmount;

    public TransactionManager() {
	sellAmount = new HashSet<ClaimedResidence>();
    }

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

    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	putForSale(res, player, amount, resadmin);
    }

    public void putForSale(ClaimedResidence res, Player player, int amount, boolean resadmin) {

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	if (Residence.getConfigManager().enabledRentSystem()) {
	    if (res.isForRent()) {
		player.sendMessage(Residence.getLM().getMessage("Economy.RentSellFail"));
		return;
	    }
	}
	if (!resadmin) {
	    if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
		player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
		return;
	    }

	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    boolean cansell = rPlayer.getGroup().canSellLand() || player.hasPermission("residence.sell");
	    if (!cansell && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return;
	    }
	    if (amount <= 0) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
		return;
	    }
	}

	if (!res.isOwner(player) && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return;
	}
	if (sellAmount.contains(res)) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.AlreadySellFail"));
	    return;
	}
	res.setSellPrice(amount);
	sellAmount.add(res);
	Residence.getSignUtil().CheckSign(res);
	player.sendMessage(Residence.getLM().getMessage("Residence.ForSale", res.getName(), amount));
    }

    @Override
    public boolean putForSale(String areaname, int amount) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	return putForSale(res, amount);
    }

    public boolean putForSale(ClaimedResidence res, int amount) {
	if (res == null)
	    return false;

	if (Residence.getConfigManager().enabledRentSystem() && res.isForRent())
	    return false;

	if (sellAmount.contains(res))
	    return false;

	res.setSellPrice(amount);
	sellAmount.add(res);
	return true;
    }

    @Override
    public void buyPlot(String areaname, Player player, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	buyPlot(res, player, resadmin);
    }

    public void buyPlot(ClaimedResidence res, Player player, boolean resadmin) {
	if (res == null || !res.isForSell()) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return;
	}

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
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

	if (res.getPermissions().getOwner().equals(player.getName())) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.OwnerBuyFail"));
	    return;
	}
	if (Residence.getResidenceManager().getOwnedZoneCount(player.getName()) >= rPlayer.getMaxRes() && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.TooMany"));
	    return;
	}
	Server serv = Residence.getServ();
	int amount = res.getSellPrice();

	if (!resadmin && !group.buyLandIgnoreLimits()) {
	    CuboidArea[] areas = res.getAreaArray();
	    for (CuboidArea thisarea : areas) {
		if (!group.inLimits(thisarea)) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.BuyTooBig"));
		    return;
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
	    removeFromSale(res);

	    Residence.getSignUtil().CheckSign(res);

	    CuboidArea area = res.getAreaArray()[0];
	    Residence.getSelectionManager().NewMakeBorders(player, area.getHighLoc(), area.getLowLoc(), false);

	    player.sendMessage(Residence.getLM().getMessage("Economy.MoneyCharged", String.format("%d", amount), econ.getName()));
	    player.sendMessage(Residence.getLM().getMessage("Residence.Bought", res.getResidenceName()));
	    Player seller = serv.getPlayer(sellerName);
	    if (seller != null && seller.isOnline()) {
		seller.sendMessage(Residence.getLM().getMessage("Residence.Buy", player.getName(), res.getResidenceName()));
		seller.sendMessage(Residence.getLM().getMessage("Economy.MoneyCredit", String.format("%d", amount), econ.getName()));
	    }
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.NotEnoughMoney"));
	}

    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	removeFromSale(player, res, resadmin);
    }

    public void removeFromSale(Player player, ClaimedResidence res, boolean resadmin) {
	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Area"));
	    return;
	}

	if (!res.isForSell()) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.NotForSale"));
	    return;
	}
	if (res.isOwner(player) || resadmin) {
	    removeFromSale(res);
	    Residence.getSignUtil().CheckSign(res);
	    player.sendMessage(Residence.getLM().getMessage("Residence.StopSelling"));
	} else {
	    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	}
    }

    @Override
    public void removeFromSale(String areaname) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	removeFromSale(res);
    }

    public void removeFromSale(ClaimedResidence res) {
	if (res == null)
	    return;
	sellAmount.remove(res);
	Residence.getSignUtil().removeSign(res);
    }

    @Override
    public boolean isForSale(String areaname) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	return isForSale(res);
    }

    public boolean isForSale(ClaimedResidence res) {
	if (res == null)
	    return false;
	return sellAmount.contains(res);
    }

    public boolean viewSaleInfo(String areaname, Player player) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	return viewSaleInfo(res, player);
    }

    public boolean viewSaleInfo(ClaimedResidence res, Player player) {

	if (res == null || !res.isForSell()) {
	    return false;
	}

	if (!sellAmount.contains(res))
	    return false;

	player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	player.sendMessage(Residence.getLM().getMessage("Area.Name", res.getName()));
	player.sendMessage(Residence.getLM().getMessage("Economy.SellAmount", res.getSellPrice()));
	if (Residence.getConfigManager().useLeases()) {
	    String etime = Residence.getLeaseManager().getExpireTime(res.getName());
	    if (etime != null) {
		player.sendMessage(Residence.getLM().getMessage("Economy.LeaseExpire", etime));
	    }
	}
	player.sendMessage(Residence.getLM().getMessage("General.Separator"));
	return true;
    }

    public void printForSaleResidences(Player player, int page) {
	List<ClaimedResidence> toRemove = new ArrayList<ClaimedResidence>();
	player.sendMessage(Residence.getLM().getMessage("Economy.LandForSale"));
	StringBuilder sbuild = new StringBuilder();
	sbuild.append(ChatColor.GREEN);

	int perpage = 10;

	int pagecount = (int) Math.ceil((double) sellAmount.size() / (double) perpage);

	if (page < 1)
	    page = 1;

	int z = 0;
	for (ClaimedResidence res : sellAmount) {
	    z++;
	    if (z <= (page - 1) * perpage)
		continue;
	    if (z > (page - 1) * perpage + perpage)
		break;

	    if (res == null) {
		z--;
		toRemove.add(res);
		continue;
	    }
	    player.sendMessage(Residence.getLM().getMessage("Economy.SellList", z, res.getName(), res.getSellPrice(), res.getOwner()));
	}

	for (ClaimedResidence one : toRemove) {
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
	for (ClaimedResidence res : sellAmount) {
	    if (res == null)
		continue;
	    res.setSellPrice(-1);
	}
	sellAmount.clear();
	System.out.println("[Residence] - ReInit land selling.");
    }

    @Override
    public int getSaleAmount(String areaname) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	return getSaleAmount(res);
    }

    public int getSaleAmount(ClaimedResidence res) {
	if (res == null)
	    return -1;
	return res.getSellPrice();
    }

    public void load(Map<String, Integer> root) {
	if (root == null)
	    return;

	for (Entry<String, Integer> one : root.entrySet()) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(one.getKey());
	    if (res == null)
		continue;
	    res.setSellPrice(one.getValue());
	    sellAmount.add(res);
	}
    }

    @Override
    public Map<String, Integer> getBuyableResidences() {
	Map<String, Integer> list = new HashMap<String, Integer>();
	for (ClaimedResidence res : sellAmount) {
	    if (res == null)
		continue;
	    list.put(res.getName(), res.getSellPrice());
	}
	return list;
    }

    public Map<String, Integer> save() {
	return getBuyableResidences();
    }
}

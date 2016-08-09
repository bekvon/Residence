package com.bekvon.bukkit.residence.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketBuyInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
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

    public static boolean chargeEconomyMoney(Player player, double chargeamount) {
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    Residence.msg(player, lm.Economy_MarketDisabled);
	    return false;
	}
	if (!econ.canAfford(player.getName(), chargeamount)) {
	    Residence.msg(player, lm.Economy_NotEnoughMoney);
	    return false;
	}
	econ.subtract(player.getName(), chargeamount);
	try {
	    Residence.msg(player, lm.Economy_MoneyCharged, chargeamount, econ.getName());
	} catch (Exception e) {
	}
	return true;
    }

    public static boolean giveEconomyMoney(Player player, int amount) {
	if (player == null)
	    return false;
	if (amount == 0)
	    return true;
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    Residence.msg(player, lm.Economy_MarketDisabled);
	    return false;
	}

	econ.add(player.getName(), amount);
	Residence.msg(player, lm.Economy_MoneyAdded, String.format("%d", amount), econ.getName());
	return true;
    }

    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	putForSale(res, player, amount, resadmin);
    }

    public void putForSale(ClaimedResidence res, Player player, int amount, boolean resadmin) {

	if (res == null) {
	    Residence.msg(player, lm.Invalid_Residence);
	    return;
	}

	if (Residence.getConfigManager().enabledRentSystem()) {
	    if (!resadmin) {
		if (res.isForRent()) {
		    Residence.msg(player, lm.Economy_RentSellFail);
		    return;
		}
		if (res.isSubzoneForRent()) {
		    Residence.msg(player, lm.Economy_SubzoneRentSellFail);
		    return;
		}
		if (res.isParentForRent()) {
		    Residence.msg(player, lm.Economy_ParentRentSellFail);
		    return;
		}
	    }
	}

	if (!Residence.getConfigManager().isSellSubzone()) {
	    if (res.isSubzone()) {
		Residence.msg(player, lm.Economy_SubzoneSellFail);
		return;
	    }
	}

	if (!resadmin) {
	    if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
		Residence.msg(player, lm.Economy_MarketDisabled);
		return;
	    }

	    ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	    boolean cansell = rPlayer.getGroup().canSellLand() || player.hasPermission("residence.sell");
	    if (!cansell && !resadmin) {
		Residence.msg(player, lm.General_NoPermission);
		return;
	    }
	    if (amount <= 0) {
		Residence.msg(player, lm.Invalid_Amount);
		return;
	    }
	}

	if (!res.isOwner(player) && !resadmin) {
	    Residence.msg(player, lm.General_NoPermission);
	    return;
	}
	if (sellAmount.contains(res)) {
	    Residence.msg(player, lm.Economy_AlreadySellFail);
	    return;
	}
	res.setSellPrice(amount);
	sellAmount.add(res);
	Residence.getSignUtil().CheckSign(res);
	Residence.msg(player, lm.Residence_ForSale, res.getName(), amount);
    }

    @Override
    public boolean putForSale(String areaname, int amount) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	return putForSale(res, amount);
    }

    public boolean putForSale(ClaimedResidence res, int amount) {
	if (res == null)
	    return false;

	if (Residence.getConfigManager().enabledRentSystem() && (res.isForRent() || res.isSubzoneForRent() || res.isParentForRent()))
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
	    Residence.msg(player, lm.Invalid_Residence);
	    return;
	}

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (!resadmin) {
	    if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
		Residence.msg(player, lm.Economy_MarketDisabled);
		return;
	    }
	    boolean canbuy = group.canBuyLand() || player.hasPermission("residence.buy");
	    if (!canbuy && !resadmin) {
		Residence.msg(player, lm.General_NoPermission);
		return;
	    }
	}

	if (res.getPermissions().getOwner().equals(player.getName())) {
	    Residence.msg(player, lm.Economy_OwnerBuyFail);
	    return;
	}
	if (Residence.getResidenceManager().getOwnedZoneCount(player.getName()) >= rPlayer.getMaxRes() && !resadmin) {
	    Residence.msg(player, lm.Residence_TooMany);
	    return;
	}
	Server serv = Residence.getServ();
	int amount = res.getSellPrice();

	if (!resadmin && !group.buyLandIgnoreLimits()) {
	    CuboidArea[] areas = res.getAreaArray();
	    for (CuboidArea thisarea : areas) {
		if (!res.isSubzone() && !res.isSmallerThanMax(player, thisarea, resadmin) || res.isSubzone() && !res.isSmallerThanMaxSubzone(player, thisarea,
		    resadmin)) {
		    Residence.msg(player, lm.Residence_BuyTooBig);
		    return;
		}
	    }
	}

	EconomyInterface econ = Residence.getEconomyManager();
	if (econ == null) {
	    Residence.msg(player, lm.Economy_MarketDisabled);
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

	    if (Residence.getConfigManager().isRemoveLwcOnBuy())
		Residence.getResidenceManager().removeLwcFromResidence(player, res);

	    Residence.getSignUtil().CheckSign(res);

	    Visualizer v = new Visualizer(player);
	    v.setAreas(res);
	    Residence.getSelectionManager().showBounds(player, v);

	    Residence.msg(player, lm.Economy_MoneyCharged, String.format("%d", amount), econ.getName());
	    Residence.msg(player, lm.Residence_Bought, res.getResidenceName());
	    Player seller = serv.getPlayer(sellerName);
	    if (seller != null && seller.isOnline()) {
		seller.sendMessage(Residence.msg(lm.Residence_Buy, player.getName(), res.getResidenceName()));
		seller.sendMessage(Residence.msg(lm.Economy_MoneyCredit, String.format("%d", amount), econ.getName()));
	    }
	} else {
	    Residence.msg(player, lm.Economy_NotEnoughMoney);
	}

    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
	removeFromSale(player, res, resadmin);
    }

    public void removeFromSale(Player player, ClaimedResidence res, boolean resadmin) {
	if (res == null) {
	    Residence.msg(player, lm.Invalid_Area);
	    return;
	}

	if (!res.isForSell()) {
	    Residence.msg(player, lm.Residence_NotForSale);
	    return;
	}
	if (res.isOwner(player) || resadmin) {
	    removeFromSale(res);
	    Residence.getSignUtil().CheckSign(res);
	    Residence.msg(player, lm.Residence_StopSelling);
	} else {
	    Residence.msg(player, lm.General_NoPermission);
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

	Residence.msg(player, lm.General_Separator);
	Residence.msg(player, lm.Area_Name, res.getName());
	Residence.msg(player, lm.Economy_SellAmount, res.getSellPrice());
	if (Residence.getConfigManager().useLeases()) {
	    String etime = Residence.getLeaseManager().getExpireTime(res.getName());
	    if (etime != null) {
		Residence.msg(player, lm.Economy_LeaseExpire, etime);
	    }
	}
	Residence.msg(player, lm.General_Separator);
	return true;
    }

    public void printForSaleResidences(Player player, int page) {
	List<ClaimedResidence> toRemove = new ArrayList<ClaimedResidence>();
	Residence.msg(player, lm.Economy_LandForSale);
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
	    Residence.msg(player, lm.Economy_SellList, z, res.getName(), res.getSellPrice(), res.getOwner());
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
	String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.msg(lm.General_PrevInfoPage)
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = "/res market list sell " + NextPage;
	String next = " {\"text\":\"" + Residence.msg(lm.General_NextInfoPage) + " " + separator
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

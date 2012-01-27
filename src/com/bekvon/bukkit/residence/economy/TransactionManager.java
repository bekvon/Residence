/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Collections;
import java.util.Date;
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
    private Map<String,Integer> sellAmount;
    PermissionManager gm;

    public static boolean chargeEconomyMoney(Player player, int amount)
    {
        EconomyInterface econ = Residence.getEconomyManager();
        if(econ==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("MarketDisabled"));
            return false;
        }
        if(!econ.canAfford(player.getName(), amount))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NotEnoughMoney"));
            return false;
        }
        econ.subtract(player.getName(), amount);
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("MoneyCharged","§e"+amount + "§a.§e" +econ.getName() +"§a"));
        return true;
    }

    public TransactionManager(ResidenceManager m, PermissionManager g)
    {
        gm = g;
        manager = m;
        sellAmount = Collections.synchronizedMap(new HashMap<String,Integer>());
    }

    public void putForSale(String areaname, Player player, int amount, boolean resadmin)
    {
        if(Residence.getConfigManager().enabledRentSystem())
        {
            if(Residence.getRentManager().isForRent(areaname))
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("RentSellFail"));
                return;
            }
        }
        if(!resadmin)
        {
            if(!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager()==null)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("MarketDisabled"));
                return;
            }
            boolean cansell = Residence.getPermissionManager().getGroup(player).canSellLand() || Residence.getPermissionManager().hasAuthority(player, "residence.sell");
            if(!cansell && !resadmin)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
            if(amount<=0)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidAmount"));
                return;
            }
        }
        String pname = player.getName();
        ClaimedResidence area = manager.getByName(areaname);
        if(area==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        if(!area.getPermissions().getOwner().equals(pname) && !resadmin)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if(sellAmount.containsKey(areaname))
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("AlreadySellFail"));
            return;
        }
        sellAmount.put(areaname, amount);
        player.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceForSale","§e" + areaname + "§a.§e" + amount + "§a"));
    }

    public void buyPlot(String areaname, Player player, boolean resadmin)
    {
        PermissionGroup group = gm.getGroup(player);
        if(!resadmin)
        {
            
            if(!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager()==null)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("MarketDisabled"));
                return;
            }
            boolean canbuy = group.canBuyLand() || Residence.getPermissionManager().hasAuthority(player, "residence.buy");
            if(!canbuy && !resadmin)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
        }
        if(isForSale(areaname))
        {
            ClaimedResidence res = manager.getByName(areaname);
            if(res == null)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidArea"));
                sellAmount.remove(areaname);
                return;
            }
            if(res.getPermissions().getOwner().equals(player.getName()))
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("OwnerBuyFail"));
                return;
            }
            if (Residence.getResidenceManager().getOwnedZoneCount(player.getName()) >= group.getMaxZones() && !resadmin) {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceTooMany"));
                return;
            }
            Server serv = Residence.getServ();
            int amount = sellAmount.get(areaname);
            if(!resadmin)
            {
                if(!group.buyLandIgnoreLimits())
                {
                    CuboidArea[] areas = res.getAreaArray();
                    for(CuboidArea thisarea : areas)
                    {
                        if(!group.inLimits(thisarea))
                        {
                            player.sendMessage("§c" + Residence.getLanguage().getPhrase("ResidenceBuyTooBig"));
                            return;
                        }
                    }
                }
            }
            EconomyInterface econ = Residence.getEconomyManager();
            if(econ==null)
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("MarketDisabled"));
                return;
            }
            String buyerName = player.getName();
            String sellerName = res.getPermissions().getOwner();
            Player sellerNameFix = Residence.getServ().getPlayer(sellerName);
            if(sellerNameFix!=null)
                sellerName = sellerNameFix.getName();
            if(econ.canAfford(buyerName, amount))
            {
                if (!econ.transfer(buyerName, sellerName, amount))
                {
                    player.sendMessage("§cError, could not transfer " + amount + " from " + buyerName + " to " + sellerName);
                    return;
                }
                res.getPermissions().setOwner(player.getName(),true);
                res.getPermissions().applyDefaultFlags();
                this.removeFromSale(areaname);
                player.sendMessage("§a"+Residence.getLanguage().getPhrase("MoneyCharged","§e"+amount + "§a.§e" +econ.getName() +"§a"));
                player.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceBought","§a" + areaname + "§e"));
                Player seller = serv.getPlayer(sellerName);
                if(seller!=null && seller.isOnline())
                {
                    seller.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceBuy","§e"+ player.getName() + "§a.§e" + areaname+"§a"));
                    seller.sendMessage("§a"+Residence.getLanguage().getPhrase("MoneyCredit","§e"+amount + "§a.§e" +econ.getName() +"§a"));
                }
            }
            else
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NotEnoughMoney"));
            }
        }
        else
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
        }
    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
        ClaimedResidence area = manager.getByName(areaname);
        if (area != null) {
            if(!isForSale(areaname))
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("ResidenceNotForSale"));
                return;
            }
            if (area.getPermissions().getOwner().equals(player.getName()) || resadmin) {
                removeFromSale(areaname);
                player.sendMessage("§a"+Residence.getLanguage().getPhrase("ResidenceStopSelling"));
            }
            else
            {
                player.sendMessage("§c"+Residence.getLanguage().getPhrase("NoPermission"));
            }
        } else {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidArea"));
        }
    }

    public void removeFromSale(String areaname)
    {
        sellAmount.remove(areaname);
    }

    public boolean isForSale(String areaname)
    {
        return sellAmount.containsKey(areaname);
    }

    public void viewSaleInfo(String areaname, Player player)
    {
        if(sellAmount.containsKey(areaname))
        {
            player.sendMessage("------------------------");
            player.sendMessage("§eName:§2 " + areaname);
            player.sendMessage("§e"+Residence.getLanguage().getPhrase("SellAmount")+":§c " + sellAmount.get(areaname));
            if(Residence.getConfigManager().useLeases())
            {
                Date etime = Residence.getLeaseManager().getExpireTime(areaname);
                if(etime!=null)
                    player.sendMessage("§e"+Residence.getLanguage().getPhrase("LeaseExpire")+":§a " + etime.toString());
            }
            player.sendMessage("------------------------");
        }
    }

    public void printForSaleResidences(Player player) {
        Set<Entry<String, Integer>> set = sellAmount.entrySet();
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("LandForSale")+":");
        StringBuilder sbuild = new StringBuilder();
        sbuild.append("§a");
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

    public void clearSales()
    {
        sellAmount.clear();
        System.out.println("[Residence] - ReInit land selling.");
    }

    public int getSaleAmount(String name)
    {
        return sellAmount.get(name);
    }

    public Map<String,Integer> save()
    {
        return sellAmount;
    }

    public static TransactionManager load(Map root, PermissionManager p, ResidenceManager r) {
        TransactionManager tman = new TransactionManager(r,p);
        if(root!=null)
            tman.sellAmount = Collections.synchronizedMap(root);
        return tman;
    }
}

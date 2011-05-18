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

    public static boolean chargeEconomyMoney(Player player, int amount, String reason)
    {
        EconomyInterface econ = Residence.getEconomyManager();
        if(econ==null)
        {
            player.sendMessage("§cError, no economy interface is available.");
            return false;
        }
        if(!econ.canAfford(player.getName(), amount))
        {
            player.sendMessage("§cNot enough money for " + reason + ", you need: " + amount);
            return false;
        }
        econ.subtract(player.getName(), amount);
        player.sendMessage("§aCharged " + amount + " to your " + econ.getName() + " account for " + reason + ".");
        return true;
    }

    public static boolean chargeEconomyMoney(String playername, int amount, String reason)
    {
        EconomyInterface econ = Residence.getEconomyManager();
        Player player = Residence.getServ().getPlayer(playername);
        if(econ==null)
        {
            if(player!=null)
                player.sendMessage("§cError, no economy interface is available.");
            return false;
        }
        if(!econ.canAfford(playername, amount))
        {
            if(player!=null)
                player.sendMessage("§cNot enough money for " + reason + ", you need: " + amount);
            return false;
        }
        econ.subtract(playername, amount);
        if(player!=null)
            player.sendMessage("§aCharged " + amount + " to your " + econ.getName() + " account for " + reason + ".");
        return true;
    }

    public TransactionManager(ResidenceManager m, PermissionManager g)
    {
        gm = g;
        manager = m;
        sellAmount = Collections.synchronizedMap(new HashMap<String,Integer>());
    }

    public void putForSale(String areaname, Player player, int amount)
    {
        if(Residence.getConfig().enabledRentSystem())
        {
            if(Residence.getRentManager().isForRent(areaname))
            {
                player.sendMessage("§cCannot sell a Residence if it is for rent!");
                return;
            }
        }
        if(!Residence.getPermissionManager().isResidenceAdmin(player))
        {
            if(!Residence.getConfig().enableEconomy() || Residence.getEconomyManager()==null)
            {
                player.sendMessage("§cError, buying / selling disabled.");
                return;
            }
            if(!Residence.getPermissionManager().getGroup(player).canSellLand() && !Residence.getPermissionManager().isResidenceAdmin(player))
            {
                player.sendMessage("§cYou dont have permission to sell plots.");
                return;
            }
            if(amount<=0)
            {
                player.sendMessage("§cInvalid money amount, must be larger then 0.");
                return;
            }
        }
        String pname = player.getName();
        ClaimedResidence area = manager.getByName(areaname);
        if(area==null)
        {
            player.sendMessage("§cInvalid residence.");
            return;
        }
        if(!area.getPermissions().getOwner().equals(pname) && !Residence.getPermissionManager().isResidenceAdmin(player))
        {
            player.sendMessage("§cOnly the owner can sell a residence.");
            return;
        }
        if(sellAmount.containsKey(areaname))
        {
            player.sendMessage("§cThis residence is already for sale!");
            return;
        }
        sellAmount.put(areaname, amount);
        player.sendMessage("§aResidence §e" + areaname + "§a is now for sale for §e" + amount + "§a!");
    }

    public void buyPlot(String areaname, Player player)
    {
        PermissionGroup group = gm.getGroup(player);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(!resadmin)
        {
            
            if(!Residence.getConfig().enableEconomy() || Residence.getEconomyManager()==null)
            {
                player.sendMessage("§cError, buying / selling disabled.");
                return;
            }
            if(!group.canBuyLand() && !resadmin)
            {
                player.sendMessage("§cYou dont have permission to buy plots.");
                return;
            }
        }
        if(isForSale(areaname))
        {
            ClaimedResidence res = manager.getByName(areaname);
            if(res == null)
            {
                player.sendMessage("§cInvalid Area.");
                sellAmount.remove(areaname);
                return;
            }
            if(res.getPermissions().getOwner().equals(player.getName()))
            {
                player.sendMessage("§cCan't buy your own land!");
                return;
            }
            if (Residence.getResidenceManger().getOwnedZoneCount(player.getName()) >= group.getMaxZones() && !resadmin) {
                player.sendMessage("§cYou own the max number of areas your allowed to.");
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
                            player.sendMessage("§cThis residence contains areas bigger then your allowed max.");
                            return;
                        }
                    }
                }
            }
            EconomyInterface econ = Residence.getEconomyManager();
            if(econ==null)
            {
                player.sendMessage("§cError, economy system not available.");
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
                    player.sendMessage("§cError, could not transfer $" + amount + " from " + buyerName + " to " + sellerName);
                    return;
                }
                res.getPermissions().setOwner(player.getName(),true);
                this.removeFromSale(areaname);
                player.sendMessage("§aCharged " + amount +" to your " + econ.getName() + " account.");
                player.sendMessage("§aYou bought residence: " + areaname + "!");
                Player seller = serv.getPlayer(sellerName);
                if(seller!=null && seller.isOnline())
                {
                    seller.sendMessage("§a"+ player.getName() + " has bought your residence " + areaname);
                    seller.sendMessage("§a" + amount + " has been credited to your " + econ.getName() + " account.");
                }
            }
            else
            {
                player.sendMessage("§cNot enough " + econ.getName() + " money.");
            }
        }
        else
        {
            player.sendMessage("§cInvalid residence, or not for sale.");
        }
    }

    public void removeFromSale(Player player, String areaname) {
        ClaimedResidence area = manager.getByName(areaname);
        if (area != null) {
            if(!isForSale(areaname))
            {
                player.sendMessage("§cResidence is not for sale.");
                return;
            }
            if (area.getPermissions().getOwner().equals(player.getName()) || Residence.getPermissionManager().isResidenceAdmin(player)) {
                removeFromSale(areaname);
                player.sendMessage("§aNo longer selling.");
            }
            else
            {
                player.sendMessage("§cYou dont have permission to do this.");
            }
        } else {
            player.sendMessage("§cInvalid area!");
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
            player.sendMessage("§ePlotName:§2 " + areaname);
            player.sendMessage("§eSellAmount:§c " + sellAmount.get(areaname));
            if(Residence.getConfig().useLeases())
            {
                Date etime = Residence.getLeaseManager().getExpireTime(areaname);
                if(etime!=null)
                    player.sendMessage("§eLeaseExpireTime:§a " + etime.toString());
            }
            player.sendMessage("------------------------");
        }
    }

    public void printForSaleResidences(Player player) {
        Set<Entry<String, Integer>> set = sellAmount.entrySet();
        player.sendMessage("§eFor Sale Land:");
        StringBuilder sbuild = new StringBuilder();
        sbuild.append("§a");
        boolean firstadd = true;
        for (Entry<String, Integer> land : set) {
            if (!firstadd) {
                sbuild.append(", ");
            } else {
                firstadd = true;
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

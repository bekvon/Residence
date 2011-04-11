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
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public static boolean chargeIConomyMoney(Player player, int amount, String reason)
    {
        if(Residence.getIConManager()==null)
        {
            player.sendMessage("§cError, iConomy not available.");
            return false;
        }
        Account account = iConomy.getBank().getAccount(player.getName());
        if(account==null)
        {
            player.sendMessage("§cError, unable to get your iConomy account.");
            return false;
        }
        if(!account.hasEnough(amount))
        {
            player.sendMessage("§cNot enough money, you need: " + amount);
            return false;
        }
        account.subtract(amount);
        player.sendMessage("§aCharged " + amount + " to your iConomy account for " + reason + ".");
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
        if(!Residence.getPermissionManager().isResidenceAdmin(player))
        {
            if(!Residence.getConfig().buySellEnabled() || Residence.getIConManager()==null)
            {
                player.sendMessage("§cError, buying / selling disabled.");
                return;
            }
            if(!Residence.getPermissionManager().getGroup(player).canSellLand() && !Residence.getPermissionManager().isResidenceAdmin(player))
            {
                player.sendMessage("§cYou dont have permission to sell plots.");
                return;
            }
            if(amount<0)
            {
                player.sendMessage("§cInvalid money amount, must be larger then 0.");
                return;
            }
        }
        String pname = player.getName().toLowerCase();
        ClaimedResidence area = manager.getByName(areaname);
        if(area==null)
        {
            player.sendMessage("§cInvalid residence.");
            return;
        }
        if(!area.getPermissions().getOwner().equals(pname) && gm.hasAuthority(player, "residence.admin", player.isOp()))
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
        player.sendMessage("§aResidence is now for sale!");
    }

    public void buyPlot(String areaname, Player player)
    {
        PermissionGroup group = gm.getGroup(player);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(!resadmin)
        {
            
            if(!Residence.getConfig().buySellEnabled() || Residence.getIConManager()==null)
            {
                player.sendMessage("§cError, buying / selling disabled.");
                return;
            }
            if(!group.canBuyLand() && !gm.hasAuthority(player, "residence.admin", player.isOp()))
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
            if(res.getPermissions().getOwner().equals(player.getName().toLowerCase()))
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
            if(Residence.getIConManager()==null)
            {
                player.sendMessage("§cError, iConomy not available.");
                return;
            }
            Account baccount = iConomy.getBank().getAccount(player.getName());
            Account saccount = iConomy.getBank().getAccount(res.getPermissions().getOwner());
            if(baccount == null || saccount == null)
            {
                player.sendMessage("§cError, unable to get iConomy accounts.");
                return;
            }
            if(baccount.hasEnough(amount))
            {
                baccount.subtract(amount);
                saccount.add(amount);
                res.getPermissions().setOwner(player.getName(),true);
                this.removeFromSale(areaname);
                player.sendMessage("§aCharged " + amount +" to your iConomy account.");
                player.sendMessage("§aYou bought residence: " + areaname + "!");
                Player seller = serv.getPlayer(saccount.getName());
                if(seller!=null && seller.isOnline())
                {
                    seller.sendMessage("§a"+ player.getName() + " has bought your residence " + areaname);
                    seller.sendMessage("§a" + amount + " has been credited to your iConomy account.");
                }
            }
            else
            {
                player.sendMessage("§cNot enough iConomy money.");
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
            if (area.getPermissions().getOwner().equals(player.getName().toLowerCase()) || Residence.getPermissionManager().isResidenceAdmin(player)) {
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
        else
        {
            player.sendMessage("§cInvalid residence, or not for sale.");
        }
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
        tman.sellAmount = Collections.synchronizedMap(root);
        return tman;
    }
}

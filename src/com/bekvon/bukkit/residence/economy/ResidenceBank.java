/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidenceBank {
    int storedMoney;
    ClaimedResidence res;

    public ResidenceBank(ClaimedResidence parent)
    {
        storedMoney = 0;
        res = parent;
    }

    public int getStoredMoney()
    {
        return storedMoney;
    }

    public void setStoredMoney(int amount)
    {
        storedMoney = amount;
    }

    public void add(int amount)
    {
        storedMoney = storedMoney + amount;
    }

    public boolean hasEnough(int amount)
    {
        if(storedMoney >= amount)
            return true;
        return false;
    }

    public void subtract(int amount)
    {
        storedMoney = storedMoney - amount;
        if(storedMoney<0)
            storedMoney = 0;
    }

    public void withdraw(Player player, int amount)
    {
        if(!Residence.getConfig().enableEconomy())
        {
            player.sendMessage("§cEconomy is disabled...");
        }
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(!resadmin && !res.getPermissions().playerHas(player.getName(), "bank", false))
        {
            player.sendMessage("§cYou don't have bank access.");
            return;
        }
        if(!hasEnough(amount))
        {
            player.sendMessage("§cNot enough money in bank.");
            return;
        }
        if(Residence.getEconomyManager().add(player.getName(), amount))
        {
            this.subtract(amount);
            player.sendMessage("§aYou withdraw §e" + amount + "§a from the residence bank.");
        }
    }

    public void deposit(Player player, int amount)
    {
        if(!Residence.getConfig().enableEconomy())
        {
            player.sendMessage("§cEconomy is disabled...");
        }
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(!resadmin && !res.getPermissions().playerHas(player.getName(), "bank", false))
        {
            player.sendMessage("§cYou don't have bank access.");
            return;
        }
        if(!Residence.getEconomyManager().canAfford(player.getName(), amount))
        {
            player.sendMessage("§cYou don't have enough money.");
            return;
        }
        if(Residence.getEconomyManager().subtract(player.getName(), amount))
        {
            this.add(amount);
            player.sendMessage("§aYou deposit §e" + amount + "§a into the residence bank.");
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy;
import org.bukkit.ChatColor;

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

    public void withdraw(Player player, int amount, boolean resadmin)
    {
        if(!Residence.getConfigManager().enableEconomy())
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("MarketDisabled"));
        }
        if(!resadmin && !res.getPermissions().playerHas(player.getName(), "bank", false))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoBankAccess"));
            return;
        }
        if(!hasEnough(amount))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("BankNoMoney"));
            return;
        }
        if(Residence.getEconomyManager().add(player.getName(), amount))
        {
            this.subtract(amount);
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("BankWithdraw",ChatColor.YELLOW + String.format("%d",amount) + ChatColor.GREEN));
        }
    }

    public void deposit(Player player, int amount, boolean resadmin)
    {
        if(!Residence.getConfigManager().enableEconomy())
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("MarketDisabled"));
        }
        if(!resadmin && !res.getPermissions().playerHas(player.getName(), "bank", false))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoBankAccess"));
            return;
        }
        if(!Residence.getEconomyManager().canAfford(player.getName(), amount))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NotEnoughMoney"));
            return;
        }
        if(Residence.getEconomyManager().subtract(player.getName(), amount))
        {
            this.add(amount);
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("BankDeposit",ChatColor.YELLOW + String.format("%d",amount) + ChatColor.GREEN));
        }
    }
}

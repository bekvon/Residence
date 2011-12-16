package com.bekvon.bukkit.residence.economy;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class IConomy4Adapter implements EconomyInterface
{
    private iConomy plugin;

    public IConomy4Adapter(iConomy p)
    {
        plugin = p;
        String serverland = "Server Land";
        if(!plugin.getBank().hasAccount(serverland))
            plugin.getBank().addAccount(serverland, 0);
    }

    public double getBalance(String playerName)
    {
        Account acc = plugin.getBank().getAccount(playerName);
        return (acc == null) ? 0 : acc.getBalance();
    }

    public boolean canAfford(String playerName, double amount)
    {
        if (amount == 0)
        {
            return true;
        }
        Account acc = plugin.getBank().getAccount(playerName);
        return (acc == null) ? false : acc.hasEnough(amount);
    }

    public boolean add(String playerName, double amount)
    {
        Account acc = plugin.getBank().getAccount(playerName);
        if (acc != null)
        {
            acc.add(amount);
            return true;
        }
        return false;
    }

    public boolean subtract(String playerName, double amount)
    {
        Account acc = plugin.getBank().getAccount(playerName);
        if (acc != null)
        {
            acc.subtract(amount);
            return true;
        }
        return false;
    }

    public boolean transfer(String playerFrom, String playerTo, double amount)
    {
        Account accFrom = plugin.getBank().getAccount(playerFrom);
        Account accTo = plugin.getBank().getAccount(playerTo);
        if (accFrom != null && accTo != null)
        {
            accFrom.subtract(amount);
            accTo.add(amount);
            return true;
        }
        return false;
    }

    @Override
    public String getName()
    {
        return "iConomy";
    }
}


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
/**
 *
 * @author Administrator
 */

public class IConomy6Adapter extends EconomyInterface {

    iConomy icon;

    public IConomy6Adapter(iConomy iconomy)
    {
        icon = iconomy;
    }

    @Override
    public double getBalance(String playerName) {
        return new Accounts().get(playerName).getHoldings().getBalance();
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        double holdings = this.getBalance(playerName);
        if(holdings>=amount)
            return true;
        return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
        new Accounts().get(playerName).getHoldings().add(amount);
        return true;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        if(this.canAfford(playerName, amount))
        {
            new Accounts().get(playerName).getHoldings().subtract(amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        if(this.canAfford(playerFrom, amount))
        {
            Account p1 = new Accounts().get(playerFrom);
            Account p2 = new Accounts().get(playerTo);
            p1.getHoldings().subtract(amount);
            p2.getHoldings().add(amount);
            return true;
        }
        return false;
    }

}

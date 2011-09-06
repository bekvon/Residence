/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy;
import com.iCo6.iConomy;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(String playerName, double amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

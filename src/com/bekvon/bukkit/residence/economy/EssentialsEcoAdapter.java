/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

/**
 *
 * @author Administrator
 */
public class EssentialsEcoAdapter implements EconomyInterface {

    Essentials plugin;

    public EssentialsEcoAdapter(Essentials p)
    {
        plugin = p;
        String serverland = "Server Land";
        if(!Economy.playerExists(serverland))
            Economy.createNPC(serverland);
    }

    @Override
    public double getBalance(String playerName) {
        try {
            if(Economy.playerExists(playerName))
                return Economy.getMoney(playerName);
            else
                return 0;
        } catch (UserDoesNotExistException ex) {
            return 0;
        }
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        try {
            if (Economy.playerExists(playerName)) {
                return Economy.hasEnough(playerName, amount);
            }
            return false;
        } catch (UserDoesNotExistException ex) {
            return false;
        }
    }

    @Override
    public boolean add(String playerName, double amount) {
        if(Economy.playerExists(playerName))
        {
            try {
                Economy.add(playerName, amount);
                return true;
            } catch (UserDoesNotExistException ex) {
                return false;
            } catch (NoLoanPermittedException ex) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        if(Economy.playerExists(playerName))
        {
            try {
                Economy.subtract(playerName, amount);
                return true;
            } catch (UserDoesNotExistException ex) {
                return false;
            } catch (NoLoanPermittedException ex) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        try {
            if (Economy.playerExists(playerFrom) && Economy.playerExists(playerTo) && Economy.hasEnough(playerFrom, amount)) {
                if(!subtract(playerFrom,amount))
                    return false;
                if(!add(playerTo,amount))
                {
                    add(playerFrom,amount);
                    return false;
                }
                return true;
            }
        } catch (UserDoesNotExistException ex) {
           return false;
        }
        return false;
    }

    @Override
    public String getName() {
        return "EssentialsEconomy";
    }

}

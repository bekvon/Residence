package com.bekvon.bukkit.residence.economy;

import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;

public class CMIEconomy implements EconomyInterface {

    public CMIEconomy() {
    }

    @Override
    public double getBalance(Player player) {
	CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
	return user == null ? 0D : user.getBalance();
    }

    @Override
    public double getBalance(String playerName) {
	CMIUser user = CMI.getInstance().getPlayerManager().getUser(playerName);
	return user == null ? 0D : user.getBalance();
    }


    @Override
    public boolean canAfford(Player player, double amount) {
        if (amount <= 0) return false;
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
        if (user != null && user.getBalance() >= amount) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canAfford(String playerName, double amount) {
        if (amount <= 0) return false;
	CMIUser user = CMI.getInstance().getPlayerManager().getUser(playerName);
	if (user != null && user.getBalance() >= amount) {
	    return true;
	}
	return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
        if (amount <= 0) return false;
	CMIUser user = CMI.getInstance().getPlayerManager().getUser(playerName);
	if (user != null)
	    user.deposit(amount);
	return true;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        if (amount <= 0) return false;
	if (!canAfford(playerName, amount)) {
	    return false;
	}
	CMIUser user = CMI.getInstance().getPlayerManager().getUser(playerName);
	if (user != null)
	    user.withdraw(amount);
	return true;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        if (amount <= 0) return false;
	if (!canAfford(playerFrom, amount)) {
	    return false;
	}
	if (subtract(playerFrom, amount)) {
	    if (!add(playerTo, amount)) {
		add(playerFrom, amount);
		return false;
	    }
	    return true;
	}
	return false;
    }

    @Override
    public String getName() {
	return "CMIEconomy";
    }

    @Override
    public String format(double amount) {
	return CMI.getInstance().getEconomyManager().format(amount);
    }
}

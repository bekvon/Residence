package com.bekvon.bukkit.residence.economy;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;

public class ResidenceBank {
    int storedMoney;
    ClaimedResidence res;

    public ResidenceBank(ClaimedResidence parent) {
	storedMoney = 0;
	res = parent;
    }

    public int getStoredMoney() {
	return storedMoney;
    }

    public void setStoredMoney(int amount) {
	storedMoney = amount;
    }

    public void add(int amount) {
	storedMoney = storedMoney + amount;
    }

    public boolean hasEnough(int amount) {
	if (storedMoney >= amount)
	    return true;
	return false;
    }

    public void subtract(int amount) {
	storedMoney = storedMoney - amount;
	if (storedMoney < 0)
	    storedMoney = 0;
    }

    public void withdraw(CommandSender sender, int amount, boolean resadmin) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    Residence.msg(sender, lm.Economy_MarketDisabled);
	}
	if (!resadmin && !res.getPermissions().playerHas(sender.getName(), "bank", false)) {
	    Residence.msg(sender, lm.Bank_NoAccess);
	    return;
	}
	if (!hasEnough(amount)) {
	    Residence.msg(sender, lm.Bank_NoMoney);
	    return;
	}
	if (sender instanceof Player && Residence.getEconomyManager().add(sender.getName(), amount) || !(sender instanceof Player)) {
	    this.subtract(amount);
	    Residence.msg(sender, lm.Bank_Withdraw, String.format("%d", amount));
	}
    }

    public void deposit(CommandSender sender, int amount, boolean resadmin) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    Residence.msg(sender, lm.Economy_MarketDisabled);
	}
	if (!resadmin && !res.getPermissions().playerHas(sender.getName(), "bank", false)) {
	    Residence.msg(sender, lm.Bank_NoAccess);
	    return;
	}
	if (sender instanceof Player && !Residence.getEconomyManager().canAfford(sender.getName(), amount)) {
	    Residence.msg(sender, lm.Economy_NotEnoughMoney);
	    return;
	}
	if (sender instanceof Player && Residence.getEconomyManager().subtract(sender.getName(), amount) || !(sender instanceof Player)) {
	    this.add(amount);
	    Residence.msg(sender, lm.Bank_Deposit, String.format("%d", amount));
	}
    }
}

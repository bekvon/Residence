package com.bekvon.bukkit.residence.economy;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
	}
	if (!resadmin && !res.getPermissions().playerHas(sender.getName(), "bank", false)) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoBankAccess"));
	    return;
	}
	if (!hasEnough(amount)) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("BankNoMoney"));
	    return;
	}
	if (sender instanceof Player && Residence.getEconomyManager().add(sender.getName(), amount) || !(sender instanceof Player)) {
	    this.subtract(amount);
	    sender.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("BankWithdraw", ChatColor.YELLOW + String.format("%d", amount) + ChatColor.GREEN));
	}
    }

    public void deposit(CommandSender sender, int amount, boolean resadmin) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
	}
	if (!resadmin && !res.getPermissions().playerHas(sender.getName(), "bank", false)) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoBankAccess"));
	    return;
	}
	if (sender instanceof Player && !Residence.getEconomyManager().canAfford(sender.getName(), amount)) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotEnoughMoney"));
	    return;
	}
	if (sender instanceof Player && Residence.getEconomyManager().subtract(sender.getName(), amount) || !(sender instanceof Player)) {
	    this.add(amount);
	    sender.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("BankDeposit", ChatColor.YELLOW + String.format("%d", amount) + ChatColor.GREEN));
	}
    }
}

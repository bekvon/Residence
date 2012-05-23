package com.bekvon.bukkit.residence.economy;
import org.bukkit.ChatColor;

public interface EconomyInterface
{

    public double getBalance(String playerName);
    public boolean canAfford(String playerName, double amount);
    public boolean add(String playerName, double amount);
    public boolean subtract(String playerName, double amount);
    public boolean transfer(String playerFrom, String playerTo, double amount);
    public String getName();
}

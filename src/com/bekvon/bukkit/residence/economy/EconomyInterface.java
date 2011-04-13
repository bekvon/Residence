package com.bekvon.bukkit.residence.economy;

public abstract class EconomyInterface 
{
    // Abstract interface (must be overriden)
    public abstract double getBalance(String playerName);
    public abstract boolean canAfford(String playerName, double amount);
    public abstract boolean add(String playerName, double amount);
    public abstract boolean subtract(String playerName, double amount);
    public abstract boolean transfer(String playerFrom, String playerTo, double amount);
    
    // Default name (should be overriden)
    public String getName()
    {
        return "Economy";
    }
}

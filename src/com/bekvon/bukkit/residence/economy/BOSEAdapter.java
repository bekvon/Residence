/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.economy;

import cosine.boseconomy.BOSEconomy;

/**
 *
 * @author Administrator
 */
public class BOSEAdapter extends EconomyInterface {

    BOSEconomy plugin;

    public BOSEAdapter(BOSEconomy p) {
        plugin = p;
    }

    @Override
    public double getBalance(String playerName) {
        return plugin.getPlayerMoney(playerName);
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        int balance = plugin.getPlayerMoney(playerName);
        if (balance <= amount) {
            return true;
        }
        return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
        return plugin.setPlayerMoney(playerName, plugin.getPlayerMoney(playerName) + ((int) amount), false);
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        if (canAfford(playerName, amount)) {
            return plugin.setPlayerMoney(playerName, plugin.getPlayerMoney(playerName) - ((int) amount), false);
        }
        return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        if (canAfford(playerFrom, amount)) {
            if (!plugin.setPlayerMoney(playerFrom, plugin.getPlayerMoney(playerFrom) - ((int) amount), false)) {
                return false;
            }
            if (!plugin.setPlayerMoney(playerTo, plugin.getPlayerMoney(playerTo) + ((int) amount), false)) {
                plugin.setPlayerMoney(playerFrom, plugin.getPlayerMoney(playerFrom) + ((int) amount), false);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "BOSEconomy";
    }
}
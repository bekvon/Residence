/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.vaultinterface;

import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.permissions.PermissionsInterface;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * 
 * @author Administrator
 */
public class ResidenceVaultAdapter implements EconomyInterface, PermissionsInterface {

    public static Permission permissions = null;
    public static Economy economy = null;
    public static Chat chat = null;

    public boolean permissionsOK() {
        if (permissions != null && !permissions.getName().equalsIgnoreCase("SuperPerms")) {
            return true;
        }
        return false;
    }

    public boolean economyOK() {
        return economy != null;
    }

    public boolean chatOK() {
        return chat != null;
    }

    public ResidenceVaultAdapter(Server s) {
        this.setupPermissions(s);
        this.setupEconomy(s);
        this.setupChat(s);
    }

    private boolean setupPermissions(Server s) {
        RegisteredServiceProvider<Permission> permissionProvider = s.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permissions = permissionProvider.getProvider();
        }
        return (permissions != null);
    }

    private boolean setupChat(Server s) {
        RegisteredServiceProvider<Chat> chatProvider = s.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }

    private boolean setupEconomy(Server s) {
        RegisteredServiceProvider<Economy> economyProvider = s.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public String getPlayerGroup(Player player) {
        String group = permissions.getPrimaryGroup(player).toLowerCase();
        if (group == null) {
            return group;
        } else {
            return group.toLowerCase();
        }
    }

    public String getPlayerGroup(String player, String world) {
        String group = permissions.getPrimaryGroup(world, player);
        if (group == null) {
            return group;
        } else {
            return group.toLowerCase();
        }
    }

    @Override
    public double getBalance(String playerName) {
        return economy.getBalance(playerName);
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        return economy.has(playerName, amount);
    }

    @Override
    public boolean add(String playerName, double amount) {
        return economy.depositPlayer(playerName, amount).transactionSuccess();
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        if (economy.withdrawPlayer(playerFrom, amount).transactionSuccess()) {
            if (economy.depositPlayer(playerTo, amount).transactionSuccess()) {
                return true;
            } else {
                economy.depositPlayer(playerFrom, amount);
                return false;
            }
        } else {
            return false;
        }
    }

    public String getEconomyName() {
        if (economy != null) {
            return economy.getName();
        }
        return "";
    }

    public String getPermissionsName() {
        if (permissions != null) {
            return permissions.getName();
        }
        return "";
    }

    public String getChatName() {
        if (chat != null) {
            return chat.getName();
        }
        return "";
    }

    public String getName() {
        return "Vault";
    }

}
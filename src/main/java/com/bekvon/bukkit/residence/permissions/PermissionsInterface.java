/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public interface PermissionsInterface {
    public String getPlayerGroup(Player player);
    public String getPlayerGroup(String player, String world);
}

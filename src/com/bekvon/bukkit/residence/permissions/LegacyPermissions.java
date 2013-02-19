/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.nijiko.permissions.PermissionHandler;
import org.bukkit.entity.Player;

/**
 * 
 * @author Administrator
 */
public class LegacyPermissions implements PermissionsInterface {

    PermissionHandler authority;

    public LegacyPermissions(PermissionHandler perms) {
        authority = perms;
    }

    public String getPlayerGroup(Player player) {
        return this.getPlayerGroup(player.getName(), player.getWorld().getName());
    }

    public String getPlayerGroup(String player, String world) {
        String group = authority.getPrimaryGroup(world, player);
        if (group != null)
            return group.toLowerCase();
        return null;
    }

}

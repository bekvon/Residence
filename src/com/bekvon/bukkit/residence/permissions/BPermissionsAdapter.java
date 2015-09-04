/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.entity.Player;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

/**
 * 
 * @author Administrator
 */
@SuppressWarnings("deprecation")
public class BPermissionsAdapter implements PermissionsInterface {

    public BPermissionsAdapter() {
    }

    public String getPlayerGroup(Player player) {
        return this.getPlayerGroup(player.getName(), player.getWorld().getName());
    }

    public String getPlayerGroup(String player, String world) {
	String[] groups = ApiLayer.getGroups(world, CalculableType.USER, player);
        PermissionManager pmanager = Residence.getPermissionManager();
        for (String group : groups) {
            if (pmanager.hasGroup(group)) {
                return group.toLowerCase();
            }
        }
        if (groups.length > 0) {
            return groups[0].toLowerCase();
        }
        return null;
    }
}

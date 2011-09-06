/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import java.util.List;
import org.bukkit.entity.Player;
import de.bananaco.permissions.Permissions;

/**
 *
 * @author Administrator
 */
public class BPermissionsAdapter implements PermissionsInterface {

    Permissions bperms;

    public BPermissionsAdapter(Permissions p)
    {
        bperms = p;
    }

    public String getPlayerGroup(Player player) {
        return this.getPlayerGroup(player.getName(), player.getWorld().getName());
    }

    public String getPlayerGroup(String player, String world) {
        List<String> groups = Permissions.getWorldPermissionsManager().getPermissionSet(world).getGroups(player);
        PermissionManager pmanager = Residence.getPermissionManager();
        for(String group : groups)
        {
            if(pmanager.hasGroup(group))
                return group.toLowerCase();
        }
        if(groups.size()>0)
            return groups.get(0).toLowerCase();
        return null;
    }

    public boolean hasPermission(Player player, String permission) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author Administrator
 */
public class PermissionManager {
    protected static PermissionHandler authority;
    protected static String defaultGroup;
    protected Map<String,PermissionGroup> groups;
    protected Map<String,String> playersGroup;

    public PermissionManager(Configuration config)
    {
        try
        {
        groups = Collections.synchronizedMap(new HashMap<String,PermissionGroup>());
        playersGroup = Collections.synchronizedMap(new HashMap<String,String>());
        boolean enable = config.getBoolean("Global.EnablePermissions", true);
        this.readConfig(config);
        if(enable)
            this.checkPermissions();
        }
        catch(Exception ex)
        {
            Logger.getLogger(PermissionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PermissionGroup getGroup(Player player)
    {
        return groups.get(this.getGroupNameByPlayer(player));
    }

    public PermissionGroup getGroup(String player, String world)
    {
        return groups.get(this.getGroupNameByPlayer(player, world));
    }

    public PermissionGroup getGroupByName(String group)
    {
        group = group.toLowerCase();
        if(!groups.containsKey(group))
            return groups.get(defaultGroup);
        return groups.get(group);
    }

    public String getGroupNameByPlayer(Player player)
    {
        return this.getGroupNameByPlayer(player.getName(), player.getWorld().getName());
    }

    public String getGroupNameByPlayer(String player, String world) {
        if(playersGroup.containsKey(player))
        {
            String group = playersGroup.get(player);
            if(group == null || !groups.containsKey(group))
                return defaultGroup;
            return group;
        }
        if (authority == null) {
            return defaultGroup;
        } else {
            String group = authority.getGroup(world, player).toLowerCase();
            if (group == null || !groups.containsKey(group)) {
                return defaultGroup;
            } else {
                return group;
            }
        }
    }

    public boolean hasAuthority(Player player, String permission, boolean def) {
        if (authority == null) {
            return def;
        } else {
            return authority.has(player, permission);
        }
    }

    public boolean isResidenceAdmin(Player player)
    {
        return this.hasAuthority(player, "residence.admin", player.isOp());
    }

    private void checkPermissions() {
        Server server = Residence.getServ();
        Plugin p = server.getPluginManager().getPlugin("Permissions");
        if (p != null) {
            authority = ((Permissions) p).getHandler();
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Permissions Plugin!");
        } else {
            authority = null;
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Permissions Plugin NOT Found!");
        }
    }

    private void readConfig(Configuration config)
    {
        defaultGroup = config.getString("DefaultGroup","default").toLowerCase();
        Map<String, ConfigurationNode> nodes = config.getNodes("Groups");
        if(nodes!=null)
        {
            Set<Entry<String, ConfigurationNode>> entrys = nodes.entrySet();
            for(Entry<String, ConfigurationNode> entry : entrys)
            {
                String key = entry.getKey().toLowerCase();
                groups.put(key, new PermissionGroup(key,entry.getValue()));
            }
        }
        if(!groups.containsKey(defaultGroup))
        {
            groups.put(defaultGroup, new PermissionGroup(defaultGroup));
        }
        List<String> keys = config.getKeys("GroupAssigments");
        if(keys!=null)
        {
            for(String key : keys)
            {
                playersGroup.put(key, config.getString("GroupAssignments."+key, defaultGroup).toLowerCase());
            }
        }
    }

    public boolean hasGroup(String group)
    {
        group = group.toLowerCase();
        return groups.containsKey(group); 
    }
}

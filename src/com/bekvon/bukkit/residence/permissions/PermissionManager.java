/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
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
    protected static PermissionsPlugin newperms;
    protected Map<String,PermissionGroup> groups;
    protected Map<String,String> playersGroup;
    protected FlagPermissions globalFlagPerms;

    public PermissionManager(Configuration config)
    {
        try
        {
        groups = Collections.synchronizedMap(new HashMap<String,PermissionGroup>());
        playersGroup = Collections.synchronizedMap(new HashMap<String,String>());
        globalFlagPerms = new FlagPermissions();
        Plugin p = Residence.getServ().getPluginManager().getPlugin("PermissionsBukkit");
        if(p!=null && p instanceof PermissionsPlugin)
        {
            newperms = (PermissionsPlugin) p;
        }
        boolean enable = config.getBoolean("Global.EnablePermissions", true);
        this.readConfig(config);
        if(enable)
            this.checkPermissions();
        else
            authority = null;
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
            return groups.get(Residence.getConfig().getDefaultGroup());
        return groups.get(group);
    }

    public String getGroupNameByPlayer(Player player)
    {
        return this.getGroupNameByPlayer(player.getName(), player.getWorld().getName());
    }

    public String getGroupNameByPlayer(String player, String world) {
        String defaultGroup = Residence.getConfig().getDefaultGroup();
        if(playersGroup.containsKey(player))
        {
            String group = playersGroup.get(player);
            if(group!=null)
            {
                group = group.toLowerCase();
                if(group != null && groups.containsKey(group))
                    return group;
            }
        }
        if (authority == null && newperms == null) {
            return defaultGroup;
        } else {
            String group = null;
            if(authority != null && Residence.getConfig().useLegacyPermissions())
                group = authority.getGroup(world, player);
            else if(newperms!=null)
            {
                List<Group> newgroups = newperms.getGroups(player);
                for(Group newgroup : newgroups)
                {
                    group = newgroup.getName().toLowerCase();
                    break;
                }
            }
            else
                group = authority.getPrimaryGroup(world, player);
            if(group!=null)
                group = group.toLowerCase();
            if (group == null || !groups.containsKey(group)) {
                return defaultGroup;
            } else {
                return group;
            }
        }
    }

    public boolean hasAuthority(Player player, String permission, boolean def) {
        if(player.hasPermission(permission))
            return true;
        if (authority == null) {
            return def;
        } else {
            return authority.has(player, permission);
        }
    }

    public boolean isResidenceAdmin(Player player)
    {
        return (this.hasAuthority(player, "residence.admin", false) || (player.isOp() && Residence.getConfig().getOpsAreAdmins()));
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
        String defaultGroup = Residence.getConfig().getDefaultGroup();
        globalFlagPerms = FlagPermissions.parseFromConfigNode("FlagPermission", config.getNode("Global"));
        Map<String, ConfigurationNode> nodes = config.getNodes("Groups");
        if(nodes!=null)
        {
            Set<Entry<String, ConfigurationNode>> entrys = nodes.entrySet();
            for(Entry<String, ConfigurationNode> entry : entrys)
            {
                String key = entry.getKey().toLowerCase();
                try
                {
                    groups.put(key, new PermissionGroup(key,entry.getValue(),globalFlagPerms));
                }
                catch(Exception ex)
                {
                    System.out.println("[Residence] Error parsing group from config:" + key + " Exception:" + ex);
                }
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

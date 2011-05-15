/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.PermissionList.FlagState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author Administrator
 */
public class WorldFlagManager {
    protected Map<String, Map<String,PermissionList>> groupperms;
    protected Map<String,PermissionList> worldperms;
    protected PermissionList globaldefaults;

    public WorldFlagManager()
    {
        globaldefaults = new PermissionList();
        worldperms = new HashMap<String,PermissionList>();
        groupperms = new HashMap<String,Map<String,PermissionList>>();
    }

    public WorldFlagManager(Configuration config)
    {
        this();
        this.parsePerms(config);
    }

    public PermissionList getPerms(Player player)
    {
        Map<String, PermissionList> get = groupperms.get(Residence.getPermissionManager().getGroupNameByPlayer(player).toLowerCase());
        if(get==null)
        {
            get = worldperms;
        }
        PermissionList list = get.get(player.getWorld().getName().toLowerCase());
        if(list==null)
        {
            list = get.get("global");
            if(list==null)
                return globaldefaults;
        }
        return list;
    }

    public PermissionList getPerms(String world)
    {
        world = world.toLowerCase();
        PermissionList list = worldperms.get(world);
        if(list==null)
            return globaldefaults;
        return list;
    }

    public void parsePerms(Configuration config) {
        try {
            
            List<String> keys = config.getKeys("Global.Flags");
            for(String key : keys)
            {
                if(key.equalsIgnoreCase("Global"))
                    globaldefaults = PermissionList.parseFromConfigNode(key, config.getNode("Global.Flags"));
                else
                    worldperms.put(key.toLowerCase(), PermissionList.parseFromConfigNode(key,config.getNode("Global.Flags")));
            }
            for(Entry<String, PermissionList> entry : worldperms.entrySet())
            {
                entry.getValue().setParent(globaldefaults);
            }
            keys = config.getKeys("Groups");
            if (keys != null) {
                for (String key : keys) {
                    List<String> worldkeys = config.getKeys("Groups." + key + ".Flags.World");
                    if (worldkeys != null) {
                        Map<String, PermissionList> perms = new HashMap<String, PermissionList>();
                        for (String wkey : worldkeys) {
                            PermissionList list = PermissionList.parseFromConfigNode(wkey, config.getNode("Groups." + key + ".Flags.World"));
                            perms.put(wkey.toLowerCase(), list);
                        }
                        for (Entry<String, PermissionList> entry : perms.entrySet()) {
                            String wkey = entry.getKey();
                            PermissionList list = entry.getValue();
                            if (wkey.equals("global")) {
                                list.setParent(worldperms.get(wkey));
                                if(list.getParent()==null)
                                    list.setParent(globaldefaults);
                            } else {
                                list.setParent(perms.get("global"));
                                if (list.getParent() == null) {
                                    list.setParent(worldperms.get(wkey));
                                }
                                if(list.getParent()==null)
                                {
                                    list.setParent(globaldefaults);
                                }
                            }
                        }
                        groupperms.put(key.toLowerCase(), perms);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(WorldFlagManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

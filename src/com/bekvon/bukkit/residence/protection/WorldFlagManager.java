/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class WorldFlagManager {
    protected Map<String, Map<String,FlagPermissions>> groupperms;
    protected Map<String,FlagPermissions> worldperms;
    protected FlagPermissions globaldefaults;

    public WorldFlagManager()
    {
        globaldefaults = new FlagPermissions();
        worldperms = new HashMap<String,FlagPermissions>();
        groupperms = new HashMap<String,Map<String,FlagPermissions>>();
    }

    public WorldFlagManager(Configuration config)
    {
        this();
        this.parsePerms(config);
    }

    public FlagPermissions getPerms(Player player)
    {
        Map<String, FlagPermissions> get = groupperms.get(Residence.getPermissionManager().getGroupNameByPlayer(player).toLowerCase());
        if(get==null)
        {
            get = worldperms;
        }
        FlagPermissions list = get.get(player.getWorld().getName().toLowerCase());
        if(list==null)
        {
            list = get.get("global");
            if(list==null)
                return globaldefaults;
        }
        return list;
    }

    public FlagPermissions getPerms(String world)
    {
        world = world.toLowerCase();
        FlagPermissions list = worldperms.get(world);
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
                    globaldefaults = FlagPermissions.parseFromConfigNode(key, config.getNode("Global.Flags"));
                else
                    worldperms.put(key.toLowerCase(), FlagPermissions.parseFromConfigNode(key,config.getNode("Global.Flags")));
            }
            for(Entry<String, FlagPermissions> entry : worldperms.entrySet())
            {
                entry.getValue().setParent(globaldefaults);
            }
            keys = config.getKeys("Groups");
            if (keys != null) {
                for (String key : keys) {
                    List<String> worldkeys = config.getKeys("Groups." + key + ".Flags.World");
                    if (worldkeys != null) {
                        Map<String, FlagPermissions> perms = new HashMap<String, FlagPermissions>();
                        for (String wkey : worldkeys) {
                            FlagPermissions list = FlagPermissions.parseFromConfigNode(wkey, config.getNode("Groups." + key + ".Flags.World"));
                            perms.put(wkey.toLowerCase(), list);
                        }
                        for (Entry<String, FlagPermissions> entry : perms.entrySet()) {
                            String wkey = entry.getKey();
                            FlagPermissions list = entry.getValue();
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

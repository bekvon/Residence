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
        Map<String, FlagPermissions> groupworldperms = groupperms.get(Residence.getPermissionManager().getGroupNameByPlayer(player).toLowerCase());
        String wname = player.getWorld().getName().toLowerCase();
        if(groupworldperms==null)
        {
            return this.getPerms(wname);
        }
        FlagPermissions list = groupworldperms.get(wname);
        if(list==null)
        {
            list = groupworldperms.get("global."+wname);
            if(list==null)
            {
                list = groupworldperms.get("global");
            }
            if(list==null)
            {
                return this.getPerms(wname);
            }
        }
        return list;
    }

    public FlagPermissions getPerms(String world)
    {
        world = world.toLowerCase();
        FlagPermissions list = worldperms.get(world);
        if (list == null) {
            if (globaldefaults == null)
                return new FlagPermissions();
            else
                return globaldefaults;
        }
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
                            if(wkey.equalsIgnoreCase("global"))
                            {
                                list.setParent(globaldefaults);
                                perms.put(wkey.toLowerCase(), list);
                                for(Entry<String, FlagPermissions> worldperm : worldperms.entrySet())
                                {
                                    list = FlagPermissions.parseFromConfigNode(wkey, config.getNode("Groups." + key + ".Flags.World"));
                                    list.setParent(worldperm.getValue());
                                    perms.put("global."+worldperm.getKey().toLowerCase(), list);
                                }
                            }
                            else
                            {
                                perms.put(wkey.toLowerCase(), list);
                            }
                        }
                        for (Entry<String, FlagPermissions> entry : perms.entrySet()) {
                            String wkey = entry.getKey();
                            FlagPermissions list = entry.getValue();
                            if (!wkey.startsWith("global.")) {
                                list.setParent(perms.get("global."+wkey));
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

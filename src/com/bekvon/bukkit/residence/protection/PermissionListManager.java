/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.PermissionList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class PermissionListManager {

    private Map<String,Map<String,PermissionList>> lists;

    public PermissionListManager()
    {
        lists = Collections.synchronizedMap(new HashMap<String,Map<String,PermissionList>>());
    }
    
    public PermissionList getList(String player, String listname)
    {
        Map<String, PermissionList> get = lists.get(player);
        if(get==null)
        {
            return null;
        }
        return get.get(listname);
    }
    
    public void makeList(Player player, String listname)
    {
        Map<String, PermissionList> get = lists.get(player.getName());
        if(get==null)
        {
            get=new HashMap<String,PermissionList>();
            lists.put(player.getName(), get);
        }
        PermissionList perms = get.get(listname);
        if(perms == null)
        {
            perms = new PermissionList();
            get.put(listname, perms);
            player.sendMessage("§aCreated list " + listname + ".");
        }
        else
        {
            player.sendMessage("§cList already exists.");
        }
    }

    public void removeList(Player player, String listname)
    {
        Map<String, PermissionList> get = lists.get(player.getName());
        if(get==null)
        {
            player.sendMessage("§cInvalid list.");
            return;
        }
        PermissionList list = get.get(listname);
        if(list==null)
        {
            player.sendMessage("§cInvalid list.");
            return;
        }
        get.remove(listname);
        player.sendMessage("§cList removed...");
    }
    
    public void applyListToResidence(Player player, String listname, String areaname)
    {
        PermissionList list = this.getList(player.getName(), listname);
        if(list == null)
        {
             player.sendMessage("§cInvalid list...");
             return;
        }
        ClaimedResidence res = Residence.getResidenceManger().getByName(areaname);
        if(res == null)
        {
            player.sendMessage("§cInvalid Residence...");
            return;
        }
        res.getPermissions().applyTemplate(player, list);
    }

    public void printList(Player player, String listname)
    {
        PermissionList list = this.getList(player.getName(), listname);
        if(list==null)
        {
            player.sendMessage("Invalid list...");
            return;
        }
        player.sendMessage("§d------Permission Template------");
        player.sendMessage("§eName: §a" + listname);
        list.printFlags(player);
    }

    public Map<String,Object> save()
    {
        Map root = new LinkedHashMap<String,Object>();
        for(Entry<String, Map<String, PermissionList>> players : lists.entrySet())
        {
            Map saveMap = new LinkedHashMap<String,Object>();
            Map<String, PermissionList> map = players.getValue();
            for(Entry<String, PermissionList> list : map.entrySet())
            {
                saveMap.put(list.getKey(), list.getValue().save());
            }
            root.put(players.getKey(), saveMap);
        }
        return root;
    }
    public static PermissionListManager load(Map<String, Object> root) {
        
        PermissionListManager p = new PermissionListManager();
        for (Entry<String, Object> players : root.entrySet()) {
            try {
                Map<String, Object> value = (Map<String, Object>) players.getValue();
                Map<String, PermissionList> loadedMap = Collections.synchronizedMap(new HashMap<String, PermissionList>());
                for (Entry<String, Object> list : value.entrySet()) {
                    loadedMap.put(list.getKey(), PermissionList.load((Map<String, Object>) list.getValue()));
                }
                p.lists.put(players.getKey(), loadedMap);
            } catch (Exception ex) {
                System.out.println("[Residence] - Failed to load permission lists for player: " + players.getKey());
            }
        }
        return p;
    }

    public void printLists(Player player)
    {
        StringBuilder sbuild = new StringBuilder();
        Map<String, PermissionList> get = lists.get(player.getName());
        if(get==null)
        {
            player.sendMessage("§cYou don't have any predefined lists yet.");
            return;
        }
        sbuild.append("§ePermission Lists:§3 ");
        for( Entry<String, PermissionList> thislist : get.entrySet())
        {
            sbuild.append(thislist.getKey()).append(" ");
        }
        player.sendMessage(sbuild.toString());
    }
}


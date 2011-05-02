/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class PermissionList {

    protected Map<String, Map<String, Boolean>> playerFlags;
    protected Map<String, Map<String, Boolean>> groupFlags;
    protected Map<String, Boolean> cuboidFlags;
    protected PermissionList parent;

    public PermissionList()
    {
        cuboidFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
        playerFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
        groupFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
    }

    public void setPlayer(String player, String flag, FlagState state) {
        player = player.toLowerCase();
        if (!playerFlags.containsKey(player)) {
            playerFlags.put(player, Collections.synchronizedMap(new HashMap<String, Boolean>()));
        }
        Map<String, Boolean> map = playerFlags.get(player);
        if (state == FlagState.FALSE) {
            map.put(flag, false);
        } else if (state == FlagState.TRUE) {
            map.put(flag, true);
        } else if (state == FlagState.NEITHER) {
            if (map.containsKey(flag)) {
                map.remove(flag);
            }
        }
        if (map.isEmpty()) {
            playerFlags.remove(player);
        }
    }

    public void setGroup(String group, String flag, FlagState state) {
        group = group.toLowerCase();
        if (!groupFlags.containsKey(group)) {
            groupFlags.put(group, Collections.synchronizedMap(new HashMap<String, Boolean>()));
        }
        Map<String, Boolean> map = groupFlags.get(group);
        if (state == FlagState.FALSE) {
            map.put(flag, false);
        } else if (state == FlagState.TRUE) {
            map.put(flag, true);
        } else if (state == FlagState.NEITHER) {
            if (map.containsKey(flag)) {
                map.remove(flag);
            }
        }
        if (map.isEmpty()) {
            groupFlags.remove(group);
        }
    }

    public void set(String flag, FlagState state) {
        if (state == FlagState.FALSE) {
            cuboidFlags.put(flag, false);
        } else if (state == FlagState.TRUE) {
            cuboidFlags.put(flag, true);
        } else if (state == FlagState.NEITHER) {
            if (cuboidFlags.containsKey(flag)) {
                cuboidFlags.remove(flag);
            }
        }
    }

    public static enum FlagState {

        TRUE, FALSE, NEITHER, INVALID
    }

    public static FlagState stringToFlagState(String flagstate) {
        if (flagstate.equalsIgnoreCase("true") || flagstate.equalsIgnoreCase("t")) {
            return FlagState.TRUE;
        } else if (flagstate.equalsIgnoreCase("false") || flagstate.equalsIgnoreCase("f")) {
            return FlagState.FALSE;
        } else if (flagstate.equalsIgnoreCase("remove") || flagstate.equalsIgnoreCase("r")) {
            return FlagState.NEITHER;
        } else {
            return FlagState.INVALID;
        }
    }

    public boolean has(String flag, boolean def)
    {
        if(cuboidFlags.containsKey(flag))
            return cuboidFlags.get(flag);
        else
        {
            if(parent!=null)
                return parent.has(flag, def);
            return def;
        }
    }

    public boolean checkValidFlag(String flag, boolean globalflag) {
        if (flag.equals("use") || flag.equals("move") || flag.equals("build") || flag.equals("tp") || flag.equals("ignite") || flag.equals("container") || flag.equals("subzone")) {
            return true;
        }
        if (globalflag) {
            if (flag.equals("pvp") || flag.equals("damage") || flag.equals("monsters") || flag.equals("firespread") || flag.equals("tnt") || flag.equals("creeper") || flag.equals("flow")) {
                return true;
            }
        } else {
            if (flag.equals("admin")) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("PlayerFlags", playerFlags);
        root.put("GroupFlags", groupFlags);
        root.put("AreaFlags", cuboidFlags);
        return root;
    }

    public static PermissionList load(Map<String, Object> root) throws Exception {
        PermissionList newperms = new PermissionList();
        newperms.playerFlags = (Map) root.get("PlayerFlags");
        newperms.groupFlags = (Map) root.get("GroupFlags");
        newperms.cuboidFlags = (Map) root.get("AreaFlags");
        return newperms;
    }

    public String listFlags()
    {
        StringBuilder sbuild = new StringBuilder();
        Set<Entry<String, Boolean>> set = cuboidFlags.entrySet();
        synchronized(cuboidFlags)
        {
            Iterator<Entry<String, Boolean>> it = set.iterator();
            while(it.hasNext())
            {
                Entry<String, Boolean> next = it.next();
                if(next.getValue())
                {
                    sbuild.append("+").append(next.getKey());
                    if(it.hasNext())
                        sbuild.append(" ");
                }
                else
                {
                    sbuild.append("-").append(next.getKey());
                        if(it.hasNext())
                            sbuild.append(" ");
                }
            }
        }
        if(sbuild.length() == 0)
            sbuild.append("none");
        return sbuild.toString();
    }

    public String listPlayerFlags(String player)
    {
        player = player.toLowerCase();
        if(playerFlags.containsKey(player))
        {
            StringBuilder sbuild = new StringBuilder();
            Map<String, Boolean> get = playerFlags.get(player);
            Set<Entry<String, Boolean>> set = get.entrySet();
            synchronized(get)
            {
                Iterator<Entry<String, Boolean>> it = set.iterator();
                while(it.hasNext())
                {
                    Entry<String, Boolean> next = it.next();
                    if(next.getValue())
                    {
                        sbuild.append("+").append(next.getKey());
                        if(it.hasNext())
                            sbuild.append(" ");
                    }
                    else
                    {
                        sbuild.append("-").append(next.getKey());
                        if(it.hasNext())
                            sbuild.append(" ");
                    }
                }
            }
            if(sbuild.length()==0)
            {
                playerFlags.remove(player);
                sbuild.append("none");
            }
            return sbuild.toString();
        }
        else
        {
            return "none";
        }
    }

    public String listOtherPlayersFlags(String player)
    {
        player = player.toLowerCase();
        StringBuilder sbuild = new StringBuilder();
        Set<String> set = playerFlags.keySet();
        synchronized(playerFlags)
        {
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String next = it.next();
                if(!next.equals(player))
                {
                    String perms = listPlayerFlags(next);
                    if(!perms.equals("none"))
                    {
                        sbuild.append(next).append("[§3").append(perms).append("§c] ");
                    }
                }
            }
        }
        return sbuild.toString();
    }

    public String listGroupFlags()
    {
        StringBuilder sbuild = new StringBuilder();
        Set<String> set = groupFlags.keySet();
        synchronized(groupFlags)
        {
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String next = it.next();
                String perms = listGroupFlags(next);
                if(!perms.equals("none"))
                {
                    sbuild.append(next).append("[§3").append(perms).append("§c] ");
                }
            }
        }
        return sbuild.toString();
    }

    public String listGroupFlags(String group)
    {
        group = group.toLowerCase();
        if(groupFlags.containsKey(group))
        {
            StringBuilder sbuild = new StringBuilder();
            Map<String, Boolean> get = groupFlags.get(group);
            Set<Entry<String, Boolean>> set = get.entrySet();
            synchronized(get)
            {
                Iterator<Entry<String, Boolean>> it = set.iterator();
                while(it.hasNext())
                {
                    Entry<String, Boolean> next = it.next();
                    if(next.getValue())
                    {
                        sbuild.append("+").append(next.getKey());
                        if(it.hasNext())
                            sbuild.append(" ");
                    }
                    else
                    {
                        sbuild.append("-").append(next.getKey());
                        if(it.hasNext())
                            sbuild.append(" ");
                    }
                }
            }
            if(sbuild.length()==0)
            {
                groupFlags.remove(group);
                sbuild.append("none");
            }
            return sbuild.toString();
        }
        else
        {
            return "none";
        }
    }

    public void clearFlags()
    {
        groupFlags.clear();
        playerFlags.clear();
        cuboidFlags.clear();
    }

    public void printFlags(Player player)
    {
        player.sendMessage("§eFlags:§9 " + listFlags());
        player.sendMessage("§eYour Flags: §a" + listPlayerFlags(player.getName()));
        player.sendMessage("§eGroup Flags:§c " + listGroupFlags());
        player.sendMessage("§eOthers Flags:§c " + listOtherPlayersFlags(player.getName()));
    }

    public void setParent(PermissionList p)
    {
        parent = p;
    }

    public PermissionList getParent()
    {
        return parent;
    }
}

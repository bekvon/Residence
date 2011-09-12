/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author Administrator
 */
public class FlagPermissions {

    protected static ArrayList<String> validFlags = new ArrayList<String>();
    protected static ArrayList<String> validPlayerFlags = new ArrayList<String>();
    protected static ArrayList<String> validAreaFlags = new ArrayList<String>();

    public static void addFlag(String flag)
    {
        if(!validFlags.contains(flag))
            validFlags.add(flag);
    }
    public static void addPlayerOrGroupOnlyFlag(String flag)
    {
        if(!validPlayerFlags.contains(flag))
            validPlayerFlags.add(flag);
    }
    public static void addResidenceOnlyFlag(String flag)
    {
        if(!validAreaFlags.contains(flag))
            validAreaFlags.add(flag);
    }
    public static void initValidFlags()
    {
        validAreaFlags.clear();
        validPlayerFlags.clear();
        validFlags.clear();
        addFlag("use");
        addFlag("move");
        addFlag("build");
        addFlag("tp");
        addFlag("ignite");
        addFlag("container");
        addFlag("subzone");
        addFlag("destroy");
        addFlag("place");
        addFlag("bucket");
        addFlag("bank");
        addResidenceOnlyFlag("pvp");
        addResidenceOnlyFlag("damage");
        addResidenceOnlyFlag("monsters");
        addResidenceOnlyFlag("firespread");
        addResidenceOnlyFlag("tnt");
        addResidenceOnlyFlag("creeper");
        addResidenceOnlyFlag("flow");
        addResidenceOnlyFlag("healing");
        addResidenceOnlyFlag("animals");
        addResidenceOnlyFlag("lavaflow");
        addResidenceOnlyFlag("waterflow");
        addResidenceOnlyFlag("physics");
        addResidenceOnlyFlag("piston");
        addPlayerOrGroupOnlyFlag("admin");
    }

    protected Map<String, Map<String, Boolean>> playerFlags;
    protected Map<String, Map<String, Boolean>> groupFlags;
    protected Map<String, Boolean> cuboidFlags;
    protected FlagPermissions parent;

    public static FlagPermissions parseFromConfigNode(String name, ConfigurationNode node)
    {
        FlagPermissions list = new FlagPermissions();
        List<String> keys = node.getKeys(name);
        if(keys!=null)
        {
            for(String key : keys)
            {
                boolean state = node.getBoolean(name + "." + key, false);
                key = key.toLowerCase();
                if(state)
                    list.setFlag(key, FlagState.TRUE);
                else
                    list.setFlag(key, FlagState.FALSE);
            }
        }
        return list;
    }

    public FlagPermissions()
    {
        cuboidFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
        playerFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
        groupFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
    }

    public boolean setPlayerFlag(String player, String flag, FlagState state) {
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
        return true;
    }

    public void removeAllPlayerFlags(String player)
    {
        playerFlags.remove(player);
    }

    public void removeAllGroupFlags(String group)
    {
        groupFlags.remove(group);
    }

    public boolean setGroupFlag(String group, String flag, FlagState state) {
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
        return true;
    }

    public boolean setFlag(String flag, FlagState state) {
        if (state == FlagState.FALSE) {
            cuboidFlags.put(flag, false);
        } else if (state == FlagState.TRUE) {
            cuboidFlags.put(flag, true);
        } else if (state == FlagState.NEITHER) {
            if (cuboidFlags.containsKey(flag)) {
                cuboidFlags.remove(flag);
            }
        }
        return true;
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

    public boolean playerHas(String player, String world, String flag, boolean def)
    {
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player, world);
        return this.playerCheck(player, flag, this.groupCheck(group, flag, this.has(flag, def)));
    }

    public boolean groupHas(String group, String flag, boolean def)
    {
        return this.groupCheck(group, flag, this.has(flag, def));
    }

    private boolean playerCheck(String player, String flag, boolean def)
    {
        player = player.toLowerCase();
        if(playerFlags.containsKey(player))
        {
            Map<String, Boolean> pmap = playerFlags.get(player);
            if(pmap.containsKey(flag))
                return pmap.get(flag);
        }
        
        if(parent!=null)
            return parent.playerCheck(player, flag, def);
        return def;
    }

    private boolean groupCheck(String group, String flag, boolean def)
    {
        if(groupFlags.containsKey(group))
        {
            Map<String, Boolean> gmap = groupFlags.get(group);
            if(gmap.containsKey(flag))
                return gmap.get(flag);
        }
        if(parent!=null)
            return parent.groupCheck(group, flag, def);
        return def;
    }

    public boolean has(String flag, boolean def)
    {
        if(cuboidFlags.containsKey(flag))
            return cuboidFlags.get(flag);
        if(parent!=null)
            return parent.has(flag, def);
        return def;
    }

    public boolean isPlayerSet(String player, String flag)
    {
        player = player.toLowerCase();
        Map<String, Boolean> flags = playerFlags.get(player);
        if(flags==null)
            return false;
        return flags.containsKey(flag);
    }

    public boolean inheritanceIsPlayerSet(String player, String flag)
    {
        player = player.toLowerCase();
        Map<String, Boolean> flags = playerFlags.get(player);
        if(flags==null)
            return parent==null ? false : parent.inheritanceIsPlayerSet(player, flag);
        return flags.containsKey(flag) ? true : parent==null ? false : parent.inheritanceIsPlayerSet(player, flag);
    }

    public boolean isGroupSet(String group, String flag)
    {
        group = group.toLowerCase();
        Map<String, Boolean> flags = groupFlags.get(group);
        if(flags==null)
            return false;
        return flags.containsKey(flag);
    }

    public boolean inheritanceIsGroupSet(String group, String flag)
    {
        group = group.toLowerCase();
        Map<String, Boolean> flags = groupFlags.get(group);
        if(flags==null)
            return parent==null ? false : parent.inheritanceIsGroupSet(group, flag);
        return flags.containsKey(flag) ? true : parent==null ? false : parent.inheritanceIsGroupSet(group, flag);
    }

    public boolean isSet(String flag)
    {
        return cuboidFlags.containsKey(flag);
    }

    public boolean inheritanceIsSet(String flag)
    {
        return cuboidFlags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsSet(flag);
    }

    public boolean checkValidFlag(String flag, boolean globalflag) {
        if (validFlags.contains(flag)) {
            return true;
        }
        if (globalflag) {
            if (validAreaFlags.contains(flag)) {
                return true;
            }
        } else {
            if (validPlayerFlags.contains(flag)) {
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

    public static FlagPermissions load(Map<String, Object> root) throws Exception {
        FlagPermissions newperms = new FlagPermissions();
        return FlagPermissions.load(root, newperms);
    }

    protected static FlagPermissions load(Map<String, Object> root, FlagPermissions newperms) throws Exception {
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
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Flags")+":§9 " + listFlags());
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Your.Flags")+":§a " + listPlayerFlags(player.getName()));
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Group.Flags")+":§c " + listGroupFlags());
        player.sendMessage("§e"+Residence.getLanguage().getPhrase("Others.Flags")+":§c " + listOtherPlayersFlags(player.getName()));
    }

    public void copyUserPermissions(String fromUser, String toUser)
    {
        fromUser = fromUser.toLowerCase();
        toUser = toUser.toLowerCase();
        Map<String, Boolean> get = playerFlags.get(fromUser);
        if(get!=null)
        {
            Map<String, Boolean> targ = playerFlags.get(toUser);
            if(targ==null)
            {
                targ = new HashMap<String,Boolean>();
                playerFlags.put(toUser, targ);
            }
            for(Entry<String, Boolean> entry : get.entrySet())
            {
                targ.put(entry.getKey(),entry.getValue());
            }
        }
    }

    public void clearPlayersFlags(String user)
    {
        if(playerFlags.containsKey(user))
            playerFlags.remove(user);
    }

    public void setParent(FlagPermissions p)
    {
        parent = p;
    }

    public FlagPermissions getParent()
    {
        return parent;
    }
}

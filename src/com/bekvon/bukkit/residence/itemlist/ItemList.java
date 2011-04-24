/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.itemlist;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.util.config.ConfigurationNode;


/**
 *
 * @author Administrator
 */
public class ItemList {

    protected ArrayList<String> list;
    protected ListType type;
    protected String world;
    protected String group;

    public static enum ListType
    {
        BLACKLIST,WHITELIST
    }

    public ItemList(ListType listType)
    {
        type = listType;
        list = new ArrayList<String>();
    }

    public ListType getType()
    {
        return type;
    }

    public boolean contains(Material mat)
    {
        return list.contains(mat.toString());
    }

    public void add(Material mat)
    {
        if(!list.contains(mat.toString()))
            list.add(mat.toString());
    }

    public void remove(Material mat)
    {
        list.remove(mat.toString());
    }

    public String getWorld()
    {
        return world;
    }

    public String getGroup()
    {
        return group;
    }

    public boolean isAllowed(Material mat, String inworld, String ingroup)
    {
        if(world!=null)
        {
            if(!world.equals(inworld))
            {
                return true;
            }
        }
        if(group!=null)
        {
            if(!group.equals(ingroup))
            {
                return true;
            }
        }
        if(type == ListType.BLACKLIST)
        {
            if(list.contains(mat.toString()))
            {
                return false;
            }
            return true;
        }
        else if(type == ListType.WHITELIST)
        {
            if(list.contains(mat.toString()))
            {
                return true;
            }
            return false;
        }
        return false;
    }

    public int getListSize()
    {
        return list.size();
    }

    public static ItemList readList(ConfigurationNode node)
    {
        ListType type = ListType.valueOf(node.getString("Type","").toUpperCase());
        ItemList list = new ItemList(type);
        list.world = node.getString("World",null);
        list.group = node.getString("Group",null);
        List<String> items = node.getStringList("Items", null);
        if (items != null) {
            for (String item : items) {
                int parse = -1;
                try {
                    parse = Integer.parseInt(item);
                } catch (Exception ex) {
                }
                if (parse == -1) {
                    try {
                        list.add(Material.valueOf(item.toUpperCase()));
                    } catch (Exception ex) {
                    }
                } else {
                    try {
                        list.add(Material.getMaterial(parse));
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return list;
    }
}

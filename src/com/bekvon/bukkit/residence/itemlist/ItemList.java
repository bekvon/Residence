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

    protected List<Material> list;
    protected ListType type;

    public ItemList(ListType listType)
    {
        this();
        type = listType;
    }

    protected ItemList()
    {
        list = new ArrayList<Material>();
    }

    public static enum ListType
    {
        BLACKLIST,WHITELIST,IGNORELIST,OTHER
    }

     public ListType getType()
    {
        return type;
    }

    public boolean contains(Material mat)
    {
        return list.contains(mat);
    }

    public void add(Material mat)
    {
        if(!list.contains(mat))
            list.add(mat);
    }

    public void remove(Material mat)
    {
        list.remove(mat);
    }

    public boolean isAllowed(Material mat)
    {
        if(type == ListType.BLACKLIST)
        {
            if(list.contains(mat))
            {
                return false;
            }
            return true;
        }
        else if(type == ListType.WHITELIST)
        {
            if(list.contains(mat))
            {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isIgnored(Material mat)
    {
        if(type == ListType.IGNORELIST)
        {
            if(list.contains(mat))
            {
                return true;
            }
        }
        return false;
    }

    public int getListSize()
    {
        return list.size();
    }

    public static ItemList readList(ConfigurationNode node)
    {
        return ItemList.readList(node, new ItemList());
    }

    protected static ItemList readList(ConfigurationNode node, ItemList list)
    {
        ListType type = ListType.valueOf(node.getString("Type","").toUpperCase());
        list.type = type;
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

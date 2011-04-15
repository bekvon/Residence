/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.itemlist;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class ItemManager {
    protected List<ItemList> lists;

    public ItemManager(Configuration config)
    {
        lists = new ArrayList<ItemList>();
        this.readLists(config);
    }

    public boolean isAllowed(Material mat, String group, String world) {
        for (ItemList list : lists) {
            if (!list.isAllowed(mat, world, group)) {
                return false;
            }
        }
        return true;
    }

    public void readLists(Configuration config) {
        List<String> keys = config.getKeys("ItemList");
        if (keys != null) {
            for (String key : keys) {
                try {
                    ItemList list = ItemList.readList(config.getNode("ItemList." + key));
                    lists.add(list);
                    //System.out.println("Debug: read list " + key + " world: " + list.getWorld() + " group: " + list.getGroup() + " itemcount:" + list.getListSize());
                } catch (Exception ex) {
                    System.out.println("Failed to load item list:" + key);
                }
            }
        }
    }
}

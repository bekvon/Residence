package com.bekvon.bukkit.residence.itemlist;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ItemList {

    protected List<Material> list;
    protected ListType type;

    public ItemList(ListType listType) {
	this();
	type = listType;
    }

    protected ItemList() {
	list = new ArrayList<Material>();
    }

    public static enum ListType {
	BLACKLIST, WHITELIST, IGNORELIST, OTHER
    }

    public ListType getType() {
	return type;
    }

    public boolean contains(Material mat) {
	return list.contains(mat);
    }

    public void add(Material mat) {
	if (!list.contains(mat))
	    list.add(mat);
    }

    public boolean toggle(Material mat) {
	if (list.contains(mat)) {
	    list.remove(mat);
	    return false;
	}
	list.add(mat);
	return true;
    }

    public void remove(Material mat) {
	list.remove(mat);
    }

    public boolean isAllowed(Material mat) {
	if (type == ListType.BLACKLIST) {
	    if (list.contains(mat)) {
		return false;
	    }
	    return true;
	} else if (type == ListType.WHITELIST) {
	    if (list.contains(mat)) {
		return true;
	    }
	    return false;
	}
	return true;
    }

    public boolean isIgnored(Material mat) {
	if (type == ListType.IGNORELIST) {
	    if (list.contains(mat)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isListed(Material mat) {
	return this.contains(mat);
    }

    public int getListSize() {
	return list.size();
    }

    public static ItemList readList(ConfigurationSection node) {
	return ItemList.readList(node, new ItemList());
    }

    @SuppressWarnings("deprecation")
    protected static ItemList readList(ConfigurationSection node, ItemList list) {
	ListType type = ListType.valueOf(node.getString("Type", "").toUpperCase());
	list.type = type;
	List<String> items = node.getStringList("Items");
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

    public void printList(Player player) {
	StringBuilder builder = new StringBuilder();
	boolean first = true;
	for (Material mat : list) {
	    if (!first)
		builder.append(", ");
	    else
		builder.append(ChatColor.YELLOW);
	    builder.append(mat);
	    first = false;
	}
	player.sendMessage(builder.toString());
    }

    public Material[] toArray() {
	Material mats[] = new Material[list.size()];
	int i = 0;
	for (Material mat : list) {
	    mats[i] = mat;
	    i++;
	}
	return mats;
    }

    public Map<String, Object> save() {
	Map<String, Object> saveMap = new LinkedHashMap<String, Object>();
	saveMap.put("Type", type.toString());
	List<String> saveList = new ArrayList<String>();
	for (Material mat : list) {
	    saveList.add(mat.toString());
	}
	saveMap.put("ItemList", saveList);
	return saveMap;
    }

    public static ItemList load(Map<String, Object> map) {
	ItemList newlist = new ItemList();
	return load(map, newlist);
    }

    protected static ItemList load(Map<String, Object> map, ItemList newlist) {
	try {
	    newlist.type = ListType.valueOf((String) map.get("Type"));
	    @SuppressWarnings("unchecked")
	    List<String> list = (List<String>) map.get("ItemList");
	    for (String item : list) {
		newlist.add(Material.valueOf(item));
	    }
	} catch (Exception ex) {
	}
	return newlist;
    }
}

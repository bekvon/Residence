/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.itemlist;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.persistance.SQLManager;

/**
 *
 * @author Administrator
 */
public class ItemList {

	protected List<Material> list;
	protected ListType type;
	protected SQLManager sql;

	public ItemList(ListType listType) {
		this();
		type = listType;
	}

	protected ItemList() {
		list = new ArrayList<Material>();
		sql = Residence.getSQLManager();
	}

	public static enum ListType {
		BLACKLIST,WHITELIST,IGNORELIST,OTHER
	}

	public ListType getType() {
		return type;
	}

	public boolean contains(Material mat) {
		return list.contains(mat);
	}

	public void add(Material mat) {
		if(!list.contains(mat)) {
			list.add(mat);
		}
	}

	public boolean toggle(Material mat) {
		if(list.contains(mat)) {
			list.remove(mat);
			return false;
		} else {
			list.add(mat);
			return true;
		}
	}

	public void remove(Material mat) {
		list.remove(mat);
	}

	public boolean isAllowed(Material mat) {
		if(type == ListType.BLACKLIST) {
			if(list.contains(mat)) {
				return false;
			}
			return true;
		} else if(type == ListType.WHITELIST) {
			if(list.contains(mat)) {
				return true;
			}
			return false;
		}
		return true;
	}

	public boolean isIgnored(Material mat) {
		if(type == ListType.IGNORELIST) {
			if(list.contains(mat)) {
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

	public void printList(Player player) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(Material mat : list) {
			if(!first) {
				builder.append(", ");
			} else {
				builder.append(ChatColor.YELLOW);
			}
			builder.append(mat);
			first = false;
		}
		player.sendMessage(builder.toString());
	}

	public Material[] toArray() {
		Material mats[] = new Material[list.size()];
		int i = 0;
		for(Material mat : list) {
			mats[i] = mat;
			i++;
		}
		return mats;
	}

	public static ItemList load(ListType type, String itemlist) {
		ItemList newlist = new ItemList();
		return load(type, itemlist ,newlist);
	}

	protected static ItemList load(ListType type, String itemlist, ItemList newlist) {
		try {
			newlist.type = type;
			String[] items = itemlist.split(":");
			for(String item : items){
				newlist.add(Material.getMaterial(Integer.valueOf(item)));
			}
		} catch (Exception ex) {

		}
		return newlist;
	}
}

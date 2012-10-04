/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.itemlist;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Administrator
 */
public class WorldItemList extends ItemList {
	protected String world;
	protected String group;

	public WorldItemList(ListType listType) {
		super(listType);
	}

	protected WorldItemList() {

	}

	public String getWorld() {
		return world;
	}

	public String getGroup() {
		return group;
	}

	public boolean isAllowed(Material mat, String inworld, String ingroup) {
		if(!listApplicable(inworld,ingroup)) {
			return true;
		}
		return super.isAllowed(mat);
	}

	public boolean isIgnored(Material mat, String inworld, String ingroup) {
		if(!listApplicable(inworld,ingroup)) {
			return false;
		}
		return super.isIgnored(mat);
	}

	public boolean isListed(Material mat, String inworld, String ingroup) {
		if(!listApplicable(inworld,ingroup)) {
			return false;
		}
		return super.isListed(mat);
	}

	public boolean listApplicable(String inworld, String ingroup) {
		if (world != null) {
			if (!world.equalsIgnoreCase(inworld)) {
				return false;
			}
		}
		if (group != null) {
			if (!group.equals(ingroup)) {
				return false;
			}
		}
		return true;
	}

	public static WorldItemList readList(ConfigurationSection node) {
		WorldItemList list = new WorldItemList();
		readList(node, list);
		list.world = node.getString("World",null);
		list.group = node.getString("Group",null);
		return list;
	}
	protected static ItemList readList(ConfigurationSection node, ItemList list) {
		ListType type = ListType.valueOf(node.getString("Type","").toUpperCase());
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
}

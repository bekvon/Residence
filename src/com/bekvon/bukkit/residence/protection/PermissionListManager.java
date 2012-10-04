/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

/**
 *
 * @author Administrator
 */
public class PermissionListManager {

	private Map<String,Map<String,FlagPermissions>> lists;

	public PermissionListManager() {
		lists = Collections.synchronizedMap(new HashMap<String,Map<String,FlagPermissions>>());
	}

	public FlagPermissions getList(String player, String listname) {
		Map<String, FlagPermissions> get = lists.get(player);
		if(get == null) {
			return null;
		}
		return get.get(listname);
	}

	public void makeList(Player player, String listname) {
		Map<String, FlagPermissions> get = lists.get(player.getName());
		if(get == null) {
			get = new HashMap<String,FlagPermissions>();
			lists.put(player.getName(), get);
		}
		FlagPermissions perms = get.get(listname);
		if(perms == null) {
			perms = new FlagPermissions();
			get.put(listname, perms);
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("ListCreate", listname));
		} else {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ListExists"));
		}
	}

	public void removeList(Player player, String listname) {
		Map<String, FlagPermissions> get = lists.get(player.getName());
		if(get == null) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidList"));
			return;
		}
		FlagPermissions list = get.get(listname);
		if(list == null) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidList"));
			return;
		}
		get.remove(listname);
		player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ListRemoved"));
	}

	public void applyListToResidence(Player player, String listname, String areaname, boolean resadmin) {
		FlagPermissions list = this.getList(player.getName(), listname);
		if(list == null) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidList"));
			return;
		}
		ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
		if(res == null) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidResidence"));
			return;
		}
		res.getPermissions().applyTemplate(player, list, resadmin);
	}

	public void printList(Player player, String listname) {
		ResidencePermissions list = (ResidencePermissions) this.getList(player.getName(), listname);
		if(list == null) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidList"));
			return;
		}
		player.sendMessage(ChatColor.LIGHT_PURPLE+"------Permission Template------");
		player.sendMessage(Residence.getLanguage().getPhrase("Name")+": "+ChatColor.GREEN + listname);
		list.printFlags(player);
	}

	public void printLists(Player player) {
		StringBuilder sbuild = new StringBuilder();
		Map<String, FlagPermissions> get = lists.get(player.getName());
		sbuild.append(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Lists")+":"+ChatColor.DARK_AQUA+" ");
		if(get != null) {
			for( Entry<String, FlagPermissions> thislist : get.entrySet()) {
				sbuild.append(thislist.getKey()).append(" ");
			}
		}
		player.sendMessage(sbuild.toString());
	}
}


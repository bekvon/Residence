/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.itemlist;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 *
 * @author Administrator
 */
public class ResidenceItemList extends ItemList {
	ClaimedResidence res;

	public ResidenceItemList(ClaimedResidence parent, ListType type) {
		super(type);
		res = parent;
	}

	private ResidenceItemList()  {

	}

	public String saveable() {
		String result = "";
		for(Material mat : this.toArray()) {
			if(!result.isEmpty()){
				result += ":";
			}
			result += mat.getId();
		}
		if(result.isEmpty()){
			return null;
		}
		return result;
	}

	public void save() {
		if(this.getType().equals(ListType.WHITELIST)) {
			sql.alterWhiteList(this.saveable(), res.getId());
		} else if (this.getType().equals(ListType.BLACKLIST)) {
			sql.alterBlackList(this.saveable(), res.getId());
		}
	}

	public void playerListChange(Player player, Material mat, boolean resadmin) {
		PermissionGroup group = Residence.getPermissionManager().getGroup(player);
		if(resadmin || res.getPermissions().hasResidencePermission(player, true) && group.itemListAccess()) {
			if(super.toggle(mat)) {
				player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ListMaterialAdd",ChatColor.GREEN + String.format("%d",mat) + ChatColor.YELLOW+"."+ChatColor.GREEN + type.toString().toLowerCase() + ChatColor.YELLOW));
			} else {
				player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ListMaterialRemove",ChatColor.GREEN + String.format("%d",mat) + ChatColor.YELLOW+"."+ChatColor.GREEN + type.toString().toLowerCase() + ChatColor.YELLOW));
			}

		} else {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
		}
	}

	public ResidenceItemList load(ClaimedResidence parent, ListType type, String list) {
		ResidenceItemList newlist = new ResidenceItemList();
		newlist.res = parent;
		return (ResidenceItemList) ItemList.load(type, list, newlist);
	}
}

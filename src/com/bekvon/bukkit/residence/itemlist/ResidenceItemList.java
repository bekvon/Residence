package com.bekvon.bukkit.residence.itemlist;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ResidenceItemList extends ItemList {
    ClaimedResidence res;

    public ResidenceItemList(ClaimedResidence parent, ListType type) {
	super(type);
	res = parent;
    }

    private ResidenceItemList() {

    }

    public void playerListChange(Player player, Material mat, boolean resadmin) {

	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (resadmin || (res.getPermissions().hasResidencePermission(player, true) && group.itemListAccess())) {
	    if (super.toggle(mat))
		Residence.msg(player, lm.General_ListMaterialAdd, mat.toString(), type.toString().toLowerCase());
	    else
		Residence.msg(player, lm.General_ListMaterialRemove, mat.toString(), type.toString().toLowerCase());
	} else {
	    Residence.msg(player, lm.General_NoPermission);
	}
    }

    public static ResidenceItemList load(ClaimedResidence parent, Map<String, Object> map) {
	ResidenceItemList newlist = new ResidenceItemList();
	newlist.res = parent;
	return (ResidenceItemList) ItemList.load(map, newlist);
    }
}

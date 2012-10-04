/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.persistance.SQLManager;

/**
 *
 * @author Administrator
 */
public class WorldPermissions extends FlagPermissions {
	protected String world;
	protected SQLManager sql;

	public WorldPermissions(String world) {
		this.world = world;
		sql = Residence.getSQLManager();
	}

	@Override
	public void removeAllGroupFlags(String group) {
		sql.removeAllWorldGroupFlags(group, world);
	}

	@Override
	public boolean setGroupFlag(String group, String flag, FlagState state) {
		boolean value = (Boolean) null;
		if (state == FlagState.FALSE) {
			value = false;
		} else if (state == FlagState.TRUE) {
			value = true;
		}
		sql.setWorldGroupFlag(group, flag, value, world);
		return true;
	}

	@Override
	public boolean setFlag(String flag, FlagState state) {
		boolean value = (Boolean) null;
		if (state == FlagState.FALSE) {
			value = false;
		} else if (state == FlagState.TRUE) {
			value = true;
		}
		sql.setWorldFlag(flag, value, world);
		return true;
	}

	@Override
	public boolean playerHas(String player, String flag, boolean def) {
		String group = Residence.getPermissionManager().getGroupNameByPlayer(player, world);
		return this.groupCheck(group, flag, this.has(flag, def));
	}

	@Override
	public boolean groupHas(String group, String flag, boolean def) {
		return this.groupCheck(group, flag, this.has(flag, def));
	}

	private boolean groupCheck(String group, String flag, boolean def) {
		boolean value = sql.getWorldGroupFlag(group, flag, world);
		if(value != (Boolean)null){
			return value;
		}
		return new GlobalPermissions().groupCheck(group, flag, def);
	}

	@Override
	public boolean has(String flag, boolean def) {
		boolean value = sql.getWorldFlag(flag, world);
		if(value != (Boolean)null){
			return value;
		}
		return new GlobalPermissions().has(flag, def);
	}
}

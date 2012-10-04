package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.persistance.SQLManager;

public class GlobalPermissions extends FlagPermissions{
	protected static SQLManager sql;

	public GlobalPermissions() {
		sql = Residence.getSQLManager();
	}

	@Override
	public void removeAllGroupFlags(String group) {
		sql.removeAllWorldGroupFlags(group, null);
	}

	@Override
	public boolean setGroupFlag(String group, String flag, FlagState state) {
		boolean value = (Boolean) null;
		if (state == FlagState.FALSE) {
			value = false;
		} else if (state == FlagState.TRUE) {
			value = true;
		}
		sql.setWorldGroupFlag(group, flag, value, null);
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
		sql.setWorldFlag(flag, value, null);
		return true;
	}

	@Override
	public boolean playerHas(String player, String flag, boolean def) {
		String group = Residence.getPermissionManager().getGroupNameByPlayer(player, null);
		return groupCheck(group, flag, has(flag, def));
	}

	@Override
	public boolean groupHas(String group, String flag, boolean def) {
		return groupCheck(group, flag, has(flag, def));
	}

	public boolean groupCheck(String group, String flag, boolean def) {
		boolean value = sql.getWorldGroupFlag(group, flag, null);
		if(value != (Boolean)null){
			return value;
		}
		return def;
	}

	@Override
	public boolean has(String flag, boolean def) {
		boolean value = sql.getWorldFlag(flag, null);
		if(value != (Boolean)null){
			return value;
		}
		return def;
	}
}

package com.bekvon.bukkit.residence.persistance;

import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class MetaDataManager {
	protected SQLManager sql;
	public MetaDataManager() {
		sql = Residence.getSQLManager();
	}
	public String getMetaData(Plugin plugin, String name, ClaimedResidence res){
		return sql.getMetaData(plugin.getName(), name, res.getId());
	}
	public void RegisterMetaDataColumn(Plugin plugin, String name) {
		sql.registerMetaData(plugin.getName(), name);
	}
	public void setMetaData(Plugin plugin, String name, String data, ClaimedResidence res){
		sql.setMetaData(plugin.getName(), name, data, res.getId());
	}
}

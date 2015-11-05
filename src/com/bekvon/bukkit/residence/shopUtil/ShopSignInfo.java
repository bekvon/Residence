package com.bekvon.bukkit.residence.shopUtil;

import java.util.ArrayList;
import java.util.List;

public class ShopSignInfo {

	List<ShopSigns> AllSigns = new ArrayList<ShopSigns>();

	public ShopSignInfo() {
	}

	public void setAllSigns(List<ShopSigns> AllSigns) {
		this.AllSigns = AllSigns;
	}

	public List<ShopSigns> GetAllSigns() {
		return this.AllSigns;
	}

	public void removeSign(ShopSigns sign) {
		this.AllSigns.remove(sign);
	}

	public void addSign(ShopSigns sign) {
		this.AllSigns.add(sign);
	}
}

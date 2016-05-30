package com.bekvon.bukkit.residence.economy.rent;

import java.util.HashMap;
import java.util.Map;

import com.bekvon.bukkit.residence.Residence;

public class RentableLand {

    public int days;
    public int cost;
    public boolean AllowRenewing = Residence.getConfigManager().isRentAllowRenewing();
    public boolean StayInMarket = Residence.getConfigManager().isRentStayInMarket();
    public boolean AllowAutoPay = Residence.getConfigManager().isRentAllowAutoPay();

    public Map<String, Object> save() {
	Map<String, Object> rented = new HashMap<>();
	rented.put("Days", days);
	rented.put("Cost", cost);
	rented.put("Repeatable", AllowRenewing);
	rented.put("StayInMarket", StayInMarket);
	rented.put("AllowAutoPay", AllowAutoPay);
	return rented;
    }

    public static RentableLand load(Map<String, Object> map) {
	RentableLand newland = new RentableLand();
	newland.cost = (Integer) map.get("Cost");
	newland.days = (Integer) map.get("Days");
	newland.AllowRenewing = (Boolean) map.get("Repeatable");
	if (map.containsKey("StayInMarket"))
	    newland.StayInMarket = (Boolean) map.get("StayInMarket");
	if (map.containsKey("AllowAutoPay"))
	    newland.AllowAutoPay = (Boolean) map.get("AllowAutoPay");
	return newland;
    }
}

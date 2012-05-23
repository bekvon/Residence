/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy.rent;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class RentableLand {

    public int days;
    public int cost;
    public boolean repeatable;

    public Map<String, Object> save() {
        Map<String, Object> rented = new HashMap<String, Object>();
        rented.put("Days", days);
        rented.put("Cost", cost);
        rented.put("Repeatable", repeatable);
        return rented;
    }

    public static RentableLand load(Map<String, Object> map) {
        RentableLand newland = new RentableLand();
        newland.cost = (Integer)map.get("Cost");
        newland.days = (Integer)map.get("Days");
        newland.repeatable = (Boolean)map.get("Repeatable");
        return newland;
    }
}

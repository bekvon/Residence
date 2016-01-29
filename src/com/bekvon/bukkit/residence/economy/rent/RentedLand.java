package com.bekvon.bukkit.residence.economy.rent;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class RentedLand {

    public String player;
    public long startTime;
    public long endTime;
    public boolean autoRefresh;

    public Map<String, Object> save() {
        Map<String, Object> rentables = new HashMap<>();
        rentables.put("Player", player);
        rentables.put("StartTime", startTime);
        rentables.put("EndTime", endTime);
        rentables.put("AutoRefresh", autoRefresh);
        return rentables;
    }
    public static RentedLand load(Map<String,Object> map)
    {
        RentedLand newland = new RentedLand();
        newland.player = (String) map.get("Player");
        newland.startTime = (Long)map.get("StartTime");
        newland.endTime = (Long)map.get("EndTime");
        newland.autoRefresh = (Boolean)map.get("AutoRefresh");
        return newland;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy.rent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class RentManager {
    protected Map<String,RentedLand> rentedLand;
    protected Map<String,RentableLand> rentableLand;

    public RentManager()
    {
        rentedLand = new HashMap<String,RentedLand>();
        rentableLand = new HashMap<String,RentableLand>();
    }

    public void setForRent(Player player, String landName, int amount, int days, boolean repeatable)
    {
        ClaimedResidence res = Residence.getResidenceManger().getByName(landName);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(res == null)
        {
            player.sendMessage(landName + " is not a valid residence.");
            return;
        }
        if(!res.getPermissions().hasResidencePermission(player, true) && !resadmin)
        {
            player.sendMessage("You are not the owner of " + landName);
            return;
        }
        if(!rentableLand.containsKey(landName))
        {
            RentableLand newrent = new RentableLand();
            newrent.days = days;
            newrent.cost = amount;
            newrent.repeatable = repeatable;
            rentableLand.put(landName,newrent);
            player.sendMessage(landName + " is now for rent for "+amount+" for "+days+" days.");
        }
        else
        {
            player.sendMessage(landName + " is already for rent!");
        }
    }

    public void removeFromRent(Player player, String landName)
    {
        ClaimedResidence res = Residence.getResidenceManger().getByName(landName);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(res == null)
        {
            player.sendMessage(landName + " is not a valid residence.");
            return;
        }
        if(!res.getPermissions().hasResidencePermission(player, true) && !resadmin)
        {
            player.sendMessage("You are not the owner of " + landName);
            return;
        }
        if(rentedLand.containsKey(landName) && !resadmin)
        {
            player.sendMessage(landName + " is currently rented out to: " + rentedLand.get(landName).player);
            return;
        }
        if(rentableLand.containsKey(landName))
        {
            rentableLand.remove(landName);
            if(rentedLand.containsKey(landName))
                rentedLand.remove(landName);
            player.sendMessage(landName + " is not longer rentable."); 
        }
        else
        {
            player.sendMessage(landName + " not for rent.");
        }
    }

    public boolean isForRent(String landName)
    {
        return rentableLand.containsKey(landName);
    }

    public boolean isRented(String landName)
    {
        return rentedLand.containsKey(landName);
    }

    public String getRentingPlayer(String landName)
    {
        return rentedLand.containsKey(landName) ? rentedLand.get(landName).player : null;
    }

    public void checkCurrentRents()
    {
        Iterator<Entry<String, RentedLand>> it = rentedLand.entrySet().iterator();
        while(it.hasNext())
        {
            Entry<String, RentedLand> next = it.next();
            RentedLand land = next.getValue();
            if(land.endTime<=System.currentTimeMillis())
            {
                RentableLand rentable = rentableLand.get(next.getKey());
                if(!rentable.repeatable)
                    rentableLand.remove(next.getKey());
                if(land.autoRefresh && rentable.repeatable)
                {
                    if(!TransactionManager.chargeEconomyMoney(land.player, rentable.cost, "rent of land: " + next.getKey()))
                    {
                        it.remove();
                    }
                }
                else
                    it.remove();
            }
        }
    }

    public void setRentRepeatable(Player player, String landName, boolean value)
    {
        RentableLand land = rentableLand.get(landName);
        ClaimedResidence res = Residence.getResidenceManger().getByName(landName);
        if(land!=null && res!=null && (res.getPermissions().getOwner().equalsIgnoreCase(player.getName()) || Residence.getPermissionManager().isResidenceAdmin(player)))
        {
            land.repeatable = value;
        }
    }

    public void setRefreshAutoRepeat(Player player, String landName, boolean value)
    {
        RentedLand land = rentedLand.get(landName);
        if(land!=null && (land.player.equals(player.getName()) || Residence.getPermissionManager().isResidenceAdmin(player)))
        {
            land.autoRefresh = value;
        }
    }

    public void removeRenter(Player player, String landName)
    {
        RentedLand land = rentedLand.get(landName);
        if(land != null && (land.player.equals(player.getName()) || Residence.getPermissionManager().isResidenceAdmin(player)))
        {
            rentedLand.remove(landName);
            player.sendMessage(landName + " is no longer rented.");
            RentableLand rentable = rentableLand.get(landName);
            if(!rentable.repeatable)
                rentableLand.remove(landName);
        }
    }

    public void printRentInfo(Player player, String landName)
    {
        RentableLand rentable = rentableLand.get(landName);
        RentedLand rented = rentedLand.get(landName);
        if(rentable!=null)
        {
            player.sendMessage("Land Name:" + landName);
            player.sendMessage("Cost: " + rentable.cost + " per " + rentable.days + " days");
            player.sendMessage("Rent Repeatable: " + rentable.repeatable);
            if(rented!=null)
            {
                player.sendMessage("Status: Currently rented by: " + rented.player);
                player.sendMessage("Expire Time:" + new Date(rented.endTime));
                if(player.getName().equalsIgnoreCase(rented.player))
                {
                    if(rented.autoRefresh)
                        player.sendMessage("You have chosen to automatically make payments so long as you have money to do so.");
                }
            }
            else
            {
                player.sendMessage("Status: Available");
            }
        }
        else
        {
            player.sendMessage(landName + " has no rent information.");
        }
    }

    public static RentManager load(Map<String,Object> root)
    {
        RentManager rentManager = new RentManager();
        if(root!=null)
        {
            Map<String,Object> rentables = (Map<String, Object>) root.get("Rentables");
            for(Entry<String, Object> rent : rentables.entrySet())
            {
                rentManager.rentableLand.put(rent.getKey(), RentableLand.load((Map<String, Object>) rent.getValue()));
            }
            Map<String,Object> rented = (Map<String, Object>) root.get("Rented");
            for(Entry<String, Object> rent : rented.entrySet())
            {
                rentManager.rentedLand.put(rent.getKey(), RentedLand.load((Map<String, Object>) rent.getValue()));
            }
        }
        return rentManager;
    }

    public Map<String,Object> save()
    {
        Map<String,Object> root = new HashMap<String,Object>();
        Map<String,Object> rentables = new HashMap<String,Object>();
        for(Entry<String, RentableLand> rent : rentableLand.entrySet())
        {
            rentables.put(rent.getKey(), rent.getValue().save());
        }
        Map<String,Object> rented = new HashMap<String,Object>();
        for(Entry<String, RentedLand> rent : rentedLand.entrySet())
        {
            rented.put(rent.getKey(), rent.getValue().save());
        }
        root.put("Rentables", rentables);
        root.put("Rented", rented);
        return root;
    }

    public void updateRentableName(String oldName, String newName)
    {
        if(rentableLand.containsKey(oldName))
        {
            rentableLand.put(newName, rentableLand.get(oldName));
            rentableLand.remove(oldName);
        }
        if(rentedLand.containsKey(oldName))
        {
            rentedLand.put(newName, rentedLand.get(oldName));
            rentedLand.remove(oldName);
        }
    }
}

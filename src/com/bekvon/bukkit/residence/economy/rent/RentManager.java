/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.economy.rent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
        if(!Residence.getConfig().enabledRentSystem())
        {
            player.sendMessage("§cRent system is disabled.");
            return;
        }
        if(Residence.getTransactionManager().isForSale(landName))
        {
            player.sendMessage("§cCannot rent a residence while it is for sale!");
            return;
        }
        ClaimedResidence res = Residence.getResidenceManger().getByName(landName);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(res == null)
        {
            player.sendMessage("§e"+landName + "§c is not a valid residence.");
            return;
        }
        if(!resadmin)
        {
            if(!res.getPermissions().hasResidencePermission(player, true))
            {
                player.sendMessage("§cYou are not the owner of " + landName);
                return;
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if(this.getRentableCount(player.getName()) >= group.getMaxRentables())
            {
                player.sendMessage("§cYou've reached the max residences your allowed to rent.");
                return;
            }
        }
        if(!rentableLand.containsKey(landName))
        {
            RentableLand newrent = new RentableLand();
            newrent.days = days;
            newrent.cost = amount;
            newrent.repeatable = repeatable;
            rentableLand.put(landName,newrent);
            player.sendMessage("§e"+landName + "§a is now for rent for §e"+amount+"§a for §e"+days+"§a days.");
        }
        else
        {
            player.sendMessage("§e"+landName + "§c is already for rent!");
        }
    }

    public void rent(Player player, String landName, boolean repeat)
    {
        if(!Residence.getConfig().enabledRentSystem())
        {
            player.sendMessage("§cRent system is disabled.");
            return;
        }
        ClaimedResidence res = Residence.getResidenceManger().getByName(landName);
        if(res!=null)
        {
            if(res.getPermissions().getOwner().equalsIgnoreCase(player.getName()))
            {
                player.sendMessage("§cCan't rent land you already own!");
                return;
            }
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(player);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(!resadmin && this.getRentCount(player.getName()) >= group.getMaxRents())
        {
            player.sendMessage("§cYou reached the maximum rents your allowed.");
            return;
        }
        if(!this.isForRent(landName))
        {
            player.sendMessage("§e"+landName + "§c is not for rent or doest not exist.");
            return;
        }
        if(this.isRented(landName))
        {
            player.sendMessage("§e"+landName + "§c has already been rented to: §e" + this.getRentingPlayer(landName));
            return;
        }
        RentableLand land = rentableLand.get(landName);
        if(Residence.getEconomyManager().canAfford(player.getName(), land.cost))
        {
            if(Residence.getEconomyManager().transfer(player.getName(), res.getPermissions().getOwner(), land.cost))
            {
                RentedLand newrent = new RentedLand();
                newrent.player = player.getName();
                newrent.startTime = System.currentTimeMillis();
                newrent.endTime = System.currentTimeMillis() + daysToMs(land.days);
                newrent.autoRefresh = repeat;
                rentedLand.put(landName, newrent);
                if(res!=null)
                    res.getPermissions().copyUserPermissions(res.getPermissions().getOwner(), player.getName());
                player.sendMessage("§aYou have rented land §e" + landName + "§a for §e" + land.days + "§a days.");
            }
            else
            {
                player.sendMessage("§cError, unable to transfer money...");
            }
        }
        else
        {
            player.sendMessage("§cYou cannot afford to rent this residence.");
        }
    }

    public void release(Player player, String landName)
    {
        RentedLand rent = rentedLand.get(landName);
        if(rent == null)
        {
            player.sendMessage("§cLand not rented.");
            return;
        }
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(resadmin || rent.player.equalsIgnoreCase(player.getName()))
        {
            rentedLand.remove(landName);
            player.sendMessage("§e"+landName + "§a has been unrented.");
        }
        else
        {
            player.sendMessage("§cYou don't have permission to unrent this.");
        }
    }

    private long daysToMs(int days)
    {
        return (((long)days) * 24L * 60L * 60L * 1000L);
    }

    private int msToDays(long ms)
    {
        return (int) Math.ceil(((((double)ms/1000D)/60D)/60D)/24D);
    }

    public void removeFromRent(Player player, String landName)
    {
        ClaimedResidence res = Residence.getResidenceManger().getByName(landName);
        boolean resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
        if(res == null)
        {
            player.sendMessage("§e"+landName + "§c is not a valid residence.");
            return;
        }
        if(!res.getPermissions().hasResidencePermission(player, true) && !resadmin)
        {
            player.sendMessage("§cYou are not the owner of §e" + landName);
            return;
        }
        if(rentedLand.containsKey(landName) && !resadmin)
        {
            player.sendMessage("§e"+landName + "§c is currently rented out to: §e" + rentedLand.get(landName).player);
            return;
        }
        if(rentableLand.containsKey(landName))
        {
            rentableLand.remove(landName);
            if(rentedLand.containsKey(landName))
                rentedLand.remove(landName);
            player.sendMessage("§e"+landName + "§c is not longer rentable.");
        }
        else
        {
            player.sendMessage("§e"+landName + "§c not for rent.");
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
                ClaimedResidence res = Residence.getResidenceManger().getByName(next.getKey());
                if(res!=null)
                {
                    RentableLand rentable = rentableLand.get(next.getKey());
                    if(!rentable.repeatable)
                    {
                        rentableLand.remove(next.getKey());
                        it.remove();
                        res.getPermissions().applyDefaultFlags();
                    }
                    else if(land.autoRefresh)
                    {
                        if(!Residence.getEconomyManager().canAfford(land.player, rentable.cost))
                        {
                            it.remove();
                            res.getPermissions().applyDefaultFlags();
                        }
                        else
                        {
                            if(!Residence.getEconomyManager().transfer(land.player, res.getPermissions().getOwner(), rentable.cost))
                            {
                                it.remove();
                                res.getPermissions().applyDefaultFlags();
                            }
                        }
                    }
                    else
                    {
                        res.getPermissions().applyDefaultFlags();
                        it.remove();
                    }
                }
                else
                {
                    rentableLand.remove(next.getKey());
                    it.remove();
                }
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
            if(!value && this.isRented(landName))
                rentedLand.get(landName).autoRefresh = false;
            if(value)
                player.sendMessage("§e"+landName + "§c will now automatically renew rentable status upon expire.");
            else
                player.sendMessage("§e"+landName + "§c will not automatically renew.");
        }
    }

    public void setRentedRepeatable(Player player, String landName, boolean value)
    {
        RentedLand land = rentedLand.get(landName);
        if(land!=null && (land.player.equals(player.getName()) || Residence.getPermissionManager().isResidenceAdmin(player)))
        {
            land.autoRefresh = value;
            if(value)
                player.sendMessage("§e"+landName + "§c will now automatically be re-rented upon expire.");
            else
                player.sendMessage("§e"+landName + "§c will not automatically re-rent.");
        }
    }

    public void removeRenter(Player player, String landName)
    {
        RentedLand land = rentedLand.get(landName);
        if(land != null && (land.player.equals(player.getName()) || Residence.getPermissionManager().isResidenceAdmin(player)))
        {
            rentedLand.remove(landName);
            player.sendMessage("§e"+landName + "§c is no longer rented.");
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
            player.sendMessage("§6Land Name:§2" + landName);
            player.sendMessage("§eCost: §3" + rentable.cost + " per " + rentable.days + " days");
            player.sendMessage("§aRentable Auto Renew:§3" + rentable.repeatable);
            if(rented!=null)
            {
                player.sendMessage("§6Status:§c Currently rented by: §e" + rented.player);
                player.sendMessage("§eExpire Time:§a" + new Date(rented.endTime));
                player.sendMessage("§aRent Auto Renew:§3" + rented.autoRefresh);
            }
            else
            {
                player.sendMessage("§6Status:§a Available");
            }
        }
        else
        {
            player.sendMessage("§e"+landName + "§c has no rent information.");
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

    public void printRentableResidences(Player player)
    {
        Set<Entry<String, RentableLand>> set = rentableLand.entrySet();
        player.sendMessage("§eRentable Land:");
        StringBuilder sbuild = new StringBuilder();
        sbuild.append("§a");
        boolean firstadd = true;
        for(Entry<String, RentableLand> land : set)
        {
            if(!this.isRented(land.getKey()))
            {
                if(!firstadd)
                    sbuild.append(", ");
                else
                    firstadd = true;
                sbuild.append(land.getKey());
            }
        }
        player.sendMessage(sbuild.toString());
    }

    public int getRentCount(String player)
    {
        Set<Entry<String, RentedLand>> set = rentedLand.entrySet();
        int count = 0;
        for(Entry<String, RentedLand> land : set)
        {
            if(land.getValue().player.equalsIgnoreCase(player))
                count++;
        }
        return count;
    }

    public int getRentableCount(String player)
    {
        Set<String> set = rentableLand.keySet();
        int count = 0;
        for(String land : set)
        {
            ClaimedResidence res = Residence.getResidenceManger().getByName(land);
            if(res!=null)
                if(res.getPermissions().getOwner().equalsIgnoreCase(player))
                    count++;
        }
        return count;
    }
}

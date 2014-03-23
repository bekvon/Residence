package net.t00thpick1.residence.protection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

public class EconomyManager {
    private List<ClaimedResidence> rentList;
    private List<ClaimedResidence> rentable;
    private List<ClaimedResidence> buyable;
    private static EconomyManager instance;

    private EconomyManager() {
        rentList = new ArrayList<ClaimedResidence>();
        rentable = new ArrayList<ClaimedResidence>();
        buyable = new ArrayList<ClaimedResidence>();
    }

    public static void printForSaleResidences(Player player) {
        // TODO Auto-generated method stub
        
    }

    public static void printRentableResidences(Player player) {
        // TODO Auto-generated method stub
        
    }

    static void setForRent(ClaimedResidence res) {
        instance.rentable.add(res);
    }

    static void removeFromRent(ClaimedResidence res) {
        instance.rentable.remove(res);
    }

    static void setRented(ClaimedResidence res) {
        instance.rentList.add(res);
    }

    static void evict(ClaimedResidence res) {
        instance.rentList.remove(res);
    }

    static void setForSale(ClaimedResidence res) {
        instance.buyable.add(res);
    }

    static void removeFromSale(ClaimedResidence res) {
        instance.buyable.remove(res);
    }

    public static void checkRent() {
        Iterator<ClaimedResidence> it = instance.rentList.iterator();
        while (it.hasNext()) {
            if (!it.next().checkRent()) {
                it.remove();
            }
        }
    }

    public static void init() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new EconomyManager();
    }
}

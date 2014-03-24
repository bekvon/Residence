package net.t00thpick1.residence.protection.yaml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

public class YAMLEconomyManager {
    private List<YAMLResidenceArea> rentList;
    private List<YAMLResidenceArea> rentable;
    private List<YAMLResidenceArea> buyable;
    private static YAMLEconomyManager instance;

    private YAMLEconomyManager() {
        rentList = new ArrayList<YAMLResidenceArea>();
        rentable = new ArrayList<YAMLResidenceArea>();
        buyable = new ArrayList<YAMLResidenceArea>();
    }

    public static void printForSaleResidences(Player player) {
        // TODO Auto-generated method stub
        
    }

    public static void printRentableResidences(Player player) {
        // TODO Auto-generated method stub
        
    }

    static void setForRent(YAMLResidenceArea res) {
        instance.rentable.add(res);
    }

    static void removeFromRent(YAMLResidenceArea res) {
        instance.rentable.remove(res);
    }

    static void setRented(YAMLResidenceArea res) {
        instance.rentList.add(res);
    }

    static void evict(YAMLResidenceArea res) {
        instance.rentList.remove(res);
    }

    static void setForSale(YAMLResidenceArea res) {
        instance.buyable.add(res);
    }

    static void removeFromSale(YAMLResidenceArea res) {
        instance.buyable.remove(res);
    }

    public static void checkRent() {
        Iterator<YAMLResidenceArea> it = instance.rentList.iterator();
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
        instance = new YAMLEconomyManager();
    }
}

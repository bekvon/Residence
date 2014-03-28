package net.t00thpick1.residence.protection.yaml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.t00thpick1.residence.api.areas.ResidenceArea;

public class YAMLEconomyManager {
    private List<ResidenceArea> rentList;
    private List<ResidenceArea> rentable;
    private List<ResidenceArea> buyable;
    private static YAMLEconomyManager instance;

    private YAMLEconomyManager() {
        rentList = new ArrayList<ResidenceArea>();
        rentable = new ArrayList<ResidenceArea>();
        buyable = new ArrayList<ResidenceArea>();
    }

    static void setForRent(ResidenceArea res) {
        instance.rentable.add(res);
    }

    static void removeFromRent(ResidenceArea res) {
        instance.rentable.remove(res);
    }

    static void setRented(ResidenceArea res) {
        instance.rentList.add(res);
    }

    static void evict(ResidenceArea res) {
        instance.rentList.remove(res);
    }

    static void setForSale(ResidenceArea res) {
        instance.buyable.add(res);
    }

    static void removeFromSale(ResidenceArea res) {
        instance.buyable.remove(res);
    }

    public static void checkRent() {
        Iterator<ResidenceArea> it = instance.rentList.iterator();
        while (it.hasNext()) {
            if (!((YAMLResidenceArea) it.next()).checkRent()) {
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

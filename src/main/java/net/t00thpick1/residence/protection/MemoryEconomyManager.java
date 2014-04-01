package net.t00thpick1.residence.protection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.t00thpick1.residence.api.EconomyManager;
import net.t00thpick1.residence.api.areas.ResidenceArea;

public class MemoryEconomyManager implements EconomyManager {
    private List<ResidenceArea> rentList;
    private List<ResidenceArea> rentable;
    private List<ResidenceArea> buyable;

    public MemoryEconomyManager() {
        rentList = new ArrayList<ResidenceArea>();
        rentable = new ArrayList<ResidenceArea>();
        buyable = new ArrayList<ResidenceArea>();
    }

    public List<ResidenceArea> getRentableResidences() {
        return ImmutableList.copyOf(rentable);
    }

    public List<ResidenceArea> getBuyableResidences() {
        return ImmutableList.copyOf(buyable);
    }

    public List<ResidenceArea> getCurrentlyRentedResidences() {
        return ImmutableList.copyOf(rentList);
    }

    public void setForRent(ResidenceArea res) {
        rentable.add(res);
    }

    public void removeFromRent(ResidenceArea res) {
        rentable.remove(res);
    }

    public void setRented(ResidenceArea res) {
        rentList.add(res);
    }

    public void evict(ResidenceArea res) {
        rentList.remove(res);
    }

    public void setForSale(ResidenceArea res) {
        buyable.add(res);
    }

    public void removeFromSale(ResidenceArea res) {
        buyable.remove(res);
    }

    public void checkRent() {
        Iterator<ResidenceArea> it = rentList.iterator();
        while (it.hasNext()) {
            if (!((MemoryResidenceArea) it.next()).checkRent()) {
                it.remove();
            }
        }
    }
}

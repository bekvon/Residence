package net.t00thpick1.residence.api;

import java.util.List;

import net.t00thpick1.residence.api.areas.ResidenceArea;

public interface EconomyManager {
    public List<ResidenceArea> getRentableResidences();
    public List<ResidenceArea> getBuyableResidences();
    public List<ResidenceArea> getCurrentlyRentedResidences();
    public void checkRent();
}

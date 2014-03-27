package net.t00thpick1.residence.api.areas;

public interface BuyableArea extends MarketableArea {
    public boolean isForSale();
    public void setForSale(int cost);
    public boolean buy(String buyer);
}

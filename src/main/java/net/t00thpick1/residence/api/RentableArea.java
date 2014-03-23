package net.t00thpick1.residence.api;

public interface RentableArea {
    public boolean isForRent();
    public void setForRent(int cost, long rentPeriod, boolean autoRenewEnabled);
    public boolean isForSale();
    public void setForSale(int cost);
    public void removeFromMarket();
    public int getCost();
    public long getRentPeriod();
    public boolean isAutoRenewEnabled();
    public void setAutoRenewEnabled(boolean autoRenew);


    public boolean buy(String buyer);
    public boolean rent(String renter, boolean isAutoRenew);


    public String getRenter();
    public boolean isAutoRenew();
    public void setAutoRenew(boolean autoRenew);
    public long getLastPaymentDate();
}

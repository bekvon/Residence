package net.t00thpick1.residence.api.areas;

public interface RentableArea extends MarketableArea {
    public boolean isForRent();
    public void setForRent(int cost, long rentPeriod, boolean autoRenewEnabled);
    public long getRentPeriod();
    public boolean isAutoRenewEnabled();
    public void setAutoRenewEnabled(boolean autoRenew);
    public boolean isRented();
    public boolean rent(String renter, boolean isAutoRenew);
    public String getRenter();
    public boolean isAutoRenew();
    public void setAutoRenew(boolean autoRenew);
    public long getLastPaymentDate();
    public void evict();
}

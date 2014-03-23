package net.t00thpick1.residence.api;


public interface ResidenceArea extends PermissionsArea, RentableArea {
    public String getOwner();
    public void setOwner(String owner);
    public void setPlayerFlag(String player, Flag flag, Boolean value);
}

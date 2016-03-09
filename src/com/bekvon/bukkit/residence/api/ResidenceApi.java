package com.bekvon.bukkit.residence.api;

import com.bekvon.bukkit.residence.Residence;

public class ResidenceApi {

    public static MarketBuyInterface getMarketBuyManager() {
	return Residence.getMarketBuyManagerAPI();
    }

    public static MarketRentInterface getMarketRentManager() {
	return Residence.getMarketRentManagerAPI();
    }

    public static ResidencePlayerInterface getPlayerManager() {
	return Residence.getPlayerManagerAPI();
    }

    public static ChatInterface getChatManager() {
	return Residence.getResidenceChatAPI();
    }

    public static ResidenceInterface getResidenceManager() {
	return Residence.getResidenceManagerAPI();
    }
}

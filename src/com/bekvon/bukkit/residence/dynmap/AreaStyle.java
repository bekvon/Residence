package com.bekvon.bukkit.residence.dynmap;

import com.bekvon.bukkit.residence.Residence;

class AreaStyle {
    String strokecolor;
    String forrentstrokecolor;
    String rentedstrokecolor;
    String forsalestrokecolor;
    double strokeopacity;
    int strokeweight;
    String fillcolor;
    double fillopacity;
    int y;

    AreaStyle() {
	strokecolor = Residence.getConfigManager().DynMapBorderColor;
	forrentstrokecolor = Residence.getConfigManager().DynMapFillForRent;
	rentedstrokecolor = Residence.getConfigManager().DynMapFillRented;
	forsalestrokecolor = Residence.getConfigManager().DynMapFillForSale;
	strokeopacity = Residence.getConfigManager().DynMapBorderOpacity;
	strokeweight = Residence.getConfigManager().DynMapBorderWeight;
	fillcolor = Residence.getConfigManager().DynMapFillColor;
	fillopacity = Residence.getConfigManager().DynMapFillOpacity;
	y = 64;
    }
}

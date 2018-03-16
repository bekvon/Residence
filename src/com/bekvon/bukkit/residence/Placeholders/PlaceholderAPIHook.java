package com.bekvon.bukkit.residence.Placeholders;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.Placeholders.Placeholder.CMIPlaceHolders;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PlaceholderAPIHook extends EZPlaceholderHook {

    private Residence plugin;

    public PlaceholderAPIHook(Residence plugin) {
	super(plugin, "residence");
	this.plugin = plugin;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
	CMIPlaceHolders placeHolder = CMIPlaceHolders.getByName(identifier);
	if (placeHolder == null) {
	    return null;
	}
	return plugin.getPlaceholderAPIManager().getValue(player, placeHolder);
    }

}

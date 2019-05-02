package com.bekvon.bukkit.residence.signsStuff;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.utils.Utils;

public class SignInfo {

    ConcurrentHashMap<String, Signs> AllSigns = new ConcurrentHashMap<String, Signs>();

    public SignInfo() {
    }

    public void setAllSigns(ConcurrentHashMap<String, Signs> AllSigns) {
	this.AllSigns.clear();
	this.AllSigns.putAll(AllSigns);
    }

    public ConcurrentHashMap<String, Signs> GetAllSigns() {
	return this.AllSigns;
    }

    public Signs getResSign(Location loc) {
	String l = Utils.convertLocToStringShort(loc);
	if (l == null)
	    return null;
	return this.AllSigns.get(l);
    }

    public void removeSign(Signs sign) {
	this.AllSigns.remove(sign);
    }

    public void addSign(Signs sign) {
	String loc = Utils.convertLocToStringShort(sign.GetLocation());
	if (loc == null)
	    return;
	this.AllSigns.put(loc, sign);
    }
}

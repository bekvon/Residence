package com.bekvon.bukkit.residence.signsStuff;

import java.util.ArrayList;
import java.util.List;

public class SignInfo {

    List<Signs> AllSigns = new ArrayList<Signs>();

    public SignInfo() {
    }

    public void setAllSigns(List<Signs> AllSigns) {
	this.AllSigns = AllSigns;
    }

    public List<Signs> GetAllSigns() {
	return this.AllSigns;
    }

    public void removeSign(Signs sign) {
	this.AllSigns.remove(sign);
    }

    public void addSign(Signs sign) {
	this.AllSigns.add(sign);
    }
}

package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
import java.util.Map.Entry;

public class MinimizeFlags {

    private HashMap<String, Boolean> flags = new HashMap<String, Boolean>();

    private int id = 0;

    public MinimizeFlags(int id, HashMap<String, Boolean> flags) {
	this.id = id;
	this.flags = flags;
    }

    public boolean same(HashMap<String, Boolean> flags) {
	if (flags.size() != this.flags.size())
	    return false;
	for (Entry<String, Boolean> one : flags.entrySet()) {
	    if (!this.flags.containsKey(one.getKey()))
		return false;
	    if (this.flags.get(one.getKey()) != one.getValue())
		return false;
	}
	return true;
    }

    public boolean add(HashMap<String, Boolean> flags) {
	if (!same(flags))
	    return false;
	this.flags = flags;
	return true;
    }

    public int getId() {
	return id;
    }

    public HashMap<String, Boolean> getFlags() {
	return flags;
    }

}

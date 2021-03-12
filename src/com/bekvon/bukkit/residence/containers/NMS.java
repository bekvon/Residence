package com.bekvon.bukkit.residence.containers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.CMIEffect;

public interface NMS {

    void playEffect(Player player, Location location, CMIEffect ef);
}

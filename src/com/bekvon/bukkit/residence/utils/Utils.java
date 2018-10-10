package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;

import cmiLib.ItemManager.CMIMaterial;

public class Utils {

    public Utils() {
    }

    public static String to24hourShort(Long ticks) {
	long years = ticks / 1000 / 60 / 60 / 24 / 365;
	ticks = ticks - (years * 1000 * 60 * 60 * 24 * 365);

	long days = ticks / 1000 / 60 / 60 / 24;
	ticks = ticks - (days * 1000 * 60 * 60 * 24);

	long hours = ticks / 1000 / 60 / 60;
	ticks = ticks - (hours * 1000 * 60 * 60);

	long minutes = ticks / 1000 / 60;
	ticks = ticks - (minutes * 1000 * 60);

	long sec = ticks / 1000;
	ticks = ticks - (sec * 1000);

	String time = "";

	if (years > 0)
	    time += years == 1 ? Residence.getInstance().getLM().getMessage(lm.info_oneYear, years) : Residence.getInstance().getLM().getMessage(lm.info_years, years);

	if (days > 0)
	    time += days == 1 ? Residence.getInstance().getLM().getMessage(lm.info_oneDay, days) : Residence.getInstance().getLM().getMessage(lm.info_day, days);

	if (hours > 0)
	    time += hours == 1 ? Residence.getInstance().getLM().getMessage(lm.info_oneHour, hours) : Residence.getInstance().getLM().getMessage(lm.info_hour, hours);

	if (minutes > 0)
	    time += Residence.getInstance().getLM().getMessage(lm.info_min, minutes);

	if (sec > 0)
	    time += Residence.getInstance().getLM().getMessage(lm.info_sec, sec);

	if (time.isEmpty())
	    time += Residence.getInstance().getLM().getMessage(lm.info_sec, 0);

	return time;
    }

    public static Block getTargetBlock(Player player, int distance, boolean ignoreNoneSolids) {
	return getTargetBlock(player, null, distance, ignoreNoneSolids);
    }

    public static Block getTargetBlock(Player player, int distance) {
	return getTargetBlock(player, null, distance, false);
    }

    public static Block getTargetBlock(Player player, Material lookingFor, int distance) {
	return getTargetBlock(player, lookingFor, distance, false);
    }

    public static Block getTargetBlock(Player player, Material lookingFor, int distance, boolean ignoreNoneSolids) {
	if (distance > 15 * 16)
	    distance = 15 * 16;
	if (distance < 1)
	    distance = 1;
	ArrayList<Block> blocks = new ArrayList<Block>();
	Iterator<Block> itr = new BlockIterator(player, distance);
	while (itr.hasNext()) {
	    Block block = itr.next();
	    blocks.add(block);
	    if (distance != 0 && blocks.size() > distance) {
		blocks.remove(0);
	    }
	    Material material = block.getType();

	    if (ignoreNoneSolids && !block.getType().isSolid())
		continue;

	    if (lookingFor == null) {
		if (!CMIMaterial.AIR.equals(material) && !CMIMaterial.CAVE_AIR.equals(material) && !CMIMaterial.VOID_AIR.equals(material)) {
		    break;
		}
	    } else {
		if (lookingFor.equals(material)) {
		    return block;
		}
	    }
	}
	return !blocks.isEmpty() ? blocks.get(blocks.size() - 1) : null;
    }
}

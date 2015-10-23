package com.bekvon.bukkit.residence.allNms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.BlockPistonRetractEvent;
import com.bekvon.bukkit.residence.NMS;
import com.bekvon.bukkit.residence.Residence;

public class v1_7_Couldron implements NMS {
    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
	List<Block> blocks = new ArrayList<Block>();
	blocks.add(event.getBlock());
	return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
	return (ent instanceof Horse || ent instanceof Bat || ent instanceof Snowman || ent instanceof IronGolem || ent instanceof Ocelot || ent instanceof Pig
	    || ent instanceof Sheep || ent instanceof Chicken || ent instanceof Wolf || ent instanceof Cow || ent instanceof Squid || ent instanceof Villager);
    }

    @Override
    public boolean isArmorStandEntity(EntityType ent) {
	return false;
    }

    @Override
    public boolean isArmorStandMaterial(Material material) {
	return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isCanUseEntity_BothClick(Material mat, Block block) {
	switch (mat) {
	case LEVER:
	case STONE_BUTTON:
	case WOOD_BUTTON:
	case WOODEN_DOOR:
	case TRAP_DOOR:
	case FENCE_GATE:
	case PISTON_BASE:
	case PISTON_STICKY_BASE:
	case DRAGON_EGG:
	    return true;
	default:
	    return Residence.getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getTypeId()));
	}
    }

    @Override
    public boolean isEmptyBlock(Block block) {
	switch (block.getType()) {
	case AIR:
	case WEB:
	case STRING:
	case WALL_SIGN:
	case SAPLING:
	case VINE:
	case TRIPWIRE_HOOK:
	case TRIPWIRE:
	case STONE_BUTTON:
	case WOOD_BUTTON:
	case PAINTING:
	case ITEM_FRAME:
	    return true;
	default:
	    break;
	}
	return false;
    }

    @Override
    public boolean isSpectator(GameMode mode) {
	return false;
    }

    @Override
    public void addDefaultFlags(Map<Material, String> matUseFlagList) {
    }

    @Override
    public int getOnlinePlayerAmount() {
	return Bukkit.getServer().getOnlinePlayers().size();
    }
}

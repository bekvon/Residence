package com.bekvon.bukkit.residence;

import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockPistonRetractEvent;

public interface NMS {

    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event);

    public boolean isAnimal(Entity ent);

    public boolean isArmorStandEntity(EntityType entityType);

    public boolean isArmorStandMaterial(Material material);

    public boolean isCanUseEntity_BothClick(Material mat, Block block);

    public boolean isEmptyBlock(Block block);

    public boolean isSpectator(GameMode mode);

    public void addDefaultFlags(Map<Material, String> matUseFlagList);

    public boolean isPlate(Material mat);
}

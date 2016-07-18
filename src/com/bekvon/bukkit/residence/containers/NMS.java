package com.bekvon.bukkit.residence.containers;

import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

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

    public boolean isMainHand(PlayerInteractEvent event);

    public Block getTargetBlock(Player player, int range);

    public ItemStack itemInMainHand(Player player);
    
    public boolean isChorusTeleport(TeleportCause tpcause);
    
    public boolean isBoat(Material mat);
}

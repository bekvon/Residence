package com.bekvon.bukkit.residence.allNms;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.residence.containers.NMS;

import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;

public class v1_7_R4 implements NMS {

    @Override
    public void playEffect(Player player, Location location, CMIEffect ef) {
	if (location == null || ef == null || location.getWorld() == null)
	    return;
	CraftPlayer cPlayer = (CraftPlayer) player;
	if (cPlayer.getHandle().playerConnection == null)
	    return;

	Effect effect = ef.getParticle().getEffect();
	if (effect == null)
	    return;
	PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(effect.name(), (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
	    (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());
	cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}

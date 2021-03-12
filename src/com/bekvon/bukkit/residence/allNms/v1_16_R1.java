package com.bekvon.bukkit.residence.allNms;

import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.craftbukkit.v1_16_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.cmiLib.CMIEffectManager.CMIParticle;
import com.bekvon.bukkit.residence.containers.NMS;

import net.minecraft.server.v1_16_R1.Packet;
import net.minecraft.server.v1_16_R1.PacketPlayOutWorldParticles;

public class v1_16_R1 implements NMS {

    @Override
    public void playEffect(Player player, Location location, CMIEffect ef) {
	if (location == null || ef == null || location.getWorld() == null)
	    return;

	CMIParticle effect = ef.getParticle();
	if (effect == null)
	    return;
	if (!effect.isParticle()) {
	    return;
	}

	org.bukkit.Particle particle = effect.getParticle();

	if (particle == null)
	    return;

	DustOptions dd = null;
	if (particle.equals(org.bukkit.Particle.REDSTONE))
	    dd = new org.bukkit.Particle.DustOptions(ef.getColor(), ef.getSize());

	Packet<?> packet = new PacketPlayOutWorldParticles(CraftParticle.toNMS(particle, dd), true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
	    (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());

	CraftPlayer cPlayer = (CraftPlayer) player;
	if (cPlayer.getHandle().playerConnection == null)
	    return;

	if (!location.getWorld().equals(cPlayer.getWorld()))
	    return;

	cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}

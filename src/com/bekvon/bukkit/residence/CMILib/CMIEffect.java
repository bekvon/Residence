package com.bekvon.bukkit.residence.CMILib;

import java.lang.reflect.Method;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.CMILib.CMIEffectManager.CMIParticle;

public class CMIEffect {

    private CMIParticle particle;
    private Color color = Color.fromBGR(0, 0, 200);
    private Vector offset = new Vector();
    private int size = 1;
    private int amount = 1;
    private float speed = 0;
    private Location loc;

    public CMIEffect(CMIParticle particle) {
	this.particle = particle;
    }

    public CMIParticle getParticle() {
	if (particle == null)
	    particle = CMIParticle.COLOURED_DUST;
	return particle;
    }

    public void setParticle(CMIParticle particle) {
	this.particle = particle;
    }

    public Color getColor() {
	return color;
    }

    public void setColor(Color color) {
	this.color = color;
    }

    public Vector getOffset() {
	return offset;
    }

    public void setOffset(Vector offset) {
	this.offset = offset;
    }

    public int getAmount() {
	return amount;
    }

    public void setAmount(int amount) {
	this.amount = amount;
    }

    public float getSpeed() {
	return speed;
    }

    public void setSpeed(float speed) {
	this.speed = speed;
    }

    public int getSize() {
	return size;
    }

    public void setSize(int size) {
	this.size = size;
    }

    public Location getLoc() {
	return loc;
    }

    public void setLoc(Location loc) {
	this.loc = loc;
    }

}

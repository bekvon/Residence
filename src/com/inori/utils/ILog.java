package com.inori.utils;

import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

/**
 * for debug log
 * 
 * @author InoriXu
 *
 */
public class ILog {
	
	private static Logger logger;
	private static String PREFIX = "[Residence] ";
	private static Boolean enable = false;
	
	static {
		logger = Residence.getServ().getLogger();
		//logger.setLevel(Level.INFO);
	}
	
	public static void log(String msg)
	{
		if(enable)
		{
			logger.info(PREFIX + msg);
		}		
	}
	
	public static void sendToPlayer(Player p, String msg)
	{
		if(enable)
		{
			p.sendMessage(PREFIX + msg);
		}
	}
}

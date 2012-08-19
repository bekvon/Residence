package com.inori.utils;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

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

	private static HashMap<PlayerMoveEvent, Integer> counter = new HashMap<PlayerMoveEvent, Integer>();
	public static void repeatCallEvent(PlayerMoveEvent event) {
		if(counter.containsKey(event))
    	{
    		Integer i = counter.get(event);
    		i++;
    		if(i < 100){
    			counter.put(event, i);
                Residence.getServ().getPluginManager().callEvent(event);    			
    		}
    		else
    			return;
    	}else{
			counter.put(event, 0);
            Residence.getServ().getPluginManager().callEvent(event);  
    	}
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

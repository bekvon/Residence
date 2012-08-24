/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.testing;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Administrator
 */
public class PerformanceTester {

    private static Thread t;
    public static void runHIGHLYDESTRUCTIVEToLiveServerPerformanceTest()
    {
        if(t==null)
        {
            Runnable r = new Runnable() {
                public void run() {
                    PerformanceTester.doTest();
                    t = null;
                }
            };
            t = new Thread(r);
            t.start();
        }
    }
    
    private static void doTest()
    {
        ResidenceManager res = Residence.getResidenceManager();
        World w = Residence.getServ().getWorlds().get(0);
        Random r = new Random(System.currentTimeMillis());
        long start;
        System.out.println("Creating 10000 random residences...");
        start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++)
        {
            while(res.getByName("res"+i) == null)
            {
                int basex = r.nextInt();
                int basey = r.nextInt(100);
                int basez = r.nextInt();
                res.addResidence("res"+i, "Test", new Location(w,basex,basey,basez), new Location(w,basex+1000,basey+100,basez+1000));
            }
        }
        System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
        System.out.println("Creating 0-9 subzones per residence...");
        start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++)
        {
            int count = r.nextInt(10);
            ClaimedResidence cres = res.getByName("res"+i);
            int basex = cres.getAreaArray()[0].getLowLoc().getBlockX();
            int basey = cres.getAreaArray()[0].getLowLoc().getBlockY();
            int basez = cres.getAreaArray()[0].getLowLoc().getBlockZ();
            for(int j = 0; j <= count; j++)
            {
                cres.addSubzone("sub"+j, new Location(w,basex,basey,basez), new Location(w,basex+9,basey+9,basez+9));
                basex = basex + 10;
                basey = basey + 10;
                basez = basez + 10;
            }
        }
        System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
        System.out.println("Running 10000 location checks using getByLoc()...");
        start = System.currentTimeMillis();
        int found = 0;
        for(int i = 0; i < 10000; i++)
        {
            if(res.getByLoc(new Location(w,r.nextInt(),r.nextInt(200),r.nextInt()))!=null)
                found++;
        }
        System.out.println("Hit " + found + " residences...");
        System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
        System.out.println("Running 10000 location checks using getNameByLoc()...");
        start = System.currentTimeMillis();
        found = 0;
        for(int i = 0; i < 10000; i++)
        {
            if(res.getNameByLoc(new Location(w,r.nextInt(),r.nextInt(200),r.nextInt()))!=null)
                found++;
        }
        System.out.println("Hit " + found + " residences...");
        System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
        found = 0;
        System.out.println("Performing name lookup on every possible subzone (90000 total) using getByName()...");
        start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                if(res.getByName("res"+i+"."+"sub"+j)!=null)
                    found++;
            }
        }
        System.out.println("Hit " + found + " subzones...");
        System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
        Player[] p = Residence.getServ().getOnlinePlayers();
        PluginManager pm = Residence.getServ().getPluginManager();
        if(p.length>0)
        {
            Player player = p[0];
            System.out.println("Fake breaking 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Event event = new BlockBreakEvent(w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt()), player);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
            System.out.println("Fake placing 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Block block = w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt(i));
                Event event = new org.bukkit.event.block.BlockPlaceEvent(block, block.getState(), block, player.getItemInHand(), player, true);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
            System.out.println("Fake punching (interacting using LEFT_CLICK_BLOCK) 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Block block = w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt(i));
                Event event = new org.bukkit.event.player.PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, player.getItemInHand(), block, BlockFace.SELF);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
            System.out.println("Fake right clicking (interacting using RIGHT_CLICK_BLOCK) 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Block block = w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt(i));
                Event event = new org.bukkit.event.player.PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), block, BlockFace.SELF);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
            System.out.println("Fake walking over (interacting using PHYSICAL) 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Block block = w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt(i));
                Event event = new org.bukkit.event.player.PlayerInteractEvent(player, Action.PHYSICAL, player.getItemInHand(), block, BlockFace.SELF);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
            
            System.out.println("Burning the world :D (ignite event testing) 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Block block = w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt(i));
                Event event = new org.bukkit.event.block.BlockIgniteEvent(block, BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL, player);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
            System.out.println("NUKING the world :D (explode event testing) 10000 blocks using player " + player.getName() + " as a test dummy :)");
            start = System.currentTimeMillis();
            for(int i = 0; i < 10000; i++)
            {
                Block block = w.getBlockAt(r.nextInt(), r.nextInt(200), r.nextInt(i));
                ArrayList<Block> b = new ArrayList<Block>();
                for(int j = 0; j < 16; j++)
                    b.add(block);
                Event event = new org.bukkit.event.entity.EntityExplodeEvent(player, block.getLocation(), b, 4);
                pm.callEvent(event);
            }
            System.out.println("Done..." + (System.currentTimeMillis() - start) + "ms");
        }
        else
            System.out.println("Found no online player to run event testing with :(");
        System.out.println("All done with testing... :)");
    }
}

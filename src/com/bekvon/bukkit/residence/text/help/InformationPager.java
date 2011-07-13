/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text.help;

import com.bekvon.bukkit.residence.Residence;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Administrator
 */
public class InformationPager {
    
    public static int linesPerPage=7;

    public static int getLinesPerPage()
    {
        return linesPerPage;
    }

    public static void setLinesPerPage(int lines)
    {
        linesPerPage = lines;
    }

    public static void printInfo(CommandSender sender, String title, String[] lines, int page)
    {

        InformationPager.printInfo(sender, title, Arrays.asList(lines), page);
    }

    public static void printInfo(CommandSender sender, String title, List<String> lines, int page) {
        int perPage = 6;
        int start = (page-1) * perPage;
        int end = start + perPage;
        int pagecount = (int) Math.ceil((double)lines.size()/(double)perPage);
        if(page>pagecount)
        {
            sender.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidPage"));
            return;
        }
        sender.sendMessage("§e---<§a"+title+"§e>---");
        sender.sendMessage("§e---<"+Residence.getLanguage().getPhrase("GenericPage","§a"+page+"§e.§a"+pagecount+"§e")+">---");
        for(int i = start; i < end; i ++)
        {
            if(lines.size()>i)
                sender.sendMessage("§a"+lines.get(i));
        }
        if(pagecount>page)
            sender.sendMessage("§7---<"+Residence.getLanguage().getPhrase("NextPage")+">---");
        else
            sender.sendMessage("§7-----------------------");
    }
}

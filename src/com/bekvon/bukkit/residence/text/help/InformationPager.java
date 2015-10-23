/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text.help;

import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.NewLanguage;
import com.bekvon.bukkit.residence.Residence;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Administrator
 */
public class InformationPager {

    public static int linesPerPage = 7;

    public static int getLinesPerPage() {
	return linesPerPage;
    }

    public static void setLinesPerPage(int lines) {
	linesPerPage = lines;
    }

    public static void printInfo(CommandSender sender, String title, String[] lines, int page) {
	InformationPager.printInfo(sender, title, Arrays.asList(lines), page);
    }

    public static void printInfo(CommandSender sender, String title, List<String> lines, int page) {
	int perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;
	int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidPage"));
	    return;
	}
	sender.sendMessage(NewLanguage.getMessage("Language.InformationPage.TopLine").replace("%1", title));
	sender.sendMessage(NewLanguage.getMessage("Language.InformationPage.Page").replace("%1", Residence.getLanguage().getPhrase("GenericPage", ChatColor.GREEN + String
	    .format("%d", page) + ChatColor.YELLOW + "|" + ChatColor.GREEN + pagecount + ChatColor.YELLOW)));
	for (int i = start; i < end; i++) {
	    if (lines.size() > i)
		sender.sendMessage(ChatColor.GREEN + lines.get(i));
	}
	if (pagecount > page)
	    sender.sendMessage(NewLanguage.getMessage("Language.InformationPage.NextPage").replace("%1", Residence.getLanguage().getPhrase("NextPage")));
	else
	    sender.sendMessage(NewLanguage.getMessage("Language.InformationPage.NoNextPage"));
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text.help;

import com.bekvon.bukkit.residence.Residence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class HelpEntry {
    protected String name;
    protected String desc;
    protected String[] lines;
    protected List<HelpEntry> subentrys;
    protected static int linesPerPage = 7;

    public HelpEntry(String entryname)
    {
        name = entryname;
        subentrys = new ArrayList<HelpEntry>();
        lines = new String[0];
    }

    public String getName() {
        if(name==null)
            return "";
        return name;
    }

    public void setName(String inname)
    {
        name = inname;
    }

    public void setDescription(String description)
    {
        desc = description;
    }
    public String getDescription()
    {
        if(desc==null)
            return "";
        return desc;
    }

    public int getLinesPerPage()
    {
        return linesPerPage;
    }

    public void setLinesPerPage(int lines)
    {
        linesPerPage = lines;
    }

    public void printHelp(CommandSender sender, int page) {
        List<String> helplines = this.getHelpData();
        int pagecount = (int) Math.ceil((double)helplines.size() / (double)linesPerPage);
        if (page > pagecount || page < 1) {
            sender.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidHelp"));
            return;
        }
        sender.sendMessage("§c"+Residence.getLanguage().getPhrase("HelpPageHeader","§e" + name + "§c.§e" + page + "§c.§e" + pagecount + "§c"));
        sender.sendMessage("§3"+Residence.getLanguage().getPhrase("Description")+": §a" + desc);
        int start = linesPerPage * (page - 1);
        int end = start + linesPerPage;
        boolean alternatecolor = false;
        for (int i = start; i < end; i++) {
            if (helplines.size() > i) {
                if(alternatecolor)
                {
                    sender.sendMessage("§e"+helplines.get(i));
                    alternatecolor = false;
                }
                else
                {
                    sender.sendMessage("§6"+helplines.get(i));
                    alternatecolor = true;
                }
            }
        }
        if(page<pagecount)
            sender.sendMessage("§7---<"+Residence.getLanguage().getPhrase("NextPage")+">---");
        else
            sender.sendMessage("§7-----------------------");
    }

    public void printHelp(CommandSender sender, int page, String path)
    {
        HelpEntry subEntry = this.getSubEntry(path);
        if(subEntry!=null)
        {
            subEntry.printHelp(sender, page);
        }
        else
        {
            sender.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidHelp"));
        }
    }

    private List<String> getHelpData()
    {
        List<String> helplines = new ArrayList<String>();
        helplines.addAll(Arrays.asList(lines));
        if(subentrys.size()>0)
            helplines.add("§d---"+Residence.getLanguage().getPhrase("SubCommands")+"---");
        for(HelpEntry entry : subentrys)
        {
            helplines.add("§a"+entry.getName() + "§e - " + entry.getDescription());
        }
        return helplines;
    }

    public boolean containesEntry(String name)
    {
        return this.getSubEntry(name)!=null;
    }

    public HelpEntry getSubEntry(String name)
    {
        String[] split = name.split("\\.");
        HelpEntry entry = this;
        for(String entryname : split)
        {
            entry = entry.findSubEntry(entryname);
            if(entry == null)
                return null;
        }
        return entry;
    }

    private HelpEntry findSubEntry(String name)
    {
        for(HelpEntry entry : subentrys)
        {
            if(entry.getName().equalsIgnoreCase(name))
                return entry;
        }
        return null;
    }

    public void addSubEntry(HelpEntry entry)
    {
        if(!subentrys.contains(entry))
        {
            subentrys.add(entry);
        }
    }

    public void removeSubEntry(HelpEntry entry)
    {
        if(subentrys.contains(entry))
        {
            subentrys.remove(entry);
        }
    }

    public int getSubEntryCount()
    {
        return subentrys.size();
    }

    public static HelpEntry parseHelp(Configuration node, String key)
    {
        String split[] = key.split("\\.");
        String thisname = split[split.length-1];
        HelpEntry entry = new HelpEntry(thisname);
        List<String> keys = node.getKeys(key);
        if(keys!=null)
        {
            if(keys.contains("Info"))
            {
                List<String> stringList = node.getStringList(key + ".Info", null);
                if(stringList != null)
                {
                    entry.lines = new String[stringList.size()];
                    for(int i = 0; i < stringList.size(); i++)
                    {
                        entry.lines[i] = "- " + stringList.get(i);
                    }
                }
            }
            if(keys.contains("Description"))
            {
                entry.desc = node.getString(key + ".Description");
            }
            if(keys.contains("SubCommands"))
            {
                List<String> subcommandkeys = node.getKeys(key + ".SubCommands");
                for(String subkey : subcommandkeys)
                {
                    entry.subentrys.add(HelpEntry.parseHelp(node, key+".SubCommands."+subkey));
                }
            }
        }
        return entry;
    }

}

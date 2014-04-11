package net.t00thpick1.residence;

import net.t00thpick1.residence.locale.LocaleLoader;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.base.Joiner;

public class HelpManager {
    private static FileConfiguration file;

    public static void init(FileConfiguration file) {
        HelpManager.file = file;
        if (!file.isConfigurationSection("res")) {
            file.createSection("res");
        }
    }

    public static void help(CommandSender sender, String[] args) {
        int i = 0;
        ConfigurationSection section = file.getConfigurationSection("res");
        while (!args[i].equalsIgnoreCase("?")) {
            if (!section.isConfigurationSection("Subcommands")) {
                sender.sendMessage(LocaleLoader.getString("Commands.Help.NotFound"));
                return;
            }
            section = section.getConfigurationSection("Subcommands");
            if (!section.isConfigurationSection(args[i].toLowerCase())) {
                sender.sendMessage(LocaleLoader.getString("Commands.Help.NotFound"));
                return;
            }
            section = section.getConfigurationSection(args[i].toLowerCase());
            i++;
        }
        int page = 0;
        if (i < args.length - 1) {
            try {
                page = Integer.parseInt(args[args.length - 1]);
                if (page <= 0) {
                    throw new Exception();
                }
            } catch (Exception ex) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[args.length - 1]));
                return;
            }
            page--;
        }
        sender.sendMessage(section.getStringList("Description").toArray(new String[0]));
        if (section.isConfigurationSection("Subcommands")) {
            sender.sendMessage(LocaleLoader.getString("Commands.Help.Subcommands", page+1));
            String[] subcommands = section.getConfigurationSection("Subcommands").getKeys(false).toArray(new String[0]);
            for (int j = 0; j < 8; j++) {
                int index = (page*8) + j;
                if (index < subcommands.length) {
                    args[args.length - 1] = subcommands[index];
                    sender.sendMessage("/res " + Joiner.on(" ").join(args));
                }
            }
        }
    }

}

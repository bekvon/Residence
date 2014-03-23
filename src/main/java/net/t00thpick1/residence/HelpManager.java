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
        while (i < args.length - 1) {
            if (!section.isConfigurationSection(args[i].toLowerCase())) {
                sender.sendMessage(LocaleLoader.getString("Commands.Help.NotFound"));
                return;
            }
            section = section.getConfigurationSection(args[i].toLowerCase());
        }
        sender.sendMessage(section.getString("Description"));
        if (section.isConfigurationSection("Subcommands")) {
            for (String subcommand : section.getConfigurationSection("Subcommands").getKeys(false)) {
                args[args.length - 1] = subcommand;
                sender.sendMessage("/res" + Joiner.on(" ").join(args) + subcommand);
            }
        }
    }

}

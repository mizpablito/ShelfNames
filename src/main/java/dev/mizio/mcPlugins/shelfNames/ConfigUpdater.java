package dev.mizio.mcPlugins.shelfNames;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ConfigUpdater {

    private ConfigUpdater() {}

    public static List<String> addMissingKeys(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        YamlConfiguration defaults = loadBundledDefaults(plugin);
        if (defaults == null) {
            return List.of();
        }

        List<String> added = merge(config, defaults);

        if (!added.isEmpty()) {
            // saveConfig() zapisuje wyłącznie realne wartości (bez domyślnych)
            plugin.saveConfig();
            plugin.getLogger().warning("config.yml updated, added missing options: " + String.join(", ", leafPaths(defaults, added)));
        }

        return added;
    }

    static List<String> merge(FileConfiguration config, YamlConfiguration defaults) {
        List<String> added = new ArrayList<>();

        for (String path : defaults.getKeys(true)) {
            if (config.contains(path, true)) {
                continue;
            }

            if (defaults.isConfigurationSection(path)) {
                config.createSection(path);
            } else {
                config.set(path, defaults.get(path));
            }

            config.setComments(path, defaults.getComments(path));
            config.setInlineComments(path, defaults.getInlineComments(path));
            added.add(path);
        }

        return added;
    }

    private static List<String> leafPaths(ConfigurationSection defaults, List<String> paths) {
        return paths.stream().filter(p -> !defaults.isConfigurationSection(p)).toList();
    }

    private static YamlConfiguration loadBundledDefaults(JavaPlugin plugin) {
        try (InputStream in = plugin.getResource("config.yml")) {
            if (in == null) {
                return null;
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read bundled config.yml, skipping config update: " + e.getMessage());
            return null;
        }
    }
}

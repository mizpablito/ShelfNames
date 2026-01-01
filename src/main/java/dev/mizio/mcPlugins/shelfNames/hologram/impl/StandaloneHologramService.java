package dev.mizio.mcPlugins.shelfNames.hologram.impl;

import dev.mizio.mcPlugins.shelfNames.MainShelfNames;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramHandle;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StandaloneHologramService implements HologramService {

    private final MainShelfNames plugin;
    private final Map<UUID, StandaloneHandle> holograms = new HashMap<>();

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SECTION =
            LegacyComponentSerializer.legacySection();     // §a
    private static final LegacyComponentSerializer LEGACY_AMPERSAND =
            LegacyComponentSerializer.legacyAmpersand();   // &a

    public StandaloneHologramService(MainShelfNames plugin) {
        this.plugin = plugin;
    }

    @Override
    public HologramHandle getOrCreate(Player player) {
        return holograms.computeIfAbsent(
                player.getUniqueId(),
                id -> new StandaloneHandle(player, plugin)
        );
    }

    @Override
    public void remove(Player player) {
        StandaloneHandle handle = holograms.remove(player.getUniqueId());
        if (handle != null) handle.remove();
    }

    @Override
    public void removeAll() {
        holograms.values().forEach(StandaloneHandle::remove);
        holograms.clear();
    }

    // ------------------------------------------------------------

    private static class StandaloneHandle implements HologramHandle {

        private final MainShelfNames plugin;
        private final TextDisplay display;

        StandaloneHandle(Player player, MainShelfNames plugin) {
            this.plugin = plugin;

            World world = player.getWorld();
            Location loc = player.getLocation();

            this.display = world.spawn(loc, TextDisplay.class, td -> {

                td.setText("");
                td.setPersistent(false);

                td.setShadowed(plugin.getPluginConfig().isStandaloneTextShadow());

                td.setAlignment(TextDisplay.TextAlignment.valueOf(
                        plugin.getPluginConfig().getStandaloneTextAlignment()
                ));

                td.setBillboard(
                        plugin.getPluginConfig().isHologramFixedPosition()
                                ? Display.Billboard.FIXED
                                : Display.Billboard.CENTER
                );

                if (!plugin.getPluginConfig().isStandaloneDefaultBackground()) {
                    td.setBackgroundColor(
                            plugin.getPluginConfig().getStandaloneBackgroundARGB()
                    );
                }

                float scale = plugin.getPluginConfig().getHologramScale();

                td.setTransformation(new Transformation(
                        new Vector3f(0f, 0f, 0f),
                        new org.joml.Quaternionf(),
                        new Vector3f(scale, scale, scale),
                        new org.joml.Quaternionf()
                ));

                if (plugin.getPluginConfig().isOnlyOnePlayer()) {
                    td.setVisibleByDefault(false);
                }
            });

            if (plugin.getPluginConfig().isOnlyOnePlayer()) {
                player.showEntity(plugin, display);
            }
        }

        // ------------------------------------------------------------

        private Component parseLine(String raw) {
            if (raw.indexOf('§') != -1) {
                return LEGACY_SECTION.deserialize(raw);
            }

            if (raw.indexOf('&') != -1) {
                return LEGACY_AMPERSAND.deserialize(raw);
            }

            return MINI_MESSAGE.deserialize(raw);
        }

        private Component parseLines(List<String> lines) {
            Component result = Component.empty();

            for (int i = 0; i < lines.size(); i++) {
                result = result.append(parseLine(lines.get(i)));

                if (i < lines.size() - 1) {
                    result = result.append(Component.newline());
                }
            }

            return result;
        }

        // ------------------------------------------------------------

        @Override
        public void update(Player player, Location location, List<String> lines) {
            display.teleport(location);
            display.text(parseLines(lines));

            if (plugin.getPluginConfig().isOnlyOnePlayer()) {
                player.showEntity(plugin, display);
            }
        }

        @Override
        public void remove() {
            display.remove();
        }
    }
}

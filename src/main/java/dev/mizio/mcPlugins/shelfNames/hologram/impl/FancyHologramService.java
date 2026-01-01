package dev.mizio.mcPlugins.shelfNames.hologram.impl;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.data.property.Visibility;
import de.oliver.fancyholograms.api.hologram.Hologram;
import dev.mizio.mcPlugins.shelfNames.MainShelfNames;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramHandle;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FancyHologramService implements HologramService {

    private final MainShelfNames plugin;
    private final HologramManager manager;
    private final Map<UUID, FancyHandle> holograms = new HashMap<>();
    private final boolean fixed;

    public FancyHologramService(MainShelfNames plugin) {
        this.plugin = plugin;
        this.manager = FancyHologramsPlugin.get().getHologramManager();
        this.fixed = plugin.getPluginConfig().isHologramFixedPosition();
    }

    private void checkExistingHolograms() {
        //TODO:
        // - sprawdzić w API FH czy istnieją hologramy z nazwą od "shelf_"
        //   jeśli tak, to wrzuć info do konsoli do wykasowania przez Admina
    }

    @Override
    public HologramHandle getOrCreate(Player player) {
        return holograms.computeIfAbsent(
                player.getUniqueId(),
                id -> new FancyHandle(player, manager, plugin, fixed)
        );
    }

    @Override
    public void remove(Player player) {
        FancyHandle handle = holograms.remove(player.getUniqueId());
        if (handle != null) handle.remove();
    }

    @Override
    public void removeAll() {
        holograms.values().forEach(FancyHandle::remove);
        holograms.clear();
    }

    private static class FancyHandle implements HologramHandle {

        private final HologramManager manager;
        private final Hologram hologram;
        private final TextHologramData data;

        FancyHandle(Player player, HologramManager manager, MainShelfNames plugin, boolean fixed) {
            this.manager = manager;

            Location loc = player.getLocation();

            this.data = new TextHologramData("shelf_" + player.getUniqueId(), loc);
            this.data.setText(List.of("", "", ""));
            this.data.setPersistent(false);
            this.data.setTextShadow(plugin.getPluginConfig().isFancyHologramsTextShadow());
            this.data.setTextAlignment(TextDisplay.TextAlignment.valueOf(plugin.getPluginConfig().getFancyHologramsTextAlignment()));
            this.data.setShadowRadius(0);//cień na ziemi
            float scale = plugin.getPluginConfig().getHologramScale();
            this.data.setScale(new Vector3f(scale, scale, scale));

            if (plugin.getPluginConfig().isOnlyOnePlayer())
                this.data.setVisibility(Visibility.MANUAL);

            if (!plugin.getPluginConfig().isFancyHologramsDefaultBackground())
                this.data.setBackground(plugin.getPluginConfig().getFancyHologramsBackgroundARGB());

            this.data.setBillboard(
                    fixed ? Billboard.FIXED : Billboard.CENTER
            );

            this.hologram = manager.create(data);
            manager.addHologram(hologram);
        }

        @Override
        public void update(Player player, Location location, List<String> lines) {
            hologram.getData().setLocation(location);
            data.setText(lines);

            hologram.forceShowHologram(player);
            hologram.forceUpdate();
        }

        @Override
        public void remove() {
            manager.removeHologram(hologram);
        }
    }
}

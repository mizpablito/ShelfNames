package dev.mizio.mcPlugins.shelfNames.hologram.impl;

import com.fancyinnovations.fancyholograms.api.FancyHolograms;
import com.fancyinnovations.fancyholograms.api.HologramRegistry;
import com.fancyinnovations.fancyholograms.api.data.TextHologramData;
import com.fancyinnovations.fancyholograms.api.data.builder.TextHologramBuilder;
import com.fancyinnovations.fancyholograms.api.data.property.Visibility;
import com.fancyinnovations.fancyholograms.api.hologram.Hologram;
import dev.mizio.mcPlugins.shelfNames.MainShelfNames;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramHandle;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Integracja z FancyHolograms 3.x (API {@code com.fancyinnovations.fancyholograms.api}).
 * Eksperymentalna - v3 jest w fazie alfa i API może się jeszcze zmieniać.
 * <p>
 * Różnice względem v2 ({@link FancyHologramService}): zamiast {@code HologramManager}
 * jest {@code HologramRegistry}, hologramy buduje się przez {@code TextHologramBuilder},
 * a "force show" zastępuje bezpośrednie {@code Hologram#spawnTo/updateFor}.
 */
public class Fancy3HologramService implements HologramService {

    private final MainShelfNames plugin;
    private final HologramRegistry registry;
    // ConcurrentHashMap: getOrCreate/remove wołane równolegle z EntityScheduler-owych
    // zadań różnych graczy (Folia).
    private final Map<UUID, Fancy3Handle> holograms = new ConcurrentHashMap<>();

    public Fancy3HologramService(MainShelfNames plugin) {
        this.plugin = plugin;
        this.registry = FancyHolograms.get().getRegistry();
    }

    @Override
    public HologramHandle getOrCreate(Player player) {
        return holograms.computeIfAbsent(
                player.getUniqueId(),
                id -> new Fancy3Handle(player, registry, plugin)
        );
    }

    @Override
    public void remove(Player player) {
        Fancy3Handle handle = holograms.remove(player.getUniqueId());
        if (handle != null) handle.remove();
    }

    @Override
    public void removeAll() {
        holograms.values().forEach(Fancy3Handle::remove);
        holograms.clear();
    }

    private static class Fancy3Handle implements HologramHandle {

        private final MainShelfNames plugin;
        private final HologramRegistry registry;
        private final Player player;
        private final Hologram hologram;
        private final TextHologramData data;

        Fancy3Handle(Player player, HologramRegistry registry, MainShelfNames plugin) {
            this.plugin = plugin;
            this.registry = registry;
            this.player = player;

            var config = plugin.getPluginConfig();
            float scale = config.getHologramScale();

            // metody specyficzne dla tekstu muszą iść przed metodami z HologramBuilder
            // (te zwracają już bazowy typ buildera)
            TextHologramBuilder builder = TextHologramBuilder.create("shelf_" + player.getUniqueId(), player.getLocation())
                    .text(List.of("", "", ""))
                    .textShadow(config.isFancyHologramsTextShadow())
                    .textAlignment(TextDisplay.TextAlignment.valueOf(config.getFancyHologramsTextAlignment()));

            if (!config.isFancyHologramsDefaultBackground()) {
                builder.background(config.getFancyHologramsBackgroundARGB());
            }

            builder.persistent(false)
                    .shadowRadius(0f) // cień na ziemi
                    .scale(scale, scale, scale)
                    .billboard(config.isHologramFixedPosition() ? Billboard.FIXED : Billboard.CENTER)
                    // v3 sprawdza dystans do gracza przy każdym odświeżeniu widoczności
                    // - musi obejmować cały zasięg raytrace'u
                    .visibilityDistance(config.getRayTraceBlocksMaxDistance() + 16);

            if (config.isOnlyOnePlayer()) {
                // widoczny tylko dla graczy, dla których wywołano spawnTo (viewers)
                builder.visibility(Visibility.MANUAL);
            }

            this.hologram = builder.buildAndRegister();
            this.data = hologram.getData(TextHologramData.class);
        }

        @Override
        public void update(Player player, Location location, List<String> lines) {
            data.setLocation(location);
            data.setText(lines);

            // Wołane z EntityScheduler-a danego gracza, więc można od razu wysłać pakiety.
            // spawnTo() samo woła updateFor(); czekanie na cykliczny task FH (1 s)
            // dałoby zauważalne opóźnienie przy zmianie półki.
            if (hologram.isViewer(player)) {
                hologram.updateFor(player);
            } else {
                hologram.spawnTo(player);
            }
        }

        @Override
        public void remove() {
            Runnable unregister = () -> registry.unregister(hologram);

            // unregister() despawnuje hologram u graczy (czyta ich lokalizację), więc
            // na Folii musi iść przez scheduler gracza. Gdy plugin się wyłącza
            // (scheduler odmówi przyjęcia zadania) albo gracz już zniknął - wykonaj od razu.
            if (plugin.isEnabled()) {
                player.getScheduler().execute(plugin, unregister, unregister, 1L);
            } else {
                unregister.run();
            }
        }
    }
}

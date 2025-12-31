package dev.mizio.mcPlugins.shelfNames.hologram.impl;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramHandle;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

import java.util.*;

public class FancyHologramService implements HologramService {

    private final HologramManager manager;
    private final Map<UUID, FancyHandle> holograms = new HashMap<>();

    public FancyHologramService() {
        this.manager = FancyHologramsPlugin.get().getHologramManager();
    }

    @Override
    public HologramHandle getOrCreate(Player player) {
        return holograms.computeIfAbsent(player.getUniqueId(), id -> new FancyHandle(player, manager));
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

        FancyHandle(Player player, HologramManager manager) {
            this.manager = manager;

            Location loc = player.getLocation();

            this.data = new TextHologramData("shelf_" + player.getUniqueId(), loc);
            this.data.setText(List.of("", "", ""));
            this.data.setPersistent(false);
            this.data.setScale(new Vector3f(0.75f, 0.75f, 0.75f));

            this.hologram = manager.create(data);
            manager.addHologram(hologram);

            hologram.queueUpdate();                      // bo jest registered w managerze
        }

        @Override
        public void update(Location location, List<String> lines) {
            // location ustawiamy w DATA (nie na Hologram)
            hologram.getData().setLocation(location);

            String l0 = lines.size() > 0 ? lines.get(0) : "";
            String l1 = lines.size() > 1 ? lines.get(1) : "";
            String l2 = lines.size() > 2 ? lines.get(2) : "";

            data.setText(List.of(l0, l1, l2));

            hologram.queueUpdate();                      // odśwież po zmianie danych
        }

        @Override
        public void remove() {
            manager.removeHologram(hologram);
        }
    }
}

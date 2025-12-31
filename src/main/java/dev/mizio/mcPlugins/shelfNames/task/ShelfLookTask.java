package dev.mizio.mcPlugins.shelfNames.task;

import dev.mizio.mcPlugins.shelfNames.hologram.HologramHandle;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfCache;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Shelf;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.util.List;

public class ShelfLookTask implements Runnable {

    private final JavaPlugin plugin;
    private final HologramService holograms;
    private final ShelfCache cache;

    public ShelfLookTask(JavaPlugin plugin, HologramService holograms, ShelfCache cache) {
        this.plugin = plugin;
        this.holograms = holograms;
        this.cache = cache;
    }

    @Override
    public void run() {
        int maxDistance = plugin.getConfig().getInt("max-distance", 5);
        double offsetY = plugin.getConfig().getDouble("holograms.offset-y", 1.2);

        for (Player player : Bukkit.getOnlinePlayers()) {

            // 1️⃣ Offline → czyścimy
            if (!player.isOnline()) {
                holograms.remove(player);
                cache.clear(player);
                continue;
            }

            // 2️⃣ Dead → skip (nie czyścimy)
            if (player.isDead()) {
                continue;
            }

            RayTraceResult result = player.rayTraceBlocks(maxDistance);

            // 3️⃣ Nic nie trafiono → czyścimy
            if (result == null || result.getHitBlock() == null) {
                holograms.remove(player);
                cache.clear(player);
                continue;
            }

            Block block = result.getHitBlock();

            // OFICJALNY, TANI CHECK
            if (!Tag.WOODEN_SHELVES.isTagged(block.getType())) {
                holograms.remove(player);
                cache.clear(player);
                continue;
            }

            // TEN SAM BLOK → SKIP
            if (cache.isSameBlock(player, block)) {
                continue;
            }

            // DROGI KOD DOPIERO TERAZ
            Shelf shelf = (Shelf) block.getState();


            Location shelfLoc = block.getLocation();
            ShelfSnapshot snapshot = ShelfSnapshot.fromShelf(
                    shelf,
                    plugin.getConfig().getBoolean("only-custom-names", true)
            );

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (!cache.hasChanged(player, shelfLoc, snapshot)) return;

                cache.update(player, shelfLoc, snapshot);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    HologramHandle holo = holograms.getOrCreate(player);

                    holo.update(
                            shelfLoc.clone().add(0.5, offsetY, 0.5),
                            List.of(snapshot.s0(), snapshot.s1(), snapshot.s2())
                    );
                });
            });
        }
    }
}

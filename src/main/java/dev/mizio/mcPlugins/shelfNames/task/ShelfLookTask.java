package dev.mizio.mcPlugins.shelfNames.task;

import dev.mizio.mcPlugins.shelfNames.MainShelfNames;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramHandle;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfCache;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Shelf;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.List;

public class ShelfLookTask implements Runnable {

    private final MainShelfNames plugin;
    private final HologramService holograms;
    private final ShelfCache cache;

    public ShelfLookTask(MainShelfNames plugin, HologramService holograms, ShelfCache cache) {
        this.plugin = plugin;
        this.holograms = holograms;
        this.cache = cache;
    }

    @Override
    public void run() {
        int maxDistance = plugin.getPluginConfig().getRayTraceBlocksMaxDistance();
        double offsetY = plugin.getPluginConfig().getHologramOffsetY();
        double forwardOffset = plugin.getPluginConfig().getHologramForwardOffset();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!player.isOnline()) {
                holograms.remove(player);
                cache.clear(player);
                continue;
            }

            if (player.isDead()) continue;

            RayTraceResult result = player.rayTraceBlocks(maxDistance);
            if (result == null || result.getHitBlock() == null) {
                holograms.remove(player);
                cache.clear(player);
                continue;
            }

            Block block = result.getHitBlock();
            if (!Tag.WOODEN_SHELVES.isTagged(block.getType())) {
                holograms.remove(player);
                cache.clear(player);
                continue;
            }

            if (cache.isSameBlock(player, block)) continue;

            Shelf shelf = (Shelf) block.getState();
            Location shelfLoc = block.getLocation();

            ShelfSnapshot snapshot = ShelfSnapshot.fromShelf(
                    shelf,
                    plugin.getPluginConfig().isOnlyCustomNames()
            );

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (!cache.hasChanged(player, shelfLoc, snapshot)) return;
                cache.update(player, shelfLoc, snapshot);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    HologramHandle holo = holograms.getOrCreate(player);

                    BlockFace facing = BlockFace.SOUTH;
                    if (block.getBlockData() instanceof Directional d) {
                        facing = d.getFacing();
                    }

                    float yaw = switch (facing) {
                        case SOUTH -> 0f;
                        case WEST  -> 90f;
                        case NORTH -> 180f;
                        case EAST  -> -90f;
                        default -> 0f;
                    };

                    Location holoLoc = shelfLoc.clone()
                            .add(0.5, offsetY, 0.5)
                            .add(facing.getDirection().multiply(forwardOffset));

                    holoLoc.setYaw(yaw);

                    holo.update(
                            player,
                            holoLoc,
                            List.of(snapshot.s0(), snapshot.s1(), snapshot.s2())
                    );
                });
            });
        }
    }
}

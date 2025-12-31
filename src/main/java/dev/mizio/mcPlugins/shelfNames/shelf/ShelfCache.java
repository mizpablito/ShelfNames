package dev.mizio.mcPlugins.shelfNames.shelf;

import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShelfCache {

    private final Map<UUID, ShelfSnapshot> snapshots = new HashMap<>();
    private final Map<UUID, Location> locations = new HashMap<>();
    private final Map<UUID, BlockKey> lastBlock = new HashMap<>();


    public boolean hasChanged(Player player, Location loc, ShelfSnapshot snap) {
        UUID id = player.getUniqueId();
        ShelfSnapshot prevSnap = snapshots.get(id);
        Location prevLoc = locations.get(id);

        if (prevSnap == null || prevLoc == null) return true;
        return !snap.equals(prevSnap) || !loc.equals(prevLoc);
    }

    public void update(Player player, Location loc, ShelfSnapshot snap) {
        UUID id = player.getUniqueId();
        snapshots.put(id, snap);
        locations.put(id, loc);
    }

    public boolean isSameBlock(Player player, Block block) {
        UUID id = player.getUniqueId();
        BlockKey key = BlockKey.from(block);
        BlockKey prev = lastBlock.get(id);

        if (key.equals(prev)) return true;

        lastBlock.put(id, key);
        return false;
    }

    public void clear(Player player) {
        UUID id = player.getUniqueId();
        snapshots.remove(id);
        locations.remove(id);
        lastBlock.remove(id);
    }

}

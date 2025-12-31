package dev.mizio.mcPlugins.shelfNames.shelf;

import org.bukkit.block.Block;

public record BlockKey(String world, int x, int y, int z) {

    public static BlockKey from(Block block) {
        return new BlockKey(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }
}

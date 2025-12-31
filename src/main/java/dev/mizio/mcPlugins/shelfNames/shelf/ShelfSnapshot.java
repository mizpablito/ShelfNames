package dev.mizio.mcPlugins.shelfNames.shelf;

import dev.mizio.mcPlugins.shelfNames.util.ItemNameUtil;
import org.bukkit.block.Shelf;
import org.bukkit.inventory.Inventory;

public record ShelfSnapshot(String s0, String s1, String s2) {

    public static ShelfSnapshot fromShelf(Shelf shelf, boolean onlyCustomNames) {
        Inventory inv = shelf.getInventory();
        return new ShelfSnapshot(
                ItemNameUtil.getName(inv.getItem(0), onlyCustomNames),
                ItemNameUtil.getName(inv.getItem(1), onlyCustomNames),
                ItemNameUtil.getName(inv.getItem(2), onlyCustomNames)
        );
    }
}

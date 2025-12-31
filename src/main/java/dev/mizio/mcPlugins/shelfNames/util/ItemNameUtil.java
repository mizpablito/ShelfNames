package dev.mizio.mcPlugins.shelfNames.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemNameUtil {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    public static String getName(ItemStack item, boolean onlyCustomNames) {
        if (item == null || item.getType().isAir()) return "";

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return onlyCustomNames ? "" : item.getType().name();
        }

        Component displayName = meta.displayName();
        return displayName == null ? "" : LEGACY.serialize(displayName);
    }

}

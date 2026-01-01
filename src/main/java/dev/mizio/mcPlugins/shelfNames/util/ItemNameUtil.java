package dev.mizio.mcPlugins.shelfNames.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemNameUtil {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    // Legacy reset (§r) – jedyny pewny reset stylu
    private static final String LEGACY_RESET = "§r";

    public static String getName(ItemStack item, boolean onlyCustomNames) {
        if (item == null || item.getType().isAir()) {
            return "";
        }

        ItemMeta meta = item.getItemMeta();

        // custom display name
        if (meta != null && meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName == null) {
                return "";
            }

            // reset stylu po nazwie
            return LEGACY.serialize(displayName) + LEGACY_RESET;
        }

        // tylko custom names
        if (onlyCustomNames) {
            return "";
        }

        // domyślna nazwa z klienta (TRANSLATABLE)
        Component vanillaName = Component.translatable(
                item.getType().translationKey()
        );

        // reset stylu po nazwie
        return LEGACY.serialize(vanillaName) + LEGACY_RESET;
    }
}

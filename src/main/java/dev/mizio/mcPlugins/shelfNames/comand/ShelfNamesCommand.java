package dev.mizio.mcPlugins.shelfNames.comand;

import dev.mizio.mcPlugins.shelfNames.MainShelfNames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * <pre>
 * /shelfnames            → informacje o pluginie (nazwa, wersja, GitHub) - dla każdego
 * /shelfnames info       → alias dla powyższego
 * /shelfnames clear      → usuwa wszystkie hologramy pluginu   (shelfnames.admin)
 * /shelfnames reload     → restart pluginu: hologramy + config + komponenty (shelfnames.admin)
 * </pre>
 * Gracz bez uprawnienia {@code shelfnames.admin} może wykonać wyłącznie
 * komendę główną (informacyjną).
 */
public class ShelfNamesCommand implements TabExecutor {

    public static final String PERMISSION_ADMIN = "shelfnames.admin";

    private static final String FALLBACK_URL = "https://github.com/mizpablito/ShelfNames";
    private static final List<String> ADMIN_SUBCOMMANDS = List.of("info", "clear", "reload");

    private final MainShelfNames plugin;

    public ShelfNamesCommand(MainShelfNames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "info";

        switch (sub) {
            case "info" -> sendInfo(sender);

            case "clear" -> {
                if (denyIfNoAdmin(sender)) return true;
                plugin.getHologramService().removeAll();
                sender.sendMessage(prefix().append(Component.text(
                        "Usunięto wszystkie hologramy utworzone przez ShelfNames.", NamedTextColor.GREEN)));
            }

            case "reload" -> {
                if (denyIfNoAdmin(sender)) return true;
                plugin.reloadRuntime();
                sender.sendMessage(prefix().append(Component.text(
                        "Przeładowano plugin.",
                        NamedTextColor.GREEN)));
            }

            default -> {
                if (denyIfNoAdmin(sender)) return true;
                sendUsage(sender);
            }
        }
        return true;
    }

    /**
     * @return {@code true} gdy nadawca nie ma uprawnień admina (i komunikat został wysłany).
     */
    private boolean denyIfNoAdmin(CommandSender sender) {
        if (sender.hasPermission(PERMISSION_ADMIN)) {
            return false;
        }
        sender.sendMessage(prefix().append(Component.text(
                "Brak uprawnień do tej komendy.", NamedTextColor.RED)));
        return true;
    }

    private void sendInfo(CommandSender sender) {
        var meta = plugin.getPluginMeta();
        String url = meta.getWebsite() != null ? meta.getWebsite() : FALLBACK_URL;

        sender.sendMessage(prefix()
                .appendNewline()
                .append(Component.text(meta.getName(), NamedTextColor.WHITE))
                .append(Component.text(" v" + meta.getVersion(), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("GitHub: ", NamedTextColor.GRAY)
                .append(Component.text(url, NamedTextColor.BLUE)
                        .clickEvent(ClickEvent.openUrl(url)))
                .appendNewline());

        if (sender.hasPermission(PERMISSION_ADMIN)) {
            sendUsage(sender);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Komendy administracyjne:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("  /shelfnames clear", NamedTextColor.YELLOW)
                .append(Component.text(" - usuwa wszystkie hologramy pluginu", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /shelfnames reload", NamedTextColor.YELLOW)
                .append(Component.text(" - restart pluginu", NamedTextColor.GRAY)));
    }

    private Component prefix() {
        return Component.text("[ShelfNames] ", NamedTextColor.AQUA);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1 || !sender.hasPermission(PERMISSION_ADMIN)) {
            return List.of();
        }
        String start = args[0].toLowerCase(Locale.ROOT);
        return ADMIN_SUBCOMMANDS.stream()
                .filter(s -> s.startsWith(start))
                .toList();
    }
}

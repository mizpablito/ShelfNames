package dev.mizio.mcPlugins.shelfNames.comand;

import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShelfNamesCommand implements CommandExecutor {

    private final HologramService holograms;

    public ShelfNamesCommand(HologramService holograms) {
        this.holograms = holograms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("shelfnames.clear")) {
            sender.sendMessage(Component.text("Brak uprawnień do tej komendy.").color(NamedTextColor.RED));
        }
        // /shelfnames clear
        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            holograms.removeAll();
            sender.sendMessage(Component.text("Wyczyszczono wszystkie hologramy utworzone przez ShelfNames.").color(NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Użycie: ").color(NamedTextColor.GOLD).append(Component.text("/shelfnames clear").color(NamedTextColor.YELLOW)));
        return true;
    }
}

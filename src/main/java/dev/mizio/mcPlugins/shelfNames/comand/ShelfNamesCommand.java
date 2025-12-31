package dev.mizio.mcPlugins.shelfNames.comand;

import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
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
        // /shelfnames clear
        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            holograms.removeAll();
            sender.sendMessage("§aWyczyszczono wszystkie hologramy utworzone przez ShelfNames.");
            return true;
        }

        sender.sendMessage("§eUżycie: /shelfnames clear");
        return true;
    }
}

package dev.mizio.mcPlugins.shelfNames.hologram;

import org.bukkit.entity.Player;

public interface HologramService {

    HologramHandle getOrCreate(Player player);

    void remove(Player player);

    void removeAll();
}

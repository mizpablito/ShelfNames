package dev.mizio.mcPlugins.shelfNames.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface HologramHandle {

    void update(Player player, Location location, List<String> lines);

    void remove();
}

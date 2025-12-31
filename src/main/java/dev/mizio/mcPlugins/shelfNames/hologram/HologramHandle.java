package dev.mizio.mcPlugins.shelfNames.hologram;

import org.bukkit.Location;

import java.util.List;

public interface HologramHandle {

    void update(Location location, List<String> lines);

    void remove();
}

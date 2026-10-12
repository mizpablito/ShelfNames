package dev.mizio.mcPlugins.shelfNames.listener;

import dev.mizio.mcPlugins.shelfNames.MainShelfNames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Startuje/zatrzymuje zadanie śledzenia półek per-gracz
 * ({@link dev.mizio.mcPlugins.shelfNames.task.ShelfLookTask} na
 * {@code EntityScheduler}) w rytm dołączania/rozłączania graczy.
 */
public class PlayerSessionListener implements Listener {

    private final MainShelfNames plugin;

    public PlayerSessionListener(MainShelfNames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.startTrackingPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.stopTrackingPlayer(event.getPlayer());
    }
}

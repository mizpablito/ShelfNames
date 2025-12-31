package dev.mizio.mcPlugins.shelfNames;

import dev.mizio.mcPlugins.shelfNames.comand.ShelfNamesCommand;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramProvider;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import dev.mizio.mcPlugins.shelfNames.hologram.impl.FancyHologramService;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfCache;
import dev.mizio.mcPlugins.shelfNames.task.ShelfLookTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class MainShelfNames extends JavaPlugin {


    private HologramService hologramService;
    private ShelfCache shelfCache;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.shelfCache = new ShelfCache();
        this.hologramService = createHologramService();

        long interval = getConfig().getLong("update-interval-ticks", 20L);

        Bukkit.getScheduler().runTaskTimer(
                this,
                new ShelfLookTask(this, hologramService, shelfCache),
                0L,
                interval
        );

        getCommand("shelfnames").setExecutor(new ShelfNamesCommand(hologramService));
    }

    @Override
    public void onDisable() {
        if (hologramService != null) {
            hologramService.removeAll();
            hologramService = null;
        }
    }

    private HologramService createHologramService() {
        String providerRaw = getConfig().getString("holograms.provider", "FANCY");
        HologramProvider provider = HologramProvider.valueOf(providerRaw.toUpperCase());

        return switch (provider) {
            case FANCY -> new FancyHologramService();
            // case DECENT -> new DecentHologramService(); // dołożysz później
            default -> new FancyHologramService();
        };
    }
}


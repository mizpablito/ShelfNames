package dev.mizio.mcPlugins.shelfNames;

import dev.mizio.mcPlugins.shelfNames.comand.ShelfNamesCommand;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramProvider;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import dev.mizio.mcPlugins.shelfNames.hologram.impl.FancyHologramService;
import dev.mizio.mcPlugins.shelfNames.hologram.impl.StandaloneHologramService;
import dev.mizio.mcPlugins.shelfNames.listener.PlayerSessionListener;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfCache;
import dev.mizio.mcPlugins.shelfNames.task.ShelfLookTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public final class MainShelfNames extends JavaPlugin {

    @Getter
    private HologramService hologramService;
    private ShelfCache shelfCache;
    @Getter
    private PluginConfig pluginConfig;

    // Jedno powtarzalne zadanie ShelfLookTask per gracz, zaplanowane na jego
    // EntityScheduler - Folia-bezpieczny odpowiednik dawnej jednej, globalnej
    // pętli po Bukkit.getOnlinePlayers().
    private final Map<UUID, ScheduledTask> lookTasks = new ConcurrentHashMap<>();

    private final AtomicInteger viewHologramCounter = new AtomicInteger(0);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginConfig = new PluginConfig();

        setupMetrics();
        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this), this);
        startRuntime();

        PluginCommand command = getCommand("shelfnames");
        if (command != null) {
            ShelfNamesCommand executor = new ShelfNamesCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        stopRuntime();
    }

    // ------------------------------------------------------------
    //  Runtime lifecycle (start / stop / reload)
    // ------------------------------------------------------------

    /**
     * (Re)loads the configuration from disk and (re)creates all live components:
     * the shelf cache, the hologram service and per-player look tasks for
     * everyone already online.
     */
    public synchronized void startRuntime() {
        reloadConfig();
        pluginConfig.load(this);

        this.shelfCache = new ShelfCache();
        this.hologramService = createHologramService();

        for (Player player : Bukkit.getOnlinePlayers()) {
            startTrackingPlayer(player);
        }
    }

    /**
     * Cancels every per-player look task and destroys every hologram created
     * by the plugin. bStats keeps running - it is set up once in {@link #onEnable()}.
     */
    public synchronized void stopRuntime() {
        lookTasks.values().forEach(ScheduledTask::cancel);
        lookTasks.clear();

        if (hologramService != null) {
            hologramService.removeAll();
            hologramService = null;
        }
        shelfCache = null;
    }

    /**
     * Full restart: stop the runtime (removing holograms), reload the config
     * and start every component again.
     */
    public synchronized void reloadRuntime() {
        stopRuntime();
        startRuntime();
    }

    // ------------------------------------------------------------
    //  Per-player tracking (Folia: EntityScheduler)
    // ------------------------------------------------------------

    /**
     * Starts (or restarts) the shelf-look task for a single player on their
     * own {@code EntityScheduler}. Called on plugin/reload start-up for every
     * already-connected player, and from {@link PlayerSessionListener} on join.
     */
    public void startTrackingPlayer(Player player) {
        long interval = Math.max(1L, pluginConfig.getUpdateIntervalTicks());

        ScheduledTask task = player.getScheduler().runAtFixedRate(
                this,
                new ShelfLookTask(this, player, hologramService, shelfCache),
                () -> stopTrackingPlayer(player),
                1L,
                interval
        );

        if (task != null) {
            lookTasks.put(player.getUniqueId(), task);
        }
    }

    /**
     * Cancels the player's look task and cleans up everything the plugin
     * tracks for them (hologram + cache). Called on quit, on the
     * {@code EntityScheduler} retired callback, and during {@link #stopRuntime()}
     * cleanup for individual entries.
     */
    public void stopTrackingPlayer(Player player) {
        ScheduledTask task = lookTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        if (hologramService != null) {
            hologramService.remove(player);
        }
        if (shelfCache != null) {
            shelfCache.clear(player);
        }
    }

    // ------------------------------------------------------------

    private void setupMetrics() {
        int pluginId = 33398;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SingleLineChart("hologram_views", this::getAndResetHoloViews));
        metrics.addCustomChart(new SimplePie("only_custom_names", () ->
                getPluginConfig().isOnlyCustomNames() ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("raytrace_distance", () ->
                getPluginConfig().getRayTraceBlocksMaxDistance() + " blocks"));
    }

    private HologramService createHologramService() {
        HologramProvider requested = pluginConfig.getHologramProvider();

        // AUTO → iteruj po enumie w kolejności
        if (requested == HologramProvider.AUTO) {
            for (HologramProvider provider : HologramProvider.values()) {
                if (provider == HologramProvider.AUTO) continue;

                if (isProviderAvailable(provider)) {
                    getLogger().info("Using hologram provider: " + provider.name());
                    return createService(provider);
                }
            }

            // teoretycznie nieosiągalne, ale bezpieczne
            return new StandaloneHologramService(this);
        }

        // Wymuszony provider
        if (isProviderAvailable(requested)) {
            getLogger().info("Using hologram provider: " + requested.name());
            return createService(requested);
        }

        // Fallback + info
        getLogger().warning(
                "Requested hologram provider " + requested.name() +
                        " is not available. Falling back to AUTO."
        );

        // Spróbuj inne integracje
        for (HologramProvider provider : HologramProvider.values()) {
            if (provider == HologramProvider.AUTO || provider == requested) continue;

            if (isProviderAvailable(provider)) {
                getLogger().info("Using fallback hologram provider: " + provider.name());
                return createService(provider);
            }
        }

        return new StandaloneHologramService(this);
    }

    private boolean isProviderAvailable(HologramProvider provider) {
        if (!provider.isExternal()) {
            return true;
        }
        return Bukkit.getPluginManager().isPluginEnabled(provider.getPluginName());
    }


    private HologramService createService(HologramProvider provider) {
        return switch (provider) {
            case FANCY -> new FancyHologramService(this);
            case STANDALONE -> new StandaloneHologramService(this);
            default -> throw new IllegalStateException("AUTO should not create service");
        };
    }


    public void incrementViewHoloCount() {
        viewHologramCounter.incrementAndGet();
    }

    public int getAndResetHoloViews() {
        return viewHologramCounter.getAndSet(0);
    }

}

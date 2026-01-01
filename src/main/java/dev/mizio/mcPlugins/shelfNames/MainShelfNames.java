package dev.mizio.mcPlugins.shelfNames;

import dev.mizio.mcPlugins.shelfNames.comand.ShelfNamesCommand;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramProvider;
import dev.mizio.mcPlugins.shelfNames.hologram.HologramService;
import dev.mizio.mcPlugins.shelfNames.hologram.impl.FancyHologramService;
import dev.mizio.mcPlugins.shelfNames.hologram.impl.StandaloneHologramService;
import dev.mizio.mcPlugins.shelfNames.shelf.ShelfCache;
import dev.mizio.mcPlugins.shelfNames.task.ShelfLookTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class MainShelfNames extends JavaPlugin {


    private HologramService hologramService;
    private ShelfCache shelfCache;
    @Getter
    private PluginConfig pluginConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginConfig = new PluginConfig();
        pluginConfig.load(getConfig());

        this.shelfCache = new ShelfCache();
        this.hologramService = createHologramService();

        long interval = pluginConfig.getUpdateIntervalTicks();

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
        Bukkit.getScheduler().cancelTasks(this);

        if (hologramService != null) {
            hologramService.removeAll();
            hologramService = null;
        }
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
//            case DECENT -> new DecentHologramService(this); // todo
            case STANDALONE -> new StandaloneHologramService(this);
            default -> throw new IllegalStateException("AUTO should not create service");
        };
    }



}


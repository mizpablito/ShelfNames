package dev.mizio.mcPlugins.shelfNames;

import dev.mizio.mcPlugins.shelfNames.hologram.HologramProvider;
import lombok.Data;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;

@Data
public class PluginConfig {

    private long updateIntervalTicks;
    private int rayTraceBlocksMaxDistance;
    private boolean onlyCustomNames;
    private boolean onlyOnePlayer;
    private double hologramOffsetY;
    private double hologramForwardOffset;
    private float hologramScale;
    private boolean hologramFixedPosition;
    private HologramProvider hologramProvider;

    // INTEGRATIONS FancyHolograms
    private boolean fancyHologramsTextShadow;
    private String fancyHologramsTextAlignment;
    private boolean fancyHologramsDefaultBackground;
    private Color fancyHologramsBackgroundARGB;

    // INTEGRATIONS Standalone
    private boolean standaloneTextShadow;
    private String standaloneTextAlignment;
    private boolean standaloneDefaultBackground;
    private Color standaloneBackgroundARGB;


    public void load(FileConfiguration config) {
        this.updateIntervalTicks = config.getLong("update-interval-ticks", 10L);
        this.rayTraceBlocksMaxDistance = config.getInt("rayTraceBlocks-max-distance", 5);
        this.onlyCustomNames = config.getBoolean("only-custom-names", true);
        this.onlyOnePlayer = config.getBoolean("only-one-player", true);

        String providerRaw = config.getString("hologram.provider", "AUTO");
        try {
            this.hologramProvider = HologramProvider.valueOf(providerRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.hologramProvider = HologramProvider.AUTO;
        }

        this.hologramOffsetY = config.getDouble("hologram.offset-y", 0.8);
        this.hologramForwardOffset = config.getDouble("hologram.forward-offset", -0.1);
        this.hologramFixedPosition = config.getBoolean("hologram.position-fixed", true);
        this.hologramScale = (float) config.getDouble("hologram.scale", 1.0);

        // INTEGRATIONS FancyHolograms
        this.fancyHologramsTextShadow = config.getBoolean("integration.fancyHolograms.textShadow", true);
        this.fancyHologramsTextAlignment = config.getString("integration.fancyHolograms.textAlignment", "CENTER");
        this.fancyHologramsDefaultBackground = config.getBoolean("integration.fancyHolograms.defaultBackground", false);
        this.fancyHologramsBackgroundARGB = Color.fromARGB(
            config.getInt("integration.fancyHolograms.backgroundARGB.alpha", 60),
            config.getInt("integration.fancyHolograms.backgroundARGB.red", 0),
            config.getInt("integration.fancyHolograms.backgroundARGB.green", 0),
            config.getInt("integration.fancyHolograms.backgroundARGB.blue", 0)
        );

        // INTEGRATIONS Standalone
        this.standaloneTextShadow = config.getBoolean("integration.standalone.textShadow", true);
        this.standaloneTextAlignment = config.getString("integration.standalone.textAlignment", "CENTER");
        this.standaloneDefaultBackground = config.getBoolean("integration.standalone.defaultBackground", false);
        this.standaloneBackgroundARGB = Color.fromARGB(
                config.getInt("integration.standalone.backgroundARGB.alpha", 60),
                config.getInt("integration.standalone.backgroundARGB.red", 0),
                config.getInt("integration.standalone.backgroundARGB.green", 0),
                config.getInt("integration.standalone.backgroundARGB.blue", 0)
        );

    }
}
package dev.mizio.mcPlugins.shelfNames.hologram;

public enum HologramProvider {

    AUTO(null),                 // tryb wyboru
    FANCY("FancyHolograms"),
    STANDALONE(null);           // zawsze dostępny

    // DecentHolograms nie jest wspierany: jego publiczne API nie udostępnia
    // przypięcia (billboard FIXED), skali ani tła, więc integracji nie da się
    // zrealizować tak jak dla FancyHolograms czy API Bukkit. Zamiennik: STANDALONE.

    private final String pluginName;

    HologramProvider(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public boolean isExternal() {
        return pluginName != null;
    }
}

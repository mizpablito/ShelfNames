package dev.mizio.mcPlugins.shelfNames.hologram;

public enum HologramProvider {

    AUTO(null),                 // tryb wyboru
    FANCY("FancyHolograms"),
//    DECENT("DecentHolograms"),
    STANDALONE(null);           // zawsze dostępny

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

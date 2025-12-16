package me.macmoment.customarmor.config;

import me.macmoment.customarmor.CustomArmor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages plugin configuration
 * Provides easy access to all config values
 */
public class ConfigManager {
    private final CustomArmor plugin;
    private FileConfiguration config;

    public ConfigManager(CustomArmor plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // Prefix and colors
    public String getPrefix() {
        return config.getString("prefix", "&8[<##FF9100>&lA<##FFB047>&lR<##FFCE8D>&lM<##FFB047>&lO<##FF9100>&lR&8] &8»&7");
    }

    public String getAccentColor() {
        return config.getString("accent-color", "<##FFCE8D>");
    }

    // Upgrade settings
    public int getDiscountPerTier() {
        return config.getInt("upgrade-settings.discount-per-tier", 15);
    }

    public int getMinimumPrice() {
        return config.getInt("upgrade-settings.minimum-price", 10);
    }

    // Armor tier data
    public String getArmorName(int tier) {
        return config.getString("armor-tiers." + tier + ".name", "Unknown");
    }

    public Color getArmorRGBColor(int tier) {
        String rgb = config.getString("armor-tiers." + tier + ".rgb-color", "150,150,150");
        String[] parts = rgb.split(",");
        try {
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return Color.fromRGB(150, 150, 150);
        }
    }

    public String getArmorHexColor(int tier) {
        return config.getString("armor-tiers." + tier + ".hex-color", "<##969696>");
    }

    public double getArmorMultiplier(int tier) {
        return config.getDouble("armor-tiers." + tier + ".multiplier", 0.05);
    }

    public int getArmorPrice(int tier) {
        return config.getInt("armor-tiers." + tier + ".price", 25);
    }

    public String getArmorHeadTexture(int tier) {
        return config.getString("armor-tiers." + tier + ".head-texture", "");
    }

    // Messages
    public String getMessage(String path) {
        return config.getString("messages." + path, "&cMessage not found: " + path);
    }

    public String getAdminMessage(String key) {
        return getMessage("admin." + key);
    }

    public String getEssenceMessage(String key) {
        return getMessage("essence." + key);
    }

    public String getShopMessage(String key) {
        return getMessage("shop." + key);
    }

    // GUI settings
    public String getGUITitle() {
        return config.getString("gui.title", "&8armor browser &7(Page {page})");
    }

    public String getStatsItemName() {
        return config.getString("gui.stats-item-name", "{accent}{player}'s stats");
    }

    public String getNavigationPrevious() {
        return config.getString("gui.navigation.previous", "&cPrevious Page");
    }

    public String getNavigationNext() {
        return config.getString("gui.navigation.next", "&aNext Page");
    }

    // Check if tier exists in config
    public boolean hasTier(int tier) {
        return config.contains("armor-tiers." + tier);
    }

    public int getMaxTier() {
        ConfigurationSection section = config.getConfigurationSection("armor-tiers");
        if (section != null) {
            return section.getKeys(false).stream()
                .filter(key -> key.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(10);
        }
        return 10;
    }
}
